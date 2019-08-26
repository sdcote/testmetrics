package coyote.metrics.prom;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

/**
 * This is a utility class to send data to a PushGateway
 *
 * To test things out, run the push gateway locally:
 * <pre>docker run -d -p 9091:9091 prom/pushgateway</pre>
 * See https://hub.docker.com/r/transactcharlie/pushgateway/ for details
 *
 */

public class PushGatewayClient {
  /**
   * Content-type for text version 0.0.4.
   */
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

  private static final int MILLISECONDS_PER_SECOND = 1000;
  public static final String DEFAULT_URL = "http://localhost:9091";
  private String gatewayUrl;
  private HttpConnectionFactory connectionFactory;

  public PushGatewayClient() {
    gatewayUrl = DEFAULT_URL;
    connectionFactory = new DefaultHttpConnectionFactory();
  }

  public PushGatewayClient setUrl(String url) {
    try {
      gatewayUrl = new URL(url).toString();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL: " + url, e);
    }
    return this;
  }



  void doRequest(String job, Map<String, String> groupingKey, String method) throws IOException {
    String url = gatewayUrl;
    if (job.contains("/")) {
      url += "job@base64/" + base64url(job);
    } else {
      url += "job/" + URLEncoder.encode(job, "UTF-8");
    }

    if (groupingKey != null) {
      for (Map.Entry<String, String> entry: groupingKey.entrySet()) {
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
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        // TextFormat.write004(writer, registry.metricFamilySamples());
        writer.flush();
        writer.close();
      }

      int response = connection.getResponseCode();
      if (response != HttpURLConnection.HTTP_ACCEPTED) {
        String errorMessage;
        InputStream errorStream = connection.getErrorStream();
        if(response >= 400 && errorStream != null) {
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

  private static String base64url(String v) {
    // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
    try {
      return DatatypeConverter.printBase64Binary(v.getBytes("UTF-8")).replace("+", "-").replace("/", "_");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);  // Unreachable.
    }
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
   * Write out the text version 0.0.4 of the given MetricFamilySamples.
   */
//  public static void write004(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
//    /* See http://prometheus.io/docs/instrumenting/exposition_formats/
//     * for the output format specification. */
//    while(mfs.hasMoreElements()) {
//      //Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
//      writer.write("# HELP ");
//      writer.write(metricFamilySamples.name);
//      writer.write(' ');
//      writeEscapedHelp(writer, metricFamilySamples.help);
//      writer.write('\n');
//
//      writer.write("# TYPE ");
//      writer.write(metricFamilySamples.name);
//      writer.write(' ');
//      writer.write(typeString(metricFamilySamples.type));
//      writer.write('\n');
//
//      for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
//        writer.write(sample.name);
//        if (sample.labelNames.size() > 0) {
//          writer.write('{');
//          for (int i = 0; i < sample.labelNames.size(); ++i) {
//            writer.write(sample.labelNames.get(i));
//            writer.write("=\"");
//            writeEscapedLabelValue(writer, sample.labelValues.get(i));
//            writer.write("\",");
//          }
//          writer.write('}');
//        }
//        writer.write(' ');
//        writer.write(Collector.doubleToGoString(sample.value));
//        if (sample.timestampMs != null){
//          writer.write(' ');
//          writer.write(sample.timestampMs.toString());
//        }
//        writer.write('\n');
//      }
//    }
//  }



  private static void writeEscapedHelp(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }

  private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\"':
          writer.append("\\\"");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }


  /**
   *
   * @param metricName
   * @param jobName
   */
  public void push(String metricName, String jobName) {
    // Scan the ScoreCard for all the Timers, Counters and Gauges with a matching metric name label, and send them to
    // the PushGateway.

  }
}
