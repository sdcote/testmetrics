package coyote.metrics;

/**
 * Timers are devices for measuring the time something takes and the Timer interface models a contract for all timers.
 *
 * <p>Timers measure the time between phases of execution. Once a Timer is started, its stores the time and waits for a
 * call to stop(). Once stopped, it calculates the total elapsed time for that run.</p>
 *
 * <p>Later a Timer (actually the entire set) can be rolled-up to provide the number of invocations, mean, long and
 * short elapsed intervals and several other phase-oriented metrics.</p>
 *
 * <p>There are two types of Timers, a Timing Timer and a Null Timer. During normal operation the ScoreCard issues a
 * Timing Timer that tracks the time between its start and stop methods are called finally placing the results in its
 * Master Timer. If monitoring has been disabled for either the entire ScoreCard or for a specific named Timer, then a
 * Null Timer is issued. It implements the exact same interface as the Timing Timer, but the Null Timer contains no
 * logic thereby saving on processing when monitoring is not desired.</p>
 *
 * <p>A single Timer reference can be started and stopped several times, each interval between the start-stop calls
 * being added to the accrued value of the Timer.</p>
 */
public interface Timer extends Labeled {

  /**
   * Access the datum collected.
   *
   * @return The value of the datum collected.
   */
  long getAccrued();


  /**
   * Access the master timer that tracks data for all timers in the named set.
   *
   * @return The master for this timer.
   */
  TimerMaster getMaster();


  /**
   * Access the name of the timer.
   *
   * @return Name of the timer instance.
   */
  String getName();


  /**
   * @return True if the time has been started, false if stopped.
   */
  boolean isRunning();


  /**
   * Start this timer collecting datum.
   */
  void start();


  /**
   * Stop this timer from collecting datum.
   */
  void stop();


}
