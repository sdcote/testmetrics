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

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 */
public class ScoreCardPerf {

  /**
   * Run a 10 second test.
   *
   * @return the actual elapsed time.
   */
  private static long runTest() {
    long started = System.currentTimeMillis();
    long end = started + 10000;
    while (System.currentTimeMillis() <= end) {
      ScoreCard.incrementCounter("DemoCounter");
    }
    return System.currentTimeMillis() - started;
  }


  public static void main(String[] args) {

    System.out.println("Initialized - starting test...");

    // don't include counter creation in the measures
    Counter counter = ScoreCard.getCounter("DemoCounter");

    long totalElapsed = 0;
    long totalCount = 0;

    int runs = 10;
    for (int x = 0; x < runs; x++) {
      totalElapsed += runTest();
      totalCount += counter.getValue();
      counter.reset();
    }

    //  2,322,454.52 calls per second on a Pentium 3 JVM 4
    // 23,356,136.00 calls per second on a i7 Haswell JVM 7  =  0.04 microseconds per call?
    // 22,972,196 calls per second on i7 Java 8

    System.out.println("Throughput = " + NumberFormat.getNumberInstance(Locale.US).format((((float) totalCount / (float) totalElapsed) * 10000) / runs) + " calls per second");
  }
}
