# Architecture: PerfSage SLO Reporter

## Overview

PerfSage SLO Reporter is a JMeter plugin that evaluates performance test results against configurable SLO (Service Level Objective) thresholds and generates intelligent reports with AI-powered anomaly detection.

## System Components

```
                                    JMeter Test Plan
                                          │
                                          │ runs
                                          ▼
    ┌─────────────────────────────────────────────────────────┐
    │                    JMeter Engine                         │
    │  ┌─────────────────────────────────────────────────────┐│
    │  │          SLO Listener (JSR223)                      ││
    │  │  Receives sample events from Samplers               ││
    │  └─────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────┘
                            │
                            │ forwards to
                            ▼
    ┌─────────────────────────────────────────────────────────┐
    │                   Core Engine                            │
    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
    │  │ SLO Config  │  │   SLO       │  │   Anomaly       │  │
    │  │  Parser     │──│  Evaluator  │──│   Detector      │  │
    │  │             │  │             │  │   (AI/ML)       │  │
    │  └─────────────┘  └─────────────┘  └─────────────────┘  │
    └─────────────────────────────────────────────────────────┘
                            │
                            │ outputs to
                            ▼
    ┌─────────────────────────────────────────────────────────┐
    │                  Report Generator                        │
    │  ┌───────────┐  ┌───────────┐  ┌─────────────────────┐  │
    │  │   HTML    │  │   PDF     │  │     Markdown        │  │
    │  │  Report   │  │  Export   │  │     Export          │  │
    │  └───────────┘  └───────────┘  └─────────────────────┘  │
    └─────────────────────────────────────────────────────────┘
```

## Component Details

### 1. SLO Listener (`SLOListener.java`)

**Purpose**: JMeter test listener that captures all sample results during test execution.

**Interfaces**: 
- `SampleListener` - Receives `SampleEvent` callbacks
- `TestListener` - Handles test start/end lifecycle

**Responsibilities**:
- Listen to all sampler events
- Accumulate response times, error counts, and throughput
- Forward aggregated data to SLO Evaluator at test end
- Support distributed JMeter testing (aggregate results from slaves)

### 2. SLO Config Parser (`SLOConfig.java`)

**Purpose**: Parse and validate SLO configuration from JMeter properties or XML.

**SLO Structure**:
```java
public class SLO {
    String name;              // "Homepage Response Time"
    String metric;            // "response_time", "error_rate", "throughput"
    String operator;          // "lte", "gte", "eq"
    double threshold;         // 500
    String unit;              // "ms", "percent", "tps"
    int percentile;           // 95 (for response time)
    String labelPattern;      // "Homepage.*" (regex for label matching)
}
```

**Configuration Sources**:
- `jmeter.properties` file
- `user.properties` file  
- Command-line `-J` arguments
- XML file (loaded via `-G`)

### 3. SLO Evaluator (`SLOEvaluator.java`)

**Purpose**: Core engine that evaluates test results against SLO thresholds.

**Algorithm**:
```
for each SLO:
    extract relevant metrics from test results
    compute aggregated value (avg, p95, max, etc.)
    apply operator (<=, >=, ==)
    mark as PASS or FAIL
    record deviation from threshold
```

**Metric Types**:
| Metric | Aggregation | SLO Operator |
|--------|-------------|-------------|
| Response Time | avg, p50, p90, p95, p99, max | <= |
| Error Rate | percentage of failed samples | <= |
| Throughput | requests/second | >= |
| Hits | total requests | >= |

### 4. Anomaly Detector (`AnomalyDetector.java`)

**Purpose**: AI/ML-based detection of unusual patterns in performance data.

**Approach**: 
- **Phase 1** (v1): Statistical anomaly detection using Z-score and IQR
- **Phase 2** (v2): Isolation Forest algorithm (embedded via SMILE library)
- **Phase 3** (v3): Neural network autoencoder for time-series patterns

**Detection Targets**:
- Response time spikes (sudden increase in latency)
- Error bursts (clustering of failures)
- Throughput degradation under constant load
- Gradual performance drift (performance regression)

### 5. Report Generator (`ReportGenerator.java`)

**Purpose**: Generate multi-format reports from evaluation results.

**Templates**:
- **HTML**: Interactive dashboard with charts (Chart.js)
- **PDF**: Professional formatted report (iText/Apache PDFBox)
- **Markdown**: Git-friendly text report

**Report Sections**:
1. Executive Summary (pass/fail status, key metrics)
2. SLO Compliance Table (each SLO with status)
3. Response Time Distribution (percentiles chart)
4. Error Analysis (top errors by count)
5. AI Anomalies (detected patterns with explanations)
6. Recommendations (actionable improvement suggestions)

## Data Flow

```
Test Start
    │
    ▼
[Sample Events] ────────► [Accumulator] ◄───── [Stats Aggregator]
    │                                                  │
    │ (per sample)                                     │ (at test end)
    │                                                  ▼
    │                                      [SLO Evaluator]
    │                                                  │
    │                                                  ▼
    │                                      [Anomaly Detector]
    │                                                  │
    │                                                  ▼
    └──────────────────────────────────► [Report Generator] ◄── [Results]
                                                   │
                                                   ▼
                                        [Output Files / CI Exit Code]
```

## Maven Project Structure

```
perfsage-slo-reporter/
├── pom.xml                 # Maven build, JMeter dependencies
├── README.md
├── ARCHITECTURE.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/perfsage/jmeter/
│       │       ├── slo/
│       │       │   ├── SLO.java              # SLO model class
│       │       │   ├── SLOConfig.java        # Config parser
│       │       │   ├── SLOEvaluator.java     # Core evaluation
│       │       │   └── AnomalyDetector.java  # AI detection
│       │       ├── listeners/
│       │       │   └── SLOListener.java      # JMeter listener
│       │       ├── reports/
│       │       │   ├── ReportGenerator.java  # Multi-format export
│       │       │   ├── HtmlReport.java       # HTML template
│       │       │   ├── PdfReport.java        # PDF export
│       │       │   └── MarkdownReport.java   # MD export
│       │       └── gui/
│       │           └── SLOConfigGUI.java     # JMeter GUI panel
│       └── resources/
│           ├── com/perfsage/jmeter/slo/slo.properties
│           └── reports/templates/            # Report HTML templates
└── target/                   # Build output
```

## Key JMeter Dependencies

```xml
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_core</artifactId>
    <version>5.6.3</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_components</artifactId>
    <version>5.6.3</version>
    <scope>provided</scope>
</dependency>
```

## Performance Considerations

- **Memory**: Keep accumulator lightweight; use streaming statistics (Welford's algorithm)
- **Thread Safety**: Listener runs on JMeter worker threads; use thread-safe collections
- **Serialization**: Support JMeter distributed mode via `Serializable` interface
- **Overhead**: Plugin overhead must be < 1% of test execution time

## Future Enhancements

- OpenTelemetry metrics export
- Grafana dashboard template
- Integration with performance baselines
- GitHub Actions workflow for automated testing
- Custom ML model training on historical data
