package coyote.metrics;

import java.text.DateFormat;
import java.util.Date;

public class MetricFormatter {

  public static String toJson(Timer timer) {
    StringBuilder sb = new StringBuilder("{");
    TimerMaster master = timer.getMaster();
    if (master != null) {
      sb.append(getField(TimingMaster.NAME, master.getName()));
      sb.append(',');
      sb.append(getField(TimingMaster.HITS, Integer.toString(master.getHits())));
      sb.append(',');

      if ((master.getHits() - master.getActiveCounter()) > 0) {
        sb.append(getField(TimingMaster.AVG, Long.toString(master.getAverage())));
        sb.append(',');
        sb.append(getField(TimingMaster.TOTAL, Long.toString(master.getTotal())));
        sb.append(',');
        sb.append(getField(TimingMaster.STANDARD_DEVIATION, Long.toString(master.getStandardDeviation())));
        sb.append(',');
        sb.append(getField(TimingMaster.MIN, Long.toString(master.getMinimum())));
        sb.append(',');
        sb.append(getField(TimingMaster.MAX, Long.toString(master.getMaximum())));
        sb.append(',');
      }
      sb.append(getField(TimingMaster.ACTIVE, Long.toString(master.getActiveCounter())));
      sb.append(',');
      sb.append(getField(TimingMaster.MAXACTIVE, Long.toString(master.getMaxActive())));
      sb.append(',');
      sb.append(getField(TimingMaster.AVGACTIVE, Float.toString(master.getAvgActive())));
      sb.append(',');
      sb.append(getField(TimingMaster.FIRSTACCESS, getDateString(master.getFirstAccessTime())));
      sb.append(',');
      sb.append(getField(TimingMaster.LASTACCESS, getDateString(master.getLastAccessTime())));
    }
    sb.append("}");
    return sb.toString();
  }

  private static String getDateString(final long time) {
    if (time == 0) {
      return "";
    } else {
      return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(new Date(time));
    }
  }

  private static String getField(String name, String value) {
    return getQuoted(name).concat(":").concat(getQuoted(value));
  }

  private static String getQuoted(String value) {
    return "\"" + value + "\"";
  }


}
