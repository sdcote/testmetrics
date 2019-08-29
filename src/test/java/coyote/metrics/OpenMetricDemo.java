package coyote.metrics;

import coyote.metrics.prom.PushGatewayClient;

import java.io.IOException;
import java.util.Random;

/**
 * Collecting metrics for tests is different than standard APM use cases. It is helpful to think about how the metrics
 * will be reported. Timers collect information on duration, so the metric name will usually be based on that; e.g.
 * "test_duration" or simply "duration". When using timers, the average of all the hits are reported.
 * <p>
 * The job label is used to differentiate the same metric (like "duration"). For testing purposes, the name of the test
 * can be used as the job label. In this way, durations of a test can be treated separately. The "login" duration can
 * be reported separately from the "retrieve balance" duration.
 */
public class OpenMetricDemo {

  public static void main(String[] args) throws IOException {

    for (int i = 0; i < 3; i++) {

      // When a test starts we create and start the timer in the @Before hook
      ScoreCard.startTimer("Name of the scenario")
              .setDescription("The duration of the test scenario in milliseconds")
              .addLabel(MetricFormatter.METRIC_NAME_LABEL, "test_duration")
              .addLabel("env", "development");

      // The test executes
      try {
        Thread.sleep(new Random().nextInt(1000));
      } catch (InterruptedException e) {
      }

      // Then we stop the timer in the @After hook
      System.out.println(ScoreCard.stopTimer("Name of the scenario").getMaster().getTotal());

    } // This happens many times, for all the test scenarios in our test cases

    System.out.println(ScoreCard.getTimerMaster("Name of the scenario"));

    // At this point we have a collection of timer "jobs" - the name of the timer is the name of the "job".

    // Now we ask the Metrics Formatter to convert all the timers with a "metric_name" label of "performance_test_duration"
    String timerMetrics = MetricFormatter.convertTimersToOpenMetrics("test_duration");
    System.out.println(timerMetrics);

    PushGatewayClient client = new PushGatewayClient().setUrl("http://localhost:9091").setCredentials("admin","admin");
    client.pushJobNamedMetrics("test_duration"); // matches the METRIC_NAME_LABEL above


  }
}
