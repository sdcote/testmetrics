package coyote.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TimerBase class models the base class for all timers.
 */
abstract class TimerBase implements Timer {
  /**
   * Our master timer used to correlate all timer metrics
   */
  protected static final TimerMaster NULL_MASTER = new NullMaster();

  /**
   * The master timer that accumulates our data.
   */
  protected TimerMaster _master = null;

  /**
   * Flag indicating if we have been started or not.
   */
  volatile boolean _isRunningFlag = false;

  /** The labels for this timer. */
  protected Map<String, String> _labels = new HashMap<>();


  /**
   *
   */
  public TimerBase(final TimerMaster master) {
    super();
    _master = master;
  }


  @Override
  public long getAccrued() {
    return 0;
  }


  @Override
  public TimerMaster getMaster() {
    return _master;
  }


  @Override
  public String getName() {
    return _master.getName();
  }


  @Override
  public boolean isRunning() {
    return _isRunningFlag;
  }


  @Override
  public void start() {
  }


  @Override
  public void stop() {
  }


  /**
   * Return the string representation of the timer.
   */
  @Override
  public String toString() {
    // TODO: add labels
    if (_master != null) {
      return _master.toString();
    }
    return "";
  }


  /**
   * Add the given name-value pair to the list of labels for this metric.
   *
   * <p>If the name is null the value will not be added. If the value is null the existing value with that name will be
   * removed.</p>
   *
   * @param name  name of the value to place
   * @param value the value to map to the name
   */
  @Override
  public Timer addLabel(String name, String value) {
    if (name != null) {
      if (value != null) {
        _labels.put(name, value);
      } else {
        _labels.remove(name);
      }
    }
    return this;
  }


  /**
   * Check to see if the timer contains a named label
   *
   * @param name the name of the label to search
   * @return true if a label with that name exists, false otherwise.
   */
  @Override
  public boolean hasLabel(String name) {
    if (name != null)
      return _labels.containsKey(name);
    else
      return false;
  }


  /**
   * Return the value of the label with the given name
   *
   * @param name the name of the label to retrieve
   * @return the value of the named label or null if the named value does ot exist.
   */
  @Override
  public String getLabel(String name) {
    String retval = null;
    if (name != null) retval = _labels.get(name);
    return retval;
  }


  /**
   * @return a mutable list of label names.
   */
  @Override
  public List<String> labelNames() {
    List<String> retval = new ArrayList<>();
    for (String name : _labels.keySet()) retval.add(name);
    return retval;
  }

}
