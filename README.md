# TestMetrics
A set of drop-in classes for testing projects which want to collect performance metrics.

The goal is to have a small namespace of classes independent of other classes and libraries which testers can drop into their test projects to collect and record performance metrics. 

For example, a Cucumber/Selenium test suite can register before/after scenario hooks to start and stop timers for each scenario. After all the tests are run, the `ScoreCard` can be used to save performance metrics for each of the tests. The results can be tracked over time to see if performance improves or degrades.

The metrics contain labels (name-value pairs) which can be used to record data about the metric, such as what server on which they were run and in what envirnment. When recorded with these labels, the tester can generate a history of performance metrics.

There is a Metrics formatter which can be used to format counters, gauges and timers into a variety of formats.

This should enable the tester to populate time series databases like Prometheus with test performance data and track performance of code over time.

