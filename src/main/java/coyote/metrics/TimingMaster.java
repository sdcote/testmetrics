package coyote.metrics;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


/**
 * The TimingMaster class models the master of all timers with a given name.
 *
 * <p>This class is used to summarize all the timers in its list.
 */
public class TimingMaster implements TimerMaster {
  public static final String CLASS_TAG = "Timer";
  public static final String NAME = "Name";
  static final String MILLISECONDS = "ms";
  static final String NONE = "";
  static final String TOTAL = "Total";
  static final String MIN = "Min Value";
  static final String MAX = "Max Value";
  static final String HITS = "Hits";
  static final String AVG = "Avg";
  static final String STANDARD_DEVIATION = "Std Dev";
  static final String ACTIVE = "Active";
  static final String AVGACTIVE = "Avg Active";
  static final String MAXACTIVE = "Max Active";
  static final String FIRSTACCESS = "First Access";
  static final String LASTACCESS = "Last Access";
  /**
   * The number of global timers currently active.
   */
  private static volatile long globalCounter;
  /**
   * Name-value pairs for labeling of metrics
   */
  protected Map<String, String> labels = new HashMap<>();

  protected String description = null;
  /**
   * The name of this master set of timers.
   */
  String _name = null;
  /**
   * Flag indicating we are currently running; the start() has been called
   */
  boolean running = true;
  long accrued;
  /**
   * Flag indicating if this timer is enabled
   */
  private volatile boolean _enabled = true;
  /**
   * The number of timers currently active.
   */
  private volatile long activeCounter;
  /**
   * Flag indicating whether or not to store the first accessed time
   */
  private boolean isFirstAccess = true;
  /**
   * Epoch time in milliseconds when this timer was first accessed
   */
  private long firstAccessTime;
  /**
   * Epoch time in milliseconds when this timer was last accessed
   */
  private long lastAccessTime;
  private long maxActive = 0;

  // -

  private long totalActive = 0;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private int hits;
  private long total;
  private long sumOfSquares;

  /**
   *
   */
  public TimingMaster(final String name) {
    _name = name;
  }

  /**
   * Convert a float value to a String
   *
   * @param value
   * @return
   */
  protected static String convertToString(final double value) {
    final DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance();
    numberFormat.applyPattern("#,###.#");
    return numberFormat.format(value);
  }

  /**
   * Method convertToString
   *
   * @param value
   * @return
   */
  protected String convertToString(final long value) {
    final DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance();
    numberFormat.applyPattern("#,###");
    return numberFormat.format(value);
  }


  /**
   * Create a new instance of a timer that will track times and other datum.
   *
   * @return A Timer that can be stopped at some time to generate datum.
   */
  public Timer createTimer() {
    Timer retval;
    if (_enabled) {
      retval = new TimingTimer(this);
      hits++;
    } else {
      retval = new NullTimer(this);
    }

    return retval;
  }


  /**
   * @return Returns the accrued datum for all stopped timers.
   */
  public long getAccrued() {
    return accrued;
  }


  /**
   * @return the average time for all stopped timers for this master list.
   */
  @Override
  public long getAverage() {
    final long closures = (hits - activeCounter);

    if (closures == 0) {
      return 0;
    } else {
      return total / closures;
    }
  }

  @Override
  public long getTotal() {
    return total;
  }


  /**
   * @return the average number of active for the life of this master list.
   */
  @Override
  public final float getAvgActive() {
    if (hits == 0) {
      return 0;
    } else {
      return (float) totalActive / hits;
    }
  }

  @Override
  public long getFirstAccessTime() {
    return firstAccessTime;
  }

  @Override
  public long getLastAccessTime() {
    return lastAccessTime;
  }

  @Override
  public void setDescription(String desc) {
    this.description = desc;
  }


  /**
   * Access the number of timers active for this timer master.
   *
   * @return Returns the number of timers currently active (started) for this
   * master timer.
   */
  public long getCurrentActive() {
    return activeCounter;
  }


  private String getDateString(final long time) {
    if (time == 0) {
      return "";
    } else {
      return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(new Date(time));
    }
  }


  /**
   * Method getDisplayString
   *
   * @param type
   * @param value
   * @param units
   * @return
   */
  protected String getDisplayString(final String type, final String value, final String units) {
    if (TimingMaster.NONE.equals(units)) {
      return type + "=" + value + ", ";
    } else {
      return type + "=" + value + " " + units + ", ";
    }
  }


  /**
   * Access the number of timers started in the runtime environment.
   *
   * @return Returns the number of timers currently active (started) for all
   * master timers.
   */
  public long getGloballyActive() {
    return TimingMaster.globalCounter;
  }


  /**
   * @return The name of this timer set.
   */
  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getDescription() {
    return description;
  }


  /**
   * Access the current standard deviation for all stopped timers using the
   * Sum of Squares algorithm.
   *
   * @return The amount of one standard deviation of all the interval times.
   */
  @Override
  public long getStandardDeviation() {
    long stdDeviation = 0;
    if (hits != 0) {
      final long sumOfX = total;
      final int n = hits;
      final int nMinus1 = (n <= 1) ? 1 : n - 1; // avoid 0 divides;

      final long numerator = sumOfSquares - ((sumOfX * sumOfX) / n);
      stdDeviation = (long) Math.sqrt(numerator / nMinus1);
    }

    return stdDeviation;
  }

  @Override
  public long getMinimum() {
    return min;
  }

  @Override
  public long getMaximum() {
    return max;
  }

  @Override
  public long getMaxActive() {
    return maxActive;
  }


  /**
   * Increase the time by the specified amount of milliseconds.
   *
   * <p>This is the method that keeps track of the various statistics being
   * tracked.
   *
   * @param value the amount to increase the accrued value.
   */
  @Override
  public synchronized void increase(final long value) {
    // calculate min
    if (value < min) {
      min = value;
    }

    // calculate max
    if (value > max) {
      max = value;
    }

    // total _accrued value
    accrued += value;

    // calculate total i.e. sumofX's
    total += value;

    sumOfSquares += value * value;
  }


  /**
   * @return True if the timer set is enabled, false otherwise.
   */
  public synchronized boolean isEnabled() {
    return _enabled;
  }

  /**
   * Enable or disable all the timers in this list.
   *
   * @param flag True to enable the timer, false to keep it from processing.
   */
  public synchronized void setEnabled(final boolean flag) {
    _enabled = flag;
  }

  /**
   * Reset all variables for this master timer instance.
   *
   * <p>The effect of this is to reset this objects variables to the state they
   * were in when the object was first created.
   */
  synchronized protected void resetThis() {
    min = Long.MAX_VALUE;
    max = Long.MIN_VALUE;
    total = accrued = sumOfSquares = maxActive = totalActive = hits = 0;
    firstAccessTime = lastAccessTime = System.currentTimeMillis();
  }

  /**
   * Start the timer in the context of this master timer.
   *
   * @param timr the timer to start.
   */
  @Override
  public synchronized void start(final Timer timr) {
    activeCounter++;
    TimingMaster.globalCounter++;

    if (activeCounter > maxActive) {
      maxActive = activeCounter;
    }

    totalActive += activeCounter;

    final long now = System.currentTimeMillis();
    lastAccessTime = now;

    if (isFirstAccess) {
      isFirstAccess = false;
      firstAccessTime = now;
    }
  }


  /**
   * Stop the timer in the context of this master timer.
   *
   * @param mon the timer to stop.
   */
  @Override
  public synchronized void stop(final Timer mon) {
    activeCounter--;
    TimingMaster.globalCounter--;
    accrued += mon.getAccrued();
  }

  @Override
  public int getHits() {
    return hits;
  }

  @Override
  public long getActiveCounter() {
    return activeCounter;
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
  public Labeled addLabel(String name, String value) {
    if (name != null) {
      if (value != null) {
        labels.put(name, value);
      } else {
        labels.remove(name);
      }
    }
    return this;
  }

  /**
   * Check to see if the metric contains a named label
   *
   * @param name the name of the label to cearch
   * @return true if a label with that name exists, false otherwise.
   */
  @Override
  public boolean hasLabel(String name) {
    if (name != null)
      return labels.containsKey(name);
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
  public String getLabelValue(String name) {
    String retval = null;
    if (name != null) retval = labels.get(name);
    return retval;
  }

  /**
   * @return a mutable list of label names.
   */
  @Override
  public List<String> labelNames() {
    List<String> retval = new ArrayList<>();
    for (String name : labels.keySet()) retval.add(name);
    return retval;
  }

  /**
   * @return a mutable map of name-value pairs representing the labels in this metric
   */
  @Override
  public Map<String, String> getLabels() {
    Map<String,String>retval = new HashMap<>();
    for( Map.Entry<String,String> entry: labels.entrySet()){
      retval.put(entry.getKey(),entry.getValue());
    }
    return retval;
  }

  /**
   * Method toString
   *
   * @return a string representing the timer.
   */
  @Override
  public String toString() {
    final StringBuffer message = new StringBuffer(_name);
    message.append(": ");
    message.append(getDisplayString(TimingMaster.HITS, convertToString(hits), TimingMaster.NONE));

    if ((hits - activeCounter) > 0) {
      message.append(getDisplayString(TimingMaster.AVG, convertToString(getAverage()), TimingMaster.MILLISECONDS));
      message.append(getDisplayString(TimingMaster.TOTAL, convertToString(total), TimingMaster.MILLISECONDS));
      message.append(getDisplayString(TimingMaster.STANDARD_DEVIATION, convertToString(getStandardDeviation()), TimingMaster.MILLISECONDS));
      message.append(getDisplayString(TimingMaster.MIN, convertToString(min), TimingMaster.MILLISECONDS));
      message.append(getDisplayString(TimingMaster.MAX, convertToString(max), TimingMaster.MILLISECONDS));
    }
    message.append(getDisplayString(TimingMaster.ACTIVE, convertToString(activeCounter), TimingMaster.NONE));
    message.append(getDisplayString(TimingMaster.MAXACTIVE, convertToString(maxActive), TimingMaster.NONE));
    message.append(getDisplayString(TimingMaster.AVGACTIVE, TimingMaster.convertToString(getAvgActive()), TimingMaster.NONE));
    message.append(getDisplayString(TimingMaster.FIRSTACCESS, getDateString(firstAccessTime), TimingMaster.NONE));
    message.append(getDisplayString(TimingMaster.LASTACCESS, getDateString(lastAccessTime), TimingMaster.NONE));
    message.setLength(message.length() - 2); // remove the last delimiter and space
    return message.toString();
  }


}
