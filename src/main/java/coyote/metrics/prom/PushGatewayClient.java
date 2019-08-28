package coyote.metrics.prom;

import coyote.metrics.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a utility class to send data to a PushGateway
 * <p>
 * To test things out, run the push gateway locally:
 * <pre>docker run -d -p 9091:9091 prom/pushgateway</pre>
 * See https://hub.docker.com/r/transactcharlie/pushgateway/ for details
 */

public class PushGatewayClient {
  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";
  public static final String DEFAULT_URL = "http://localhost:9091/metrics/";
  private static final int MILLISECONDS_PER_SECOND = 1000;
  private String gatewayUrl;
  private HttpConnectionFactory connectionFactory;

  public PushGatewayClient() {
    gatewayUrl = DEFAULT_URL;
    connectionFactory = new DefaultHttpConnectionFactory();
  }

  private static String base64url(String v) {
    // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
    return DatatypeConverter.printBase64Binary(v.getBytes(StandardCharsets.UTF_8)).replace("+", "-").replace("/", "_");
  }

  private static String readFromStream(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
  }

  public PushGatewayClient setUrl(String url) {
    gatewayUrl = URI.create(url + "/metrics/").normalize().toString();
    return this;
  }

  public PushGatewayClient setCredentials(String username, String password) {
    connectionFactory = new BasicAuthHttpConnectionFactory(connectionFactory, username, password);
    return this;
  }

  /**
   *
   * @param job primary grouping element representing the name of the job to which these metrics apply.
   * @param groupingKey additional grouping pairs such as "instance-myhost"
   * @param method One of the HTTP methods (e.g. POST, PUT, and DELETE)
   * @param monitors A list of monitors (gauges, counters, and timers) to push.
   * @throws IOException
   */
  void doRequest(String job, Map<String, String> groupingKey, String method, List<Monitor> monitors) throws IOException {
    String url = gatewayUrl;
    if (job.contains("/")) {
      url += "job@base64/" + base64url(job);
    } else {
      url += "job/" + URLEncoder.encode(job, "UTF-8");
    }

    if (groupingKey != null) {
      for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
        if (entry.getValue().contains("/")) {
          url += "/" + entry.getKey() + "@base64/" + base64url(entry.getValue());
        } else {
          url += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
        }
      }
    }

    HttpURLConnection connection = connectionFactory.create(url);
    connection.setRequestProperty("Content-Type", CONTENT_TYPE_004);
    if (!method.equals("DELETE")) {
      connection.setDoOutput(true);
    }
    connection.setRequestMethod(method);
    connection.setConnectTimeout(10 * MILLISECONDS_PER_SECOND);
    connection.setReadTimeout(10 * MILLISECONDS_PER_SECOND);
    connection.connect();

    try {
      if (!method.equals("DELETE")) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
        MetricFormatter.convertToOpenMetrics(writer, monitors);
        writer.flush();
        writer.close();
      }

      int response = connection.getResponseCode();
      if (response != HttpURLConnection.HTTP_ACCEPTED) {
        String errorMessage;
        InputStream errorStream = connection.getErrorStream();
        if (response >= 400 && errorStream != null) {
          String errBody = readFromStream(errorStream);
          errorMessage = "Response code from " + url + " was " + response + ", response body: " + errBody;
        } else {
          errorMessage = "Response code from " + url + " was " + response;
        }
        throw new IOException(errorMessage);
      }
    } finally {
      connection.disconnect();
    }
  }

  /**
   * @param metricName name of the metrics to push (e.g. "test_duration")
   */
  public void push(String metricName) {
    push(metricName, null);
  }


  /**
   * Scans through the scorecard and finds all the monitors (counters, gauges and Timers) which have the label of
   * "metric_name" which matches the given value (i.e. the name of the metric to push).
   *
   * @param metricName name of the metrics to push (e.g. "test_duration")
   * @param jobName    name of the job (e.g. "integration_test")
   */
  public void push(String metricName, String jobName) {
    if (metricName != null) {
      List<Monitor> monitors = new ArrayList<>();
      for (Iterator<TimingMaster> it = ScoreCard.getTimerIterator(); it.hasNext(); ) {
        TimingMaster timer = it.next();
        if (metricName.equals(timer.getLabelValue(MetricFormatter.METRIC_NAME_LABEL))) monitors.add(timer);
      }
      for (Iterator<Counter> it = ScoreCard.getCounterIterator(); it.hasNext(); ) {
        Metric metric = it.next();
        if (metricName.equals(metric.getLabelValue(MetricFormatter.METRIC_NAME_LABEL))) monitors.add(metric);
      }
      for (Iterator<Gauge> it = ScoreCard.getGaugeIterator(); it.hasNext(); ) {
        Metric metric = it.next();
        if (metricName.equals(metric.getLabelValue(MetricFormatter.METRIC_NAME_LABEL))) monitors.add(metric);
      }

      for(Monitor monitor:monitors){
        if( monitor instanceof Counter){
          System.out.println( "counter");
        } else if( monitor instanceof Gauge){
          System.out.println( "gauge");
        }if( monitor instanceof TimerMaster){
          System.out.println( "timer");
        } else {
          System.out.println( monitor.getClass().getSimpleName());
        }
      }
    }


  }
}
