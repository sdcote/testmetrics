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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class TimingTimerTest {


  @Test
  public void testGetAccrued() {
    Timer monitor = new TimingTimer();

    for (int x = 0; x < 10; x++) {
      try {
        monitor.start();
        Thread.sleep(10);
        monitor.stop();
        Thread.sleep(500);
        System.out.println(monitor.getAccrued());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    assertTrue(monitor.getAccrued() < 500, "Too much time accrued");

    System.out.println(monitor.getAccrued());

  }

}