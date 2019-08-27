package coyote.metrics;

/**
 * The TimerMaster class models the master of all timers with a given name.
 */
public interface TimerMaster extends Labeled {
  /**
   * @return The name of this timer set.
   */
  String getName();


  void increase(long value);


  void start(Timer mon);


  void stop(Timer mon);


  int getHits();


  long getActiveCounter();


  long getAverage();


  long getTotal();


  long getStandardDeviation();


  long getMinimum();


  long getMaximum();


  long getMaxActive();


  float getAvgActive();


  long getFirstAccessTime();


  long getLastAccessTime();

}
