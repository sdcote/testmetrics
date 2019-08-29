package coyote.metrics.prom;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;

class PushGatewayClientTest {

  @Test
  void push() {
    Writer writer = new StringWriter();

    System.out.println(writer.toString());
  }

  @Test
  void standardUseCase() {

    // create some monitors

    // at the end of the job, send all of the collected metrics to the push gateway
    // client.push(monitors, jobName)
    // client.push(monitors, jobName, instanceName) overriding the instance name from that in the scorecard
    // each monitor will
    //   - have its name based on the name of the monitor
    //   - have its description used as the help text
    //   - have each of the labels represented

  }


  /**
   *
   */
  @Test
  void testingUseCase() {
    // create monitors with the following labels:
    // - metric_name - the name of the metric to use (the monitor name will be used as the job name instead)
    // - job_name

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