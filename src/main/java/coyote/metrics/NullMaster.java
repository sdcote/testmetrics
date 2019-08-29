package coyote.metrics;

import java.util.List;
import java.util.Map;

/**
 * The NullMaster class models...
 */
public class NullMaster implements TimerMaster {

  /**
   * @return The name of this timer set.
   */
  @Override
  public String getName() {
    return "";
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void setDescription(String desc) {
  }

  @Override
  public long getValue() {
    return 0;
  }

  @Override
  public void increase(final long value) {
  }

  @Override
  public void start(final Timer mon) {
  }

  @Override
  public void stop(final Timer mon) {
  }

  @Override
  public int getHits() {
    return 0;
  }

  @Override
  public long getActiveCounter() {
    return 0;
  }

  @Override
  public long getAverage() {
    return 0;
  }

  @Override
  public long getTotal() {
    return 0;
  }

  @Override
  public long getStandardDeviation() {
    return 0;
  }

  @Override
  public long getMinimum() {
    return 0;
  }

  @Override
  public long getMaximum() {
    return 0;
  }

  @Override
  public long getMaxActive() {
    return 0;
  }

  @Override
  public float getAvgActive() {
    return 0;
  }

  @Override
  public long getFirstAccessTime() {
    return 0;
  }

  @Override
  public long getLastAccessTime() {
    return 0;
  }

  @Override
  public Labeled addLabel(String name, String value) {
    return null;
  }

  @Override
  public boolean hasLabel(String name) {
    return false;
  }

  @Override
  public String getLabelValue(String name) {
    return null;
  }

  @Override
  public List<String> labelNames() {
    return null;
  }

  @Override
  public Map<String, String> getLabels() {
    return null;
  }
}
