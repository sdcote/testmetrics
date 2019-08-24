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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class TimingMasterTests {

  public static int binarySearch(final long[] array, final long target) {
    //assert array.length > 0;
    int indx = 0;
    int size = array.length - 1;
    while (indx <= size) {
      final int i = (indx + size) >> 1;
      final long value = array[i];
      if (value < target) {
        indx = i + 1;
      } else if (value > target) {
        size = i - 1;
      } else {
        return i;
      }
    }
    return -(indx + 1);
  }


  public static void doWork() {
    // setup an array to sort, simulating some real work. This should be added 
    // to our overhead calculation
    final int ARRAY_LENGTH = 500000;
    final Random generator = new Random();
    final long[] array = new long[ARRAY_LENGTH];
    for (int i = 0; i < array.length; array[i++] = generator.nextLong()) ;

    final long target = array[0];

    // sort the array
    Arrays.sort(array);

    binarySearch(array, target);
  }


  @Test
  public void testGetName() {
    TimingMaster subject = new TimingMaster("Bob");
    assertNotNull("Name should not be null", subject.getName());
    assertTrue("Bob".equals(subject.getName()), "Timer name did not match");
  }


  @Test
  public void testIsEnabled() {
    TimingMaster subject = new TimingMaster("testIsEnabled");
    assertTrue(subject.isEnabled(), "Timer should be enabled by default");
  }


  @Test
  public void testSetEnabled() {
    TimingMaster subject = new TimingMaster("testSetEnabled");

    Timer monitor = subject.createTimer();
    assertNotNull(monitor, "TimingMaster did not return a monitor");

    subject.setEnabled(false);
    assertFalse(subject.isEnabled(), "Timer should be disabled now");

    monitor = subject.createTimer();
    assertNotNull(monitor, "TimingMaster did not return a monitor when disabled");

    assertTrue((monitor instanceof NullTimer), "Returned monitor was not a NullTimer type");
  }


  @Test
  public void testCreateTimer() {
    TimingMaster subject = new TimingMaster("testCreateTimer");
    Timer monitor = subject.createTimer();
    assertNotNull(monitor, "TimingMaster did not return a monitor");
  }


  @Test
  public void testResetThis() {
    TimingMaster subject = new TimingMaster("testResetThis");
    subject.increase(5);
    subject.resetThis();
    assertTrue(subject.accrued == 0, "Accrued value was not reset");
  }


  @Test
  public void testIncrease() {
    TimingMaster subject = new TimingMaster("testIncrease");
    subject.increase(5);
    assertTrue(subject.accrued == 5, "Accrued value was not incremented");
  }


  /**
   *
   */
  @Test
  public void testGetCurrentActive() {
    TimingMaster subject = new TimingMaster("testGetCurrentActive");
    assertTrue(subject.getCurrentActive() == 0, "CurrentActive did not start out at zero");
    Timer m1 = subject.createTimer();
    assertTrue(subject.getCurrentActive() == 0, "CurrentActive did not remain at zero after creating monitor");
    Timer m2 = subject.createTimer();
    assertTrue(subject.getCurrentActive() == 0, "CurrentActive did not remain at zero after creating monitor");
    Timer m3 = subject.createTimer();
    assertTrue(subject.getCurrentActive() == 0, "CurrentActive did not remain at zero after creating monitor");

    m1.start();
    assertTrue(subject.getCurrentActive() == 1, "CurrentActive did not increment to 1 after starting monitor");
    m2.start();
    assertTrue(subject.getCurrentActive() == 2, "CurrentActive did not increment to 2 after starting monitor");
    m3.start();
    assertTrue(subject.getCurrentActive() == 3, "CurrentActive did not increment to 3 after starting monitor");

    m1.stop();
    assertTrue(subject.getCurrentActive() == 2, "CurrentActive did not decrement to 2 after stopping monitor 1");
    m2.stop();
    assertTrue(subject.getCurrentActive() == 1, "CurrentActive did not decrement to 1 after stopping monitor 2");
    m3.stop();
    assertTrue(subject.getCurrentActive() == 0, "CurrentActive did not decrement to 0 after stopping monitor 3");

  }


  @Test
  public void simpleTest() {
    TimingMaster subject = new TimingMaster("simpleTest");
    System.out.println(subject);
    Timer t1 = null;
    for (int i = 0; i < 10; i++) {
      t1 = subject.createTimer();
      t1.start();
      doWork();
      t1.stop();
    }
    System.out.println(subject);
  }


  @Disabled("Test poorly designed test, no way to quantify global activity especially in multi-threaded environments")
  public void testGetGloballyActive() {
    TimingMaster subject = new TimingMaster("testGetGloballyActive");
    TimingMaster otherMaster = new TimingMaster("OtherMasterTimer");
    assertTrue(subject.getGloballyActive() == 0, "GloballyActive started out at " + subject.getGloballyActive() + " not zero");

    Timer m1 = subject.createTimer();
    assertTrue(subject.getGloballyActive() == 0, "GloballyActive did not remain at zero after creating monitor");
    Timer m2 = otherMaster.createTimer();
    assertTrue(subject.getGloballyActive() == 0, "GloballyActive did not remain at zero after creating monitor");

    m1.start();
    assertTrue(subject.getGloballyActive() == 1, "GloballyActive did not increment to 1 after starting a monitor");
    m2.start();
    assertTrue(subject.getGloballyActive() == 2, "GloballyActive did not increment to 2 after starting a monitor");

    m1.stop();
    assertTrue(subject.getGloballyActive() == 1, "GloballyActive did not decrement to 1 after stopping monitor 1");
    m2.stop();
    assertTrue(subject.getGloballyActive() == 0, "GloballyActive did not decrement to 0 after stopping monitor 2");

  }

}
