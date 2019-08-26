package coyote.metrics;

import coyote.metrics.prom.PushGatewayClient;

import java.util.Random;

/**
 * Collecting metrics for tests is different than standard APM use cases. It is helpful to think about how the metrics
 * will be reported. Timers collect information on duration, so the metric name will usually be based on that; e.g.
 * "test_duration" or simply "duration".
 * <p>
 * The job label is used to differentiate the same metric (like "duration"). For testing purposes, the name of the test
 * can be used as the job label. In this way, durations of a test can be treated separately. The "login" duration can
 * be reported separately from the "retrieve balance" duration.
 */
public class OpenMetricDemo {

  public static void main(String[] args) {

    // When a test starts we create and start the timer in the @Before hook
    ScoreCard.startTimer("Name of the scenario")
            .addLabel(MetricFormatter.METRIC_NAME_LABEL, "test_duration")
            .addLabel(MetricFormatter.METRIC_HELP_LABEL, "The duration of the test job")
            .addLabel("env", "development");

    // The test executes
    try {
      Thread.sleep(new Random().nextInt(3000));
    } catch (InterruptedException e) {
    }
    // Then we stop the timer in the @After hook
    ScoreCard.stopTimer("Name of the scenario");

    // This happens many times, for all the test scenarios in our test cases

    // At this point we have a collection of timer "jobs" - the name of the timer is the name of the "job".

    // Now we ask the Metrics Formatter to convert all the timers with a "metric_name" label of "performance_test_duration"
    String timerMetrics = MetricFormatter.convertTimersToOpenMetrics("test_duration");
    // this scans all the timers with a label with a name of "metric_name" which matches "performance_test_duration" and
    // creates an OpenMetric formatted line for that metric. All other labels are added as labels to the OpenMetric record.

    // counters are expected only to increase. The batch processing nature of tests tend to make counters useless
    // String counterMetrics = MetricFormatter.convertCountersToOpenMetrics("test_step_count");

    // gauges
    String gaugeMetrics = MetricFormatter.convertGaugesToOpenMetrics("test_failure_count");

    // All our metrics to be sent to the PushGateway, or posted to an exporter for Prometheus to scrape
    //String metrics = timerMetrics.concat(counterMetrics.concat(gaugeMetrics));

    PushGatewayClient client = new PushGatewayClient();
    client.push("test_duration", "job_name");

  }
}
