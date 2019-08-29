package coyote.metrics.prom;

import coyote.metrics.ScoreCard;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PushGatewayClientTest {

  @Test
  @Tag("integration")
  void standardUseCase() throws IOException {

    // create some monitors
    ScoreCard.startTimer("timer1").setDescription("Test time in milliseconds");
    ScoreCard.incrementCounter("counter1");
    ScoreCard.incrementGauge("gauge1");
    ScoreCard.increaseCounter("counter2", 22);
    ScoreCard.increaseCounter("counter2", 20);
    ScoreCard.increaseGauge("gauge2", 52);
    ScoreCard.decreaseGauge("gauge2", 10);
    ScoreCard.stopTimer("timer1");

    // push them to the gateway using the defaults (http://localhost:9091)
    new PushGatewayClient().push("tmetrixTest");
  }


  /**
   *
   */
  @Test
  void testingUseCase() {
    // create monitors with the following labels:
    // - metric_name - the name of the metric to use (the monitor name will be used as the job name instead)

    // At the end of the job call the client to collect and send metrics based on the internally set metric name:
    // client.pushJobNamedMetrics() - the entire scoreboard
    // client.pushJobNamedMetrics(monitors) - just the given monitors
    // Each monitor will:
    // - have its name base on the internally set (label) name of the monitor
    // - have its job name base on the name of the monitor (unless overridden or missing)
    // - have its description used as the help text
    // - have each of the remaining labels represented (except for job_name and metric_name)
    // - result in each metric pushed (posted) separately

  }

}