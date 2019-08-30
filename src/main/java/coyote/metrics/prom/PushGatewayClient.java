package coyote.metrics.prom;

import coyote.metrics.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This is a utility class to send data to a PushGateway
 * <p>To test things out, run the push gateway locally:
 * <pre>docker run -d -p 9091:9091 prom/pushgateway</pre>
 * See https://hub.docker.com/r/transactcharlie/pushgateway/ for details
 */

public class PushGatewayClient {
  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";
  public static final String DEFAULT_URL = "http://localhost:9091";
  private static final String POST = "POST";
  private static final int MILLISECONDS_PER_SECOND = 1000;
  private String gatewayUrl;
  private HttpConnectionFactory connectionFactory;

  /**
   * Default constructor pointing to http://localhost:9091
   */
  public PushGatewayClient() {
    setUrl(DEFAULT_URL);
    connectionFactory = new DefaultHttpConnectionFactory();
  }

  /**
   * Constructor specifying the location of the PushGateway
   *
   * @param url The scheme host and port (i.e. authority) of the PushGateway server
   */
  public PushGatewayClient(String url) {
    setUrl(url);
    connectionFactory = new DefaultHttpConnectionFactory();
  }

  /**
   * Constructor specifying the location of the PushGateway and basic auth credentials for preemptive authentication.
   *
   * @param url      The scheme host and port (i.e. authority) of the PushGateway server
   * @param username the username of the account to be authenticated
   * @param password the password credential to authenticate the account
   */
  public PushGatewayClient(String url, String username, String password) {
    setUrl(url);
    connectionFactory = new BasicAuthHttpConnectionFactory(connectionFactory, username, password);
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

  /**
   * Set the location of the PushGateway
   *
   * @param url The scheme host and port (i.e. authority) of the PushGateway server
   * @return this client instance for fluent method chaining
   */
  public PushGatewayClient setUrl(String url) {
    gatewayUrl = URI.create(url + "/metrics/").normalize().toString();
    return this;
  }

  /**
   * Set the basic auth credentials for preemptive authentication.
   *
   * @param username the username of the account to be authenticated
   * @param password the password credential to authenticate the account
   * @return this client instance for fluent method chaining
   */
  public PushGatewayClient setCredentials(String username, String password) {
    connectionFactory = new BasicAuthHttpConnectionFactory(connectionFactory, username, password);
    return this;
  }

  /**
   * Send the given monitors to the push gateway.
   *
   * @param job         primary grouping element representing the name of the job to which these metrics apply.
   * @param groupingKey additional grouping pairs such as "instance-myhost"
   * @param method      One of the HTTP methods (e.g. POST, PUT, and DELETE)
   * @param monitors    A list of monitors (gauges, counters, and timers) to push.
   * @throws IOException if there were problems sending metrics to the push gateway
   */
  void doRequest(String job, Map<String, String> groupingKey, String method, List<Monitor> monitors) throws IOException {
    doRequest(job, groupingKey, method, monitors, false);
  }

  /**
   * Send the given monitors to the push gateway.
   *
   * @param job                  primary grouping element representing the name of the job to which these metrics apply.
   * @param groupingKey          additional grouping pairs such as "instance-myhost"
   * @param method               One of the HTTP methods (e.g. POST, PUT, and DELETE)
   * @param monitors             A list of monitors (gauges, counters, and timers) to push.
   * @param honorMetricNameLabel true to use the "metric_name" label as the metric name and the monitor name as the job name
   * @throws IOException if there were problems sending metrics to the push gateway
   */
  void doRequest(String job, Map<String, String> groupingKey, String method, List<Monitor> monitors, boolean honorMetricNameLabel) throws IOException {
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
        MetricFormatter.convertToOpenMetrics(writer, monitors, honorMetricNameLabel);
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
   * Scans through the scorecard and finds all the monitors (counters, gauges and Timers) which have the label of
   * "metric_name" which matches the given value (i.e. the name of the metric to push).
   *
   * @param metricName name of the metrics to push (e.g. "test_duration")
   * @throws IOException if there were problems posting the ScoreCard to the push gateway
   */
  public void pushJobNamedMetrics(String metricName) throws IOException {
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
      Map<String, String> groupingKey = new HashMap<>();
      groupingKey.put("instance", ScoreCard.getHostname());
      List<Monitor> monitorList = new ArrayList<>();
      for (Monitor monitor : monitors) {
        monitorList.add(monitor); // only one monitor per request since monitor names are used as job names
        doRequest(monitor.getName(), groupingKey, POST, monitorList, true);
        monitorList.clear();
      }
    }
  }

  /**
   * Push all the metrics in the scorecard.
   *
   * <p>This is the primary use case. Components will use the ScoreCard and just before existing, a call to push all
   * collected metrics will be made.</p>
   *
   * @param jobName The name of the job these metrics represent
   * @throws IOException if there were problems posting the ScoreCard to the push gateway
   */
  public void push(String jobName) throws IOException {
    List<Monitor> monitors = new ArrayList<>();
    for (Iterator<TimingMaster> it = ScoreCard.getTimerIterator(); it.hasNext(); monitors.add(it.next())) ;
    for (Iterator<Counter> it = ScoreCard.getCounterIterator(); it.hasNext(); monitors.add(it.next())) ;
    for (Iterator<Gauge> it = ScoreCard.getGaugeIterator(); it.hasNext(); monitors.add(it.next())) ;

    Map<String, String> groupingKey = new HashMap<>();
    groupingKey.put("instance", ScoreCard.getHostname());
    groupingKey.put("job", jobName);

    doRequest(jobName, groupingKey, POST, monitors);
  }

  /**
   * Push all the metrics in the ScoreCard with the given name to the gateway.
   *
   * <p>Note: is is possible that up to 3 metrics will be sent if there is a Time, Counter and Gauge with the given
   * name.</p>
   *
   * @param jobName    The name of the job these metrics represent
   * @param metricName The name of the metric(s) to send.
   * @throws IOException
   */
  public void push(String jobName, String metricName) throws IOException {
    List<Monitor> monitors = new ArrayList<>();
    for (Iterator<TimingMaster> it = ScoreCard.getTimerIterator(); it.hasNext(); ) {
      TimingMaster timer = it.next();
      if (metricName.equals(timer.getName())) monitors.add(timer);
    }
    for (Iterator<Counter> it = ScoreCard.getCounterIterator(); it.hasNext(); ) {
      Metric metric = it.next();
      if (metricName.equals(metric.getName())) monitors.add(metric);
    }
    for (Iterator<Gauge> it = ScoreCard.getGaugeIterator(); it.hasNext(); ) {
      Metric metric = it.next();
      if (metricName.equals(metric.getName())) monitors.add(metric);
    }

    Map<String, String> groupingKey = new HashMap<>();
    groupingKey.put("instance", ScoreCard.getHostname());
    groupingKey.put("job", jobName);

    doRequest(jobName, groupingKey, POST, monitors);
  }

}
