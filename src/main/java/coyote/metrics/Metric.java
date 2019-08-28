package coyote.metrics;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Metric class models a basic metric.
 */
public class Metric implements Labeled {
  protected String _name;
  protected String _description = null;
  protected long _updateCount = 0;
  protected Map<String, String> _labels = new HashMap<>();


  /**
   *
   */
  public Metric(final String name) {
    _name = name;
  }


  /**
   * @return The currently set name of this object.
   */
  public String getName() {
    return _name;
  }

  /**
   * Included for balance but it should not be used by the uninitiated.
   *
   * @param name The new name to set.
   */
  void setName(final String name) {
    _name = name;
  }

  /**
   * @return The number of times the value was updated.
   */
  public long getUpdateCount() {
    return _updateCount;
  }

  /**
   * @return the description of this metric, may be null.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * @param description the description of this metric.
   */
  public void setDescription(String description) {
    this._description = description;
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
        _labels.put(name, value);
      } else {
        _labels.remove(name);
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
      return _labels.containsKey(name);
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
    if (name != null) retval = _labels.get(name);
    return retval;
  }

  /**
   * @return a mutable list of label names.
   */
  @Override
  public List<String> labelNames() {
    List<String> retval = new ArrayList<>();
    for (String name : _labels.keySet()) retval.add(name);
    return retval;
  }

  /**
   * @return a mutable map of name-value pairs representing the labels in this metric
   */
  @Override
  public Map<String, String> getLabels() {
    Map<String,String>retval = new HashMap<>();
    for( Map.Entry<String,String> entry: _labels.entrySet()){
      retval.put(entry.getKey(),entry.getValue());
    }
    return retval;
  }

}