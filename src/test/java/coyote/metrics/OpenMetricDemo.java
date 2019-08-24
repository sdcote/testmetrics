package coyote.metrics;

public class OpenMetricDemo {

  public static void main (String[] args){


    // When a test starts
    ScoreCard.startTimer("Name of the scenario").addLabel("metric_name", "performance_test_duration").addLabel("env","dev");

    // The test executes
    ScoreCard.stopTimer("Name of the scenario");

    // This happens many times, for all the test scenarios in our test cases

    // At this point we have a collection of timer "jobs" - the name of the timer is the name of the "job".

    // Now we ask the Metrics Formatter to convert all the timers with a "metric_name" label of "performance_test_duration"
    String metrics = MetricFormatter.convertTimersToOpenMetrics("performance_test_duration","Help text for the metric");
    // this scans all the timers with a label with a name of "metric_name" which matches "performance_test_duration" and
    // creates an OpenMetric formatted line for that metric. All other labels are added as labels to the OpenMetric record.


  }
}
