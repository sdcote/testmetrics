package coyote.metrics;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;

public class ScoreCard {

  /**
   * Map of counters by their name
   */
  private static final HashMap<String, Counter> counters = new HashMap<>();

  /**
   * Map of gauges by their name
   */
  private static final HashMap<String, Gauge> gauges = new HashMap<>();

  /**
   * Re-usable null timer to save object creation and GC'n
   */
  private static final Timer NULL_TIMER = new NullTimer(null);
  /**
   * Map of master timers by their name
   */
  private static final HashMap<String, TimingMaster> masterTimers = new HashMap<String, TimingMaster>();
  private static InetAddress localAddress = null;
  private static String cachedLocalHostName = null;
  private static String BOARDID = UUID.randomUUID().toString().toLowerCase();
  /**
   * The time this scorecard was create/started.
   */
  private static long startedTimestamp = 0;
  /**
   * Timing is disabled by default
   */
  private static volatile boolean timingEnabled = true;

  static {
    startedTimestamp = System.currentTimeMillis();
  }


  /**
   * No instances
   */
  private ScoreCard() {
  }


  /**
   * Returns an {@code InetAddress} object encapsulating what is most likely
   * the machine's LAN IP address.
   *
   * <p>This method is intended for use as a replacement of JDK method {@code
   * InetAddress.getLocalHost}, because that method is ambiguous on Linux
   * systems. Linux systems enumerate the loopback network interface the same
   * way as regular LAN network interfaces, but the JDK {@code
   * InetAddress.getLocalHost} method does not specify the algorithm used to
   * select the address returned under such circumstances, and will often
   * return the loopback address, which is not valid for network
   * communication. Details
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
   *
   * <p>This method will scan all IP addresses on all network interfaces on
   * the host machine to determine the IP address most likely to be the
   * machine's LAN address. If the machine has multiple IP addresses, this
   * method will prefer a site-local IP address (e.g. 192.168.x.x or 10.10.x.x,
   * usually IPv4) if the machine has one (and will return the first site-local
   * address if the machine has more than one), but if the machine does not
   * hold a site-local address, this method will return simply the first non-
   * loopback address found (IPv4 or IPv6).
   *
   * <p>Last ditch effort is to try a DNS lookup of the machines hostname.
   * This can take a little time which it is the last resort and any results
   * are cached to avoid any future DNS lookups.
   *
   * <p>If the above methods cannot find a non-loopback address using this
   * selection algorithm, it will fall back to calling and returning the
   * result of JDK method {@code InetAddress.getLocalHost}.
   */
  private static InetAddress getLocalAddress() {
    if (localAddress != null) {
      return localAddress;
    }

    try {
      InetAddress candidateAddress = null;
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
        NetworkInterface iface = ifaces.nextElement();
        for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
          InetAddress inetAddr = inetAddrs.nextElement();
          if (!inetAddr.isLoopbackAddress()) {
            if (inetAddr.isSiteLocalAddress()) {
              // Found non-loopback site-local address. Cache and return it
              localAddress = inetAddr;
              return localAddress;
            } else if (candidateAddress == null) {
              candidateAddress = inetAddr;
            }
          }
        }
      }
      if (candidateAddress != null) {
        localAddress = candidateAddress;
        return localAddress;
      }

      // Make sure we get the IP Address by which the rest of the world knows
      // us or at least, our host's default network interface
      InetAddress inetAddr;
      try {
        // This helps insure that we do not get localhost (127.0.0.1)
        inetAddr = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
        if (!inetAddr.isLoopbackAddress() && inetAddr.isSiteLocalAddress()) {
          localAddress = inetAddr;
          return localAddress;
        }
      } catch (UnknownHostException e) {
      }

      // Fall back to returning whatever InetAddress.getLocalHost() returns...
      inetAddr = InetAddress.getLocalHost();
      if (inetAddr == null) {
        throw new UnknownHostException();
      }
      localAddress = inetAddr;
      return localAddress;
    } catch (Exception e) {
    }
    //failure. Well, at least we tried.
    return null;
  }

  /**
   * Return the identifier the card is using to differentiate itself from other
   * cards on this host and the system overall.
   *
   * @return The identifier for this scorecard.
   */
  public static String getId() {
    return BOARDID;
  }

  /**
   * Assign a unique identifier to this scorecard.
   *
   * @param id the unique identifier to set
   */
  public static void setId(final String id) {
    BOARDID = id;
  }

  /**
   * @return The epoch time in milliseconds this scorecard was started.
   */
  public static long getStartedTime() {
    return startedTimestamp;
  }


  /**
   * Get an iterator over all the Master Timers in the scorecard.
   */
  public static Iterator<TimingMaster> getTimerIterator() {
    final ArrayList<TimingMaster> list = new ArrayList<TimingMaster>();
    synchronized (masterTimers) {
      for (final Iterator<TimingMaster> it = masterTimers.values().iterator(); it.hasNext(); list.add(it.next())) {
      }
    }
    return list.iterator();
  }

  /**
   * Get the master timer with the given name.
   *
   * @param name The name of the master timer to retrieve.
   * @return The master timer with the given name or null if that timer
   * does not exist.
   */
  public static TimingMaster getTimerMaster(final String name) {
    synchronized (masterTimers) {
      return masterTimers.get(name);
    }
  }

  /**
   * Return how long the scorecard has been active in a format using only the
   * significant time measurements.
   *
   * <p>Significant measurements means if the number of seconds extend past 24
   * hours, then only report the days and hours skipping the minutes and
   * seconds. Examples include <tt>4m 23s</tt> or <tt>22d 4h</tt>. The format
   * is designed to make reporting scorecard up-time more polished.
   *
   * @return the time the scorecard has been active in a reportable format.
   */
  public static String getUptimeString() {
    return formatSignificantElapsedTime((System.currentTimeMillis() - startedTimestamp) / 1000);
  }

  /**
   * Print only the most significant portion of the time.
   *
   * <p>This is the two most significant units of time. Form will be something
   * like "3h 26m" indicating 3 hours 26 minutes and some insignificant number
   * of seconds. Formats are Xd Xh (days-hours), Xh Xm (Hours-minutes), Xm Xs
   * (minutes-seconds) and Xs (seconds).</p>
   *
   * @param seconds number of elapsed seconds NOT milliseconds.
   * @return formatted string
   */
  private static String formatSignificantElapsedTime(long seconds) {
    final long days = seconds / 86400;
    final StringBuffer buffer = new StringBuffer();
    if (days > 0) // Display days and hours
    {
      buffer.append(days);
      buffer.append("d ");
      buffer.append(((seconds / 3600) % 24)); // hours
      buffer.append("h");
      return buffer.toString();
    }
    final int hours = (int) ((seconds / 3600) % 24);
    if (hours > 0) // Display hours and minutes
    {
      buffer.append(hours);
      buffer.append("h ");
      buffer.append(((seconds / 60) % 60)); // minutes
      buffer.append("m");
      return buffer.toString();
    }
    final int minutes = (int) ((seconds / 60) % 60);
    if (minutes > 0) // Display minutes and seconds
    {
      buffer.append(minutes);
      buffer.append("m ");
      buffer.append((seconds % 60)); // seconds
      buffer.append("s");
      return buffer.toString();
    }
    final int secs = (int) (seconds % 60);
    buffer.append(secs); // seconds
    buffer.append("s");
    return buffer.toString();
  }

  /**
   * @return the FQDN of the local host or null if the lookup failed for any
   * reason.
   */
  public static String getLocalQualifiedHostName() {
    // Use the cached version of the hostname to save DNS lookups
    if (cachedLocalHostName != null) {
      return cachedLocalHostName;
    }

    cachedLocalHostName = getQualifiedHostName(getLocalAddress());

    return cachedLocalHostName;
  }

  /**
   * Use the underlying getCanonicalHostName as used in Java, but return null
   * if the value is the numerical address (dotted-quad) representation of the
   * address.
   *
   * @param addr The IP address to lookup.
   * @return The Canonical Host Name; null if the FQDN could not be determined
   * or if the return value was the dotted-quad representation of the
   * host address.
   */
  public static String getQualifiedHostName(InetAddress addr) {
    String name = null;

    try {
      name = addr.getCanonicalHostName();

      if (name != null) {
        // Check for a return value of and address instead of a name
        if (Character.isDigit(name.charAt(0))) {
          // Looks like an address, return null;
          return null;
        }

        // normalize the case
        name = name.toLowerCase();
      }
    } catch (Exception ex) {
    }

    return name;
  }


  /**
   * Start a timer with the given name.
   *
   * <p>Use the returned Timer to stop the interval measurement.
   *
   * @param name The name of the timer instance to start.
   * @return The timer instance that should be stopped when the interval is
   * completed.
   */
  public static Timer startTimer(final String name) {
    Timer retval = null;
    if (timingEnabled) {
      synchronized (masterTimers) {
        TimingMaster master = masterTimers.get(name);
        if (master == null) {
          master = new TimingMaster(name);
          masterTimers.put(name, master);
        }
        retval = master.createTimer();
        retval.start();
      }
    } else {
      retval = NULL_TIMER;
    }
    return retval;
  }

  /**
   * Stop a timer with the given name.
   *
   * @param name The name of the timer instance to start.
   * @return The timer instance that was stopped.
   */
  public static Timer stopTimer(final String name) {
    Timer retval = null;
    if (timingEnabled) {
      synchronized (masterTimers) {
        TimingMaster master = masterTimers.get(name);
        if (master == null) {
          master = new TimingMaster(name);
          masterTimers.put(name, master);
        }
        retval = master.createTimer();
        retval.stop();
      }
    } else {
      retval = NULL_TIMER;
    }
    return retval;
  }


  /**
   * Disable the timer with the given name.
   *
   * <p>Disabling a timer will cause all new timers with the given name to
   * skip processing reducing the amount of processing performed by the
   * timers without losing the existing data in the timer. Any existing
   * timers will continue to accumulate data.
   *
   * <p>If a timer is disabled that has not already been created, a disabled
   * timer will be created in memory that can be enabled at a later time.
   *
   * @param name The name of the timer to disable.
   */
  public static void disableTimer(final String name) {
    synchronized (masterTimers) {
      TimingMaster master = masterTimers.get(name);
      if (master == null) {
        master = new TimingMaster(name);
        masterTimers.put(name, master);
      }
      master.setEnabled(false);
    }
  }

  /**
   * Enable the timer with the given name.
   *
   * <p>If a timer is enabled that has not already been created, a new
   * timer will be created in memory.
   *
   * @param name The name of the timer to enable.
   */
  public static void enableTimer(final String name) {
    synchronized (masterTimers) {
      // get an existing master timer or create a new one
      TimingMaster master = masterTimers.get(name);
      if (master == null) {
        master = new TimingMaster(name);
        masterTimers.put(name, master);
      }
      master.setEnabled(true);
    }
  }

  /**
   * Enable fully-functional timers from this point forward.
   *
   * <p>When timing is enabled, functional timers are returned and their metrics are collected for later reporting.</p>
   */
  public static void enableTiming() {
    synchronized (masterTimers) {
      timingEnabled = true;
    }
  }


  /**
   * Disable timers from this point forward.
   *
   * <p>When timing is disabled, null timers are be returned each time a timer is requested. This keeps all code
   * operational regardless of the runtime status of timing.
   */
  public static void disableTiming() {
    synchronized (masterTimers) {
      timingEnabled = false;
    }
  }

  /**
   * Return the counter with the given name.
   *
   * <p>If the counter does not exist, one will be created and added to the
   * static list of counters for later retrieval.
   *
   * @param name The name of the counter to return.
   * @return The counter with the given name.
   */
  public static Counter getCounter(final String name) {
    Counter counter = null;
    if (name != null) {
      synchronized (counters) {
        counter = counters.get(name);
        if (counter == null) {
          counter = new Counter(name);
          counters.put(name, counter);
        }
      }
    }
    return counter;
  }

  /**
   * @return The number of counters in the scorecard at the present time.
   */
  public static int getCounterCount() {
    return counters.size();
  }

  /**
   * Remove the counter with the given name.
   *
   * @param name Name of the counter to remove.
   * @return The removed counter.
   */
  public static Counter removeCounter(final String name) {
    Counter retval = null;
    if (name != null) {
      synchronized (counters) {
        retval = counters.remove(name);
      }
    }
    return retval;
  }

  /**
   * Access an iterator over the counters.
   *
   * <p>NOTE: this iterator is detached from the counters in that the remove() call on the iterator will only affect
   * the returned iterator and not the counter collection in the scorecard. If you wish to remove a counter, you
   * MUST call removeCounter(Counter) with the reference returned from this iterator as well.</p>
   *
   * @return a detached iterator over the counters.
   */
  public static Iterator<Counter> getCounterIterator() {
    final ArrayList<Counter> list = new ArrayList<Counter>();
    for (final Iterator<Counter> it = counters.values().iterator(); it.hasNext(); list.add(it.next())) {
    }
    return list.iterator();
  }

  /**
   * Access an iterator over the gauges.
   *
   * <p>NOTE: this iterator is detached from the gauges in that the remove() call on the iterator will only affect
   * the returned iterator and not the gauge collection in the scorecard. If you wish to remove a gauge, you
   * MUST call removeGauge(Gauge) with the reference returned from this iterator as well.</p>
   *
   * @return a detached iterator over the gauges.
   */
  public static Iterator<Gauge> getGaugeIterator() {
    final ArrayList<Gauge> list = new ArrayList<>();
    for (final Iterator<Gauge> it = gauges.values().iterator(); it.hasNext(); list.add(it.next())) {
    }
    return list.iterator();
  }

  /**
   * Remove the gauge with the given name.
   *
   * @param name Name of the gauge to remove.
   * @return The removed gauge.
   */
  public static Gauge removeGauge(final String name) {
    Gauge retval = null;
    if (name != null) {
      synchronized (gauges) {
        retval = gauges.remove(name);
      }
    }
    return retval;
  }

  /**
   * Return the counter with the given name.
   *
   * <p>If the counter does not exist, one will be created and added to the
   * static list of counters for later retrieval.
   *
   * @param name The name of the counter to return.
   * @return The counter with the given name.
   */
  public static Gauge getGauge(final String name) {
    Gauge gauge = null;
    if (name != null) {
      synchronized (gauges) {
        gauge = gauges.get(name);
        if (gauge == null) {
          gauge = new Gauge(name);
          gauges.put(name, gauge);
        }
      }
    }
    return gauge;
  }

  /**
   * @return The number of counters in the scorecard at the present time.
   */
  public static int getGaugeCount() {
    return gauges.size();
  }


  /**
   * Reset the counter with the given name returning a copy of the counter before the reset occurred.
   *
   * <p>The return value will represent a copy of the counter prior to the reset and is useful for applications that
   * desire delta values. These delta values are simply the return values of successive reset calls.</p>
   *
   * <p>If the counter does not exist, it will be created prior to being reset. The return value will reflect an empty
   * counter with the given name.</p>
   *
   * @param name The name of the counter to reset.
   * @return a counter containing the values of the counter prior to the reset.
   */
  public static Counter resetCounter(final String name) {
    Counter retval = null;
    if (name != null) {
      synchronized (counters) {
        retval = getCounter(name).reset();
      }
    }
    return retval;
  }

  /**
   * Reset the gauge with the given name returning a copy of the gauge before the reset occurred.
   *
   * <p>The return value will represent a copy of the gauge prior to the reset and is useful for applications that
   * desire delta values. These delta values are simply the return values of successive reset calls.</p>
   *
   * <p>If the gauge does not exist, it will be created prior to being reset. The return value will reflect an empty
   * gauge with the given name.</p>
   *
   * @param name The name of the gauge to reset.
   * @return a gauge containing the values of the counter prior to the reset.
   */
  public static Gauge resetGauge(final String name) {
    Gauge retval = null;
    if (name != null) {
      synchronized (gauges) {
        retval = getGauge(name).reset();
      }
    }
    return retval;
  }

  public static String getHostname() {
    return getLocalQualifiedHostName();
  }


  public static InetAddress getHostIpAddress() {
    return getLocalAddress();
  }

  /**
   * Increment the value of the counter with the given name.
   *
   * <p>This method retrieves the counter with the given name or creates one by that name if it does not yet exist. The
   * retrieved counter is then increased by one (1).
   *
   * @param name The name of the counter to increment.
   * @return The final value of the counter after the operation.
   */
  public static long incrementCounter(final String name) {
    return getCounter(name).increment();
  }


  /**
   * Increase the value of the counter with the given name by the given amount.
   *
   * <p>This method retrieves the counter with the given name or creates one by that name if it does not yet exist. The
   * retrieved counter is then increased by the given amount.</p>
   *
   * @param name The name of the counter to increase.
   * @return The final value of the counter after the operation.
   */
  public static long increaseCounter(final String name, final long value) {
    return getCounter(name).increase(value);
  }


  /**
   * Increment the value of the gauge with the given name.
   *
   * <p>This method retrieves the gauge with the given name or creates one by that name if it does not yet exist. The
   * retrieved gauged is then increased by one (1).
   *
   * @param name The name of the gauge to increment.
   * @return The final value of the gauge after the operation.
   */
  public static long incrementGauge(final String name) {
    return getGauge(name).increment();
  }

  /**
   * Increase the value of the gauge with the given name by the given amount.
   *
   * <p>This method retrieves the gauge with the given name or creates one by that name if it does not yet exist. The
   * retrieved gauge is then increased by the given amount.</p>
   *
   * @param name The name of the gauge to increase.
   * @return The final value of the gauge after the operation.
   */
  public static long increaseGauge(final String name, final long value) {
    return getGauge(name).increase(value);
  }

  /**
   * Decrement the value of the gauge with the given name.
   *
   * <p>This method retrieves the gauge with the given name or creates one by that name if it does not yet exist. The
   * retrieved gauged is then decreased by one (1).
   *
   * @param name The name of the gauge to decrement.
   * @return The final value of the gauge after the operation.
   */
  public static long decrementGauge(final String name) {
    return getGauge(name).increment();
  }


  /**
   * Decrease the value of the gauge with the given name by the given amount.
   *
   * <p>This method retrieves the gauge with the given name or creates one by that name if it does not yet exist. The
   * retrieved gauge is then decreased by the given amount.
   *
   * @param name The name of the gauge to decrease.
   * @return The final value of the gauge after the operation.
   */
  public static long decreaseGauge(final String name, final long value) {
    return getGauge(name).decrease(value);
  }


}
