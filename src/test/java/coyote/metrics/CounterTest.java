package coyote.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }


  @Test
  public void testIncrement() {
    String NAME = "testIncrement";
    long LIMIT = 10;
    Counter counter = new Counter(NAME);
    for (int x = 0; x < LIMIT; x++) {
      counter.increment();
    }
    assertTrue(counter.getValue() == LIMIT, "Value is " + counter.getValue() + " and should be " + LIMIT);
    assertTrue(counter.getUpdateCount() == LIMIT, "UpdateCount is " + counter.getUpdateCount() + " and should be " + LIMIT);
  }


  @Test
  public void testConstructor() {
    String NAME = "test";
    Counter counter = new Counter(NAME);
    assertTrue(counter.getName().equals(NAME), "Name is wrong");
    assertTrue(counter.getValue() == 0, "Value is wrong");
    assertTrue(counter.getUnits() == null, "Units is wrong");
    assertTrue(counter.getUpdateCount() == 0, "UpdateCount is wrong");
  }


  @Test
  public void testReset() {
    String NAME = "testReset";
    long LIMIT = 10;
    Counter counter = new Counter(NAME);
    for (int x = 0; x < LIMIT; x++) {
      counter.increment();
    }
    Counter delta = counter.reset();
    assertTrue(delta.getName().equals(NAME), "Delta Name is " + delta.getName() + " and should be " + NAME);
    assertTrue(delta.getValue() == LIMIT, "Delta Value is " + delta.getValue() + " and should be " + LIMIT);
    assertTrue(delta.getUnits() == null, "Delta Units are " + delta.getUnits() + " and should be null");
    assertTrue(delta.getUpdateCount() == LIMIT, "Delta UpdateCount is " + delta.getUpdateCount() + " and should be " + LIMIT);
    assertTrue(counter.getName().equals(NAME), "Counter Name is " + counter.getName() + " and should be " + NAME);
    assertTrue(counter.getValue() == 0, "Counter Value is " + counter.getValue() + " and should be 0");
    assertTrue(counter.getUnits() == null, "Counter Units are " + counter.getUnits() + " and should be null");
    assertTrue(counter.getUpdateCount() == 0, "Counter UpdateCount is " + counter.getUpdateCount() + " and should be 0");
  }


  @Test
  public void tags() {
    Counter counter = new Counter("test");
    assertFalse(counter.hasLabel(""), "Empty counter should not contain any labels");
    counter.addLabel("bob", "value");
    assertNotNull(counter.getLabelValue("bob"), "Missing value for requested label name");
    assertEquals("value", counter.getLabelValue("bob"), "Incorrect label value retrieved");
    assertFalse(counter.hasLabel("alice"), "Counter should not contain requested label name");
    assertTrue(counter.hasLabel("bob"), "Counter should contain requested tag");
    assertFalse(counter.hasLabel("Bob"), "Counter should not contain requested tag");
  }

}
