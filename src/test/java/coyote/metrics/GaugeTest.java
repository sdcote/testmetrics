package coyote.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GaugeTest {


  @Test
  public void testIncrement() {
    String NAME = "testIncrement";
    long LIMIT = 10;
    Gauge counter = new Gauge(NAME);
    for (int x = 0; x < LIMIT; x++) {
      counter.increment();
    }
    assertTrue(counter.getMaxValue() == LIMIT, "MaxValue is " + counter.getMaxValue() + " and should be " + LIMIT);
    assertTrue(counter.getMinValue() == 0, "MinValue is " + counter.getMinValue() + " and should be 0");
  }


  @Test
  public void testConstructor() {
    String NAME = "test";
    Gauge counter = new Gauge(NAME);
    assertTrue(counter.getMaxValue() == 0, "MaxValue is wrong");
    assertTrue(counter.getMinValue() == 0, "MinValue is wrong");
  }


  @Test
  public void testReset() {
    String NAME = "testReset";
    long LIMIT = 10;
    Gauge counter = new Gauge(NAME);
    for (int x = 0; x < LIMIT; x++) {
      counter.increment();
    }
    Gauge delta = counter.reset();
    assertTrue(delta.getMaxValue() == LIMIT, "Delta MaxValue is " + delta.getMaxValue() + " and should be " + LIMIT);
    assertTrue(delta.getMinValue() == 0, "Delta MinValue is " + delta.getMinValue() + " and should be 0");
    assertTrue(counter.getMaxValue() == 0, "Gauge MaxValue is " + counter.getMaxValue() + " and should be 0");
    assertTrue(counter.getMinValue() == 0, "Gauge MinValue is " + counter.getMinValue() + " and should be 0");
  }

}
