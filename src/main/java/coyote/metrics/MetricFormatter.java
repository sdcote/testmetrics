package coyote.metrics;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MetricFormatter {
  public static final String METRIC_NAME_LABEL = "metric_name";

  public static String toJson(Timer timer) {
    StringBuilder sb = new StringBuilder("{");
    TimerMaster master = timer.getMaster();
    if (master != null) {
      sb.append(getField(TimingMaster.NAME, master.getName()));
      sb.append(',');
      sb.append(getField(TimingMaster.HITS, Integer.toString(master.getHits())));
      sb.append(',');

      if ((master.getHits() - master.getActiveCounter()) > 0) {
        sb.append(getField(TimingMaster.AVG, Long.toString(master.getAverage())));
        sb.append(',');
        sb.append(getField(TimingMaster.TOTAL, Long.toString(master.getTotal())));
        sb.append(',');
        sb.append(getField(TimingMaster.STANDARD_DEVIATION, Long.toString(master.getStandardDeviation())));
        sb.append(',');
        sb.append(getField(TimingMaster.MIN, Long.toString(master.getMinimum())));
        sb.append(',');
        sb.append(getField(TimingMaster.MAX, Long.toString(master.getMaximum())));
        sb.append(',');
      }
      // TODO: add labels
      sb.append(getField(TimingMaster.ACTIVE, Long.toString(master.getActiveCounter())));
      sb.append(',');
      sb.append(getField(TimingMaster.MAXACTIVE, Long.toString(master.getMaxActive())));
      sb.append(',');
      sb.append(getField(TimingMaster.AVGACTIVE, Float.toString(master.getAvgActive())));
      sb.append(',');
      sb.append(getField(TimingMaster.FIRSTACCESS, getDateString(master.getFirstAccessTime())));
      sb.append(',');
      sb.append(getField(TimingMaster.LASTACCESS, getDateString(master.getLastAccessTime())));
    }
    sb.append("}");
    return sb.toString();
  }


  private static String getDateString(final long time) {
    if (time == 0) {
      return "";
    } else {
      return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(new Date(time));
    }
  }


  private static String getField(String name, String value) {
    return getQuoted(name).concat(":").concat(getQuoted(value));
  }

  private static String getQuoted(String value) {
    return "\"" + value + "\"";
  }


  /**
   * Create a set of OpenMetric representations of all the counters, timers, and gauges in the Scorecard which have a
   * label name of "metric_name".
   *
   * <p>If the timer, counter or gauge contains a label with the name of "metric_help", that label will be used as the
   * help text for the returned metric.</p>
   *
   * <p>The "job" will be the name of the timer, gauge or counter and the "instance" will be the hostname retrieved
   * from the ScoreCard.</p>
   *
   * @return a set of OpenMetric records each terminated with a new line character, or an empty string if no timers,
   * counter or gauges were found in the ScoreCard.
   */
  public static String convertScoreCardToOpenMetrics() {
    Writer writer = new StringWriter();
    //TODO: implement this
    // get a list of all the metric names for all types of metrics
    // call all three types with each metric name to group metric names together so headers are not repeated unnecessarily
    // concatenate all the results
    return writer.toString();
  }


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

  public static void convertToOpenMetrics(Writer writer, List<Monitor> monitors) throws IOException {
    convertToOpenMetrics(writer, monitors, false);
  }

  public static void convertToOpenMetrics(Writer writer, List<Monitor> monitors, boolean honorMetricNameLabel) throws IOException {

    for (Monitor monitor : monitors) {
      String metricName = monitor.getName();

      if (honorMetricNameLabel && monitor.hasLabel(METRIC_NAME_LABEL)) {
        metricName = monitor.getLabelValue(METRIC_NAME_LABEL);
      }

      if (monitor.getDescription() != null && monitor.getDescription().trim().length() > 0) {
        writer.append("# HELP ");
        writer.append(metricName);
        writer.write(' ');
        writeEscapedHelp(writer, monitor.getDescription().trim());
        writer.append("\n");
      }
      writer.append("# TYPE ");
      writer.append(metricName);
      if (monitor instanceof Gauge) {
        writer.append(" gauge");
      } else if (monitor instanceof Counter) {
        writer.append(" counter");
      } else {
        writer.append(" gauge");
      }
      writer.append("\n");

      writer.append(metricName);
      writer.append(" ");

      List<String> names = monitor.labelNames();
      if (honorMetricNameLabel) {
        for (Iterator<String> itr = names.iterator(); itr.hasNext(); ) {
          String name = itr.next();
          if (name.equalsIgnoreCase(METRIC_NAME_LABEL)) {
            itr.remove();
          }
        }
      }

      if (names.size() > 0) {
        writer.write('{');
        for (int i = 0; i < names.size(); ++i) {
          writer.write(names.get(i));
          writer.write("=\"");
          writeEscapedLabelValue(writer, monitor.getLabelValue(names.get(i)));
          writer.write("\"");
          if (i + 1 < names.size()) writer.write(",");
        }
        writer.write("} ");
      }

      writer.append(Long.toString(monitor.getValue()));
      writer.append("\n");
    } // for each monitor
  }

}
