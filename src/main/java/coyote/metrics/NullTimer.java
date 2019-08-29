package coyote.metrics;

/**
 * Creates a do-nothing timer to facilitate the disabling of timers while not affecting the compiled code of any
 * callers.
 *
 * <p>When a NullTimer is returned, performs no logic when it is stopped and therefore allows for very fast operation
 * when the timer is disabled.
 *
 * <p>See Martin Fowler's refactoring book for details on using Null Objects in software.
 */
public class NullTimer extends TimerBase {

  /**
   * Create a new timer with a null master.
   */
  public NullTimer() {
    super(TimerBase.NULL_MASTER);
  }


  /**
   * @param master the master timer for this timer...not used
   */
  public NullTimer(final TimingMaster master) {
    super(master);
  }

  @Override
  public Timer setDescription(String desc) { return this; }

}
