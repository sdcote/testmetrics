package coyote.metrics;

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
    if (_master != null) {
      return _master.toString();
    }
    return "";
  }

}
