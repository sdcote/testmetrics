package coyote.metrics;

/**
 * The Gauge class models an object that tracks a numerical value that can increase and decrease.
 *
 * <p>This class is thread-safe in that all the methods synchronize on the name of the gauge to avoid double
 * synchronization on the class itself.</p>
 */
public class Gauge extends Counter implements Cloneable {


  /**
   * Create a gauge with a name.
   */
  public Gauge(final String name) {
    super(name);
  }


  /**
   * Create a deep copy of this Gauge.
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
   * Decrease the gauge by the given amount.
   *
   * @param amt The amount to subtract from the gauge.
   * @return The final value of the gauge after the operation.
   */
  public long decrease(final long amt) {
    synchronized (_name) {
      _updateCount++;
      _value -= amt;
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
   * Decrement the gauge by one.
   *
   * @return The final value of the gauge after the operation.
   */
  public long decrement() {
    synchronized (_name) {
      _updateCount++;
      _value--;
      if (_value < _minValue) {
        _minValue = _value;
      }
      return _value;
    }
  }


  /**
   * @return Returns the maximum value the gauge ever represented.
   */
  public long getMaxValue() {
    synchronized (_name) {
      return _maxValue;
    }
  }


  /**
   * @return Returns the minimum value the gauge ever represented.
   */
  public long getMinValue() {
    synchronized (_name) {
      return _minValue;
    }
  }


  /**
   * Set the current, update count and Min/Max values to zero.
   *
   * <p>The return value will represent a copy of the gauge prior to the reset and is useful for applications that
   * desire delta values. These delta values are simply the return values of successive reset calls.</p>
   *
   * @return a gauge representing the state prior to the reset.
   */
  @Override
  public Gauge reset() {
    synchronized (_name) {
      final Gauge retval = (Gauge) clone();
      _value = 0;
      _minValue = 0;
      _maxValue = 0;
      _updateCount = 0;
      return retval;
    }
  }

  /**
   * Return the human-readable form of this gauge.
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
      buff.append("[min=");
      buff.append(_minValue);
      buff.append(":max=");
      buff.append(_maxValue);
      buff.append("]");

      return buff.toString();
    }
  }
}
