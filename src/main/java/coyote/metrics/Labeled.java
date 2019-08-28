package coyote.metrics;

import java.util.List;
import java.util.Map;

public interface Labeled {


  /**
   * Add the given name-value pair to the list of labels for this metric.
   *
   * <p>If the name is null the value will not be added. If the value is null the existing value with that name will be
   * removed.</p>
   *
   * @param name  name of the value to place
   * @param value the value to map to the name
   */
  Labeled addLabel(String name, String value);

  /**
   * Check to see if the metric contains a named label
   *
   * @param name the name of the label to cearch
   * @return true if a label with that name exists, false otherwise.
   */
  boolean hasLabel(String name);

  /**
   * Return the value of the label with the given name
   *
   * @param name the name of the label to retrieve
   * @return the value of the named label or null if the named value does ot exist.
   */
  String getLabelValue(String name);

  /**
   * @return a mutable list of label names.
   */
  List<String> labelNames();

  /**
   * @return a mutable map of name-value pairs
   */
  Map<String, String> getLabels();


}
