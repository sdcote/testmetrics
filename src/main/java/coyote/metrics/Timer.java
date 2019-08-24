package coyote.metrics;

import java.util.List;

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
public interface Timer {

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


  /**
   * Add the given name-value pair to the list of labels for this metric.
   *
   * <p>If the name is null the value will not be added. If the value is null the existing value with that name will be
   * removed.</p>
   *
   * @param name  name of the value to place
   * @param value the value to map to the name
   */
  Timer addLabel(String name, String value);


  /**
   * Check to see if the timer contains a named label
   *
   * @param name the name of the label to search
   * @return true if a label with that name exists, false otherwise.
   */
  boolean hasLabel(String name);


  /**
   * Return the value of the label with the given name
   *
   * @param name the name of the label to retrieve
   * @return the value of the named label or null if the named value does ot exist.
   */
  String getLabel(String name);


  /**
   * @return a mutable list of label names.
   */
  List<String> labelNames();

}
