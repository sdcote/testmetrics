/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote
 *      - Initial concept and implementation
 */
package coyote.metrics;

import org.junit.jupiter.api.Test;

/**
 *
 */
public class TimerDemo {


  @Test
  public void simpleDemo() {
    // timing instrumentation is disabled by default so we have to explicitly enable it
    ScoreCard.enableTiming();

    // Call the start timer method on the scorecard to start a timer with a correlating name
    Timer t1 = ScoreCard.startTimer("Demo");

    // Different named timers roll-up statistics separately
    Timer t2 = ScoreCard.startTimer("Test");

    // Stopping a timer totals the number of milliseconds between the start and stop calls
    t1.stop();

    // Timers can be re-started and stopped as necessary to accrue total time. This is helpful when trying to measure
    // only the time spent in methods and not waiting for calls to external systems.
    t1.start();
    t1.stop();
    // ... making a call to an external system not to be included in our time
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
    }
    t1.start();
    t1.stop(); // finally completed our processing

    // Measure total time spent in this method
    t2.stop(); // Test timer is stopped only once

    System.out.println(t1);
    System.out.println(t2);

    t1.getName();
    System.out.flush();
  }

}
