package coyote.metrics;

public class OpenMetricDemo {

  public static void main(String[] args) {

    // When a test starts we create and start the timer in the @Before hook
    ScoreCard.startTimer("Name of the scenario")
            .addLabel(MetricFormatter.METRIC_NAME_LABEL, "performance_test_duration")
            .addLabel(MetricFormatter.METRIC_HELP_LABEL, "The duration of the test job")
            .addLabel("env", "dev");

    // The test executes

    // Then we stop the timer in the @After hook
    ScoreCard.stopTimer("Name of the scenario");

    // This happens many times, for all the test scenarios in our test cases

    // At this point we have a collection of timer "jobs" - the name of the timer is the name of the "job".

    // Now we ask the Metrics Formatter to convert all the timers with a "metric_name" label of "performance_test_duration"
    String timerMetrics = MetricFormatter.convertTimersToOpenMetrics("performance_test_duration");
    // this scans all the timers with a label with a name of "metric_name" which matches "performance_test_duration" and
    // creates an OpenMetric formatted line for that metric. All other labels are added as labels to the OpenMetric record.

    // counters
    String counterMetrics = MetricFormatter.convertCountersToOpenMetrics("performance_test_step_count");

    // gauges
    String gaugeMetrics = MetricFormatter.convertGaugesToOpenMetrics("performance_test_failure_count");

    // All our metrics to be sent to the PushGateway, or posted to an exporter for Prometheus to scrape
    String metrics = timerMetrics.concat(counterMetrics.concat(gaugeMetrics));

  }
}
