package coyote.metrics;

/**
 * The Counter class models an object that tracks a numerical value which only increases over time.
 *
 * <p>This class is thread-safe in that all the methods synchronize on the name of the counter to avoid double
 * synchronization on the class itself.</p>
 */
public class Counter extends Metric implements Cloneable {
  protected String _units = null;
  protected long _value = 0;
  protected long _minValue = 0; // used in sub-classes
  protected long _maxValue = 0; // used in sub-classes


  /**
   * Create a counter with a name.
   */
  public Counter(final String name) {
    super(name);
  }


  /**
   * Create a deep copy of this counter.
   */
  @Override
  public Object clone() {
    final Gauge retval = new Gauge(_name);
    retval._units = _units;
    retval._value = _value;
    retval._minValue = _minValue;
    retval._maxValue = _maxValue;
    retval._updateCount = _updateCount;
    return retval;
  }


  /**
   * @return Returns the units the counter measures.
   */
  public String getUnits() {
    return _units;
  }

  /**
   * Sets the units the counter measures.
   *
   * @param units The units to set.
   */
  public void setUnits(final String units) {
    synchronized (_name) {
      _units = units;
    }
  }

  /**
   * @return Returns the current value of the counter.
   */
  public long getValue() {
    synchronized (_name) {
      return _value;
    }
  }

  /**
   * Increase the counter by the given amount.
   *
   * @param amt The amount to add to the counter.
   * @return The final value of the counter after the operation.
   */
  public long increase(final long amt) {
    synchronized (_name) {
      _updateCount++;
      _value += amt;
      if (_value < _minValue) {
        _minValue = _value;
      }
      if (_value > _maxValue) {
        _maxValue = _value;
      }
      return _value;
    }
  }

  /**
   * Increment the counter by one.
   *
   * @return The final value of the counter after the operation.
   */
  public long increment() {
    synchronized (_name) {
      _updateCount++;
      _value++;
      if (_value > _maxValue) {
        _maxValue = _value;
      }
      return _value;
    }
  }

  /**
   * Set the current, update count and Min/Max values to zero.
   *
   * <p>The return value will represent a copy of the counter prior to the reset and is useful for applications that
   * desire delta values. These delta values are simply the return values of successive reset calls.</p>
   *
   * @return a counter representing the state prior to the reset.
   */
  public Counter reset() {
    synchronized (_name) {
      final Counter retval = (Counter) clone();
      _value = 0;
      _minValue = 0;
      _maxValue = 0;
      _updateCount = 0;
      return retval;
    }
  }

  /**
   * Return the human-readable form of this counter.
   */
  @Override
  public String toString() {
    synchronized (_name) {
      final StringBuffer buff = new StringBuffer(_name);
      buff.append("=");
      buff.append(_value);
      if (_units != null) {
        buff.append(_units);
      }
      return buff.toString();
    }
  }

}
