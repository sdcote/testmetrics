package coyote.metrics;

public interface Monitor extends Labeled {
  String getName();
  String getDescription();
  long getValue();
}
