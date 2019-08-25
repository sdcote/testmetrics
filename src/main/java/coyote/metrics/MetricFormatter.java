package coyote.metrics;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

public class MetricFormatter {
  public static final String METRIC_NAME_LABEL = "metric_name";
  public static final String METRIC_HELP_LABEL = "metric_help";

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
   * Create a set of OpenMetric representations of all the timers in the Scorecard which have a label name of
   * "metric_name" which matches the given name.
   *
   * <p>If the Timer contains a label with the name of "metric_help", that label will be used as the help text for the
   * returned metric.</p>
   *
   * <p>The "job" will be the name of the timer and the "instance" will be the hostname retrieved from the ScoreCard.</p>
   *
   * @param metricName the name of the metric
   * @return a set of OpenMetric records each terminated with a new line character, or an empty string if no timers
   * were found with the matching labels.
   */
  public static String convertTimersToOpenMetrics(String metricName) {
    StringBuilder sb = new StringBuilder();
    //TODO: implement this
    boolean outputHelp = false;
    boolean outputType = false;
    for (Iterator<TimingMaster> it = ScoreCard.getTimerIterator(); it.hasNext(); ) {
      Timer timer = (Timer) it.next();
      if (timer.hasLabel(METRIC_NAME_LABEL) && metricName.equals(timer.getLabel(METRIC_NAME_LABEL))) {
        if (!outputHelp && timer.hasLabel(METRIC_HELP_LABEL) && timer.getLabel(METRIC_HELP_LABEL).trim().length() > 0) {
          sb.append("HELP ");
          sb.append(timer.getLabel(METRIC_HELP_LABEL).trim());
          sb.append("\n");
          outputHelp = true;
        }
        if (!outputType) {
          sb.append("TYPE ");
          sb.append(metricName);
          sb.append(" counter\n");
          outputType = true;
        }
        sb.append(metricName);
        sb.append(" ");
        sb.append(getLabels(timer));
        sb.append(" ");
        sb.append(timer.getAccrued());
        sb.append("\n");
      }
    }
    return sb.toString();
  }


  /**
   * Create a set of OpenMetric representations of all the counters in the Scorecard which have a label name of
   * "metric_name" which matches the given name.
   *
   * <p>If the Counter contains a label with the name of "metric_help", that label will be used as the help text for
   * the returned metric.</p>
   *
   * <p>The "job" will be the name of the counter and the "instance" will be the hostname retrieved from the ScoreCard.</p>
   *
   * @param metricName the name of the metric
   * @return a set of OpenMetric records each terminated with a new line character, or an empty string if no counters
   * were found with the matching labels.
   */
  public static String convertCountersToOpenMetrics(String metricName) {
    StringBuilder sb = new StringBuilder();
    //TODO: implement this
    boolean outputHelp = false;
    boolean outputType = false;
    if (metricName != null) {
      for (Iterator<Counter> it = ScoreCard.getCounterIterator(); it.hasNext(); ) {
        Counter counter = it.next();
        if (counter.hasLabel(METRIC_NAME_LABEL) && metricName.equals(counter.getLabel(METRIC_NAME_LABEL))) {
          if (!outputHelp && counter.hasLabel(METRIC_HELP_LABEL) && counter.getLabel(METRIC_HELP_LABEL).trim().length() > 0) {
            sb.append("HELP ");
            sb.append(counter.getLabel(METRIC_HELP_LABEL).trim());
            sb.append("\n");
            outputHelp = true;
          }
          if (!outputType) {
            sb.append("TYPE ");
            sb.append(metricName);
            sb.append(" counter\n");
            outputType = true;
          }
          sb.append(metricName);
          sb.append(" ");
          sb.append(getLabels(counter));
          sb.append(" ");
          sb.append(counter.getValue());
          sb.append("\n");
        }
      }
    }
    return sb.toString();
  }


  /**
   * Create a set of OpenMetric representations of all the gauges in the Scorecard which have a label name of
   * "metric_name" which matches the given name.
   *
   * <p>If the Gauge contains a label with the name of "metric_help", that label will be used as the help text for the
   * returned metric.</p>
   *
   * <p>The "job" will be the name of the gauge and the "instance" will be the hostname retrieved from the ScoreCard.</p>
   *
   * @param metricName the name of the metric
   * @return a set of OpenMetric records each terminated with a new line character, or an empty string if no gauges
   * were found with the matching labels.
   */
  public static String convertGaugesToOpenMetrics(String metricName) {
    StringBuilder sb = new StringBuilder();
    //TODO: implement this
    boolean outputHelp = false;
    boolean outputType = false;
    if (metricName != null) {
      for (Iterator<Gauge> it = ScoreCard.getGaugeIterator(); it.hasNext(); ) {
        Gauge gauge = it.next();
        if (gauge.hasLabel(METRIC_NAME_LABEL) && metricName.equals(gauge.getLabel(METRIC_NAME_LABEL))) {
          if (!outputHelp && gauge.hasLabel(METRIC_HELP_LABEL) && gauge.getLabel(METRIC_HELP_LABEL).trim().length() > 0) {
            sb.append("HELP ");
            sb.append(gauge.getLabel(METRIC_HELP_LABEL).trim());
            sb.append("\n");
            outputHelp = true;
          }
          if (!outputType) {
            sb.append("TYPE ");
            sb.append(metricName);
            sb.append(" gauge\n");
            outputType = true;
          }
          sb.append(metricName);
          sb.append(" ");
          sb.append(getLabels(gauge));
          sb.append(" ");
          sb.append(gauge.getValue());
          sb.append("\n");
        }
      }
    }
    return sb.toString();
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
    StringBuilder sb = new StringBuilder();
    //TODO: implement this
    // get a list of all the metric names for all types of metrics
    // call all three types with each metric name to group metric names together so headers are not repeated unnecessarily
    // concatenate all the results
    return sb.toString();
  }


  private static String getLabels(Labeled labeled) {
    StringBuilder sb = new StringBuilder();
    //TODO: implement this
    return sb.toString();
  }

}
