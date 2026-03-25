# Configuration Reference

PerfSage SLO Reporter can be configured via JMeter properties file (`user.properties`) or programmatically through the listener GUI.

## Configuration Methods

### 1. Properties File

Edit your `$JMETER_HOME/bin/user.properties` file:

```properties
# PerfSage SLO Reporter Configuration

# Global settings
perfsage.slo.enabled=true
perfsage.slo.report.format=html,json
perfsage.slo.report.dir=${JMETER_HOME}/reports
perfsage.slo.exit.on.failure=false

# SLO Definition: Response Time
perfsage.slo.responseTime.name=Homepage Response Time
perfsage.slo.responseTime.unit=ms
perfsage.slo.responseTime.operator=lte
perfsage.slo.responseTime.threshold=500
perfsage.slo.responseTime.percentile=95

# SLO Definition: Error Rate
perfsage.slo.errorRate.name=API Error Rate
perfsage.slo.errorRate.unit=percent
perfsage.slo.errorRate.operator=lte
perfsage.slo.errorRate.threshold=1.0

# SLO Definition: Throughput
perfsage.slo.throughput.name=Throughput
perfsage.slo.throughput.unit=tps
perfsage.slo.throughput.operator=gte
perfsage.slo.throughput.threshold=1000
```

### 2. Listener GUI

Configure SLOs directly in the JMeter listener:

1. Add `PerfSage SLO Reporter` listener to your test plan
2. Click `Add SLO Rule` for each metric you want to track
3. Configure name, unit, operator, threshold, and percentile

## Configuration Properties

### Global Properties

| Property | Default | Description |
|----------|---------|-------------|
| `perfsage.slo.enabled` | `true` | Enable/disable SLO reporting |
| `perfsage.slo.report.format` | `html` | Comma-separated list: `html,json,csv` |
| `perfsage.slo.report.dir` | `${JMETER_HOME}/reports` | Output directory for reports |
| `perfsage.slo.exit.on.failure` | `false` | Exit with code 1 if any SLO fails (for CI/CD) |

### SLO Rule Properties

Each SLO rule uses this naming pattern:
`perfsage.slo.<rulename>.<property>`

| Property | Description | Example Values |
|----------|-------------|----------------|
| `name` | Human-readable SLO name | `Homepage Response Time` |
| `unit` | Measurement unit | `ms`, `s`, `percent`, `tps`, `count` |
| `operator` | Comparison operator | `lte` (≤), `lt` (<), `gte` (≥), `gt` (>), `eq` (=) |
| `threshold` | Numeric threshold value | `500`, `1.0`, `1000` |
| `percentile` | Percentile for response time (optional) | `90`, `95`, `99` |

### Supported Units

| Unit | Description | Valid For |
|------|-------------|----------|
| `ms` | Milliseconds | Response time |
| `s` | Seconds | Response time |
| `percent` | Percentage (0-100) | Error rate |
| `tps` | Transactions per second | Throughput |
| `count` | Raw count | Error count, request count |

## Sample Configurations

### Basic E-commerce SLOs

```properties
# 95th percentile response time under 1 second
perfsage.slo.rt.name=Checkout Response Time
perfsage.slo.rt.unit=ms
perfsage.slo.rt.operator=lte
perfsage.slo.rt.threshold=1000
perfsage.slo.rt.percentile=95

# Error rate below 0.1%
perfsage.slo.err.name=Checkout Error Rate
perfsage.slo.err.unit=percent
perfsage.slo.err.operator=lte
perfsage.slo.err.threshold=0.1

# At least 100 TPS
perfsage.slo.tps.name=Checkout Throughput
perfsage.slo.tps.unit=tps
perfsage.slo.tps.operator=gte
perfsage.slo.tps.threshold=100
```

### CI/CD Mode

```properties
# Exit with failure code if SLOs are violated
perfsage.slo.exit.on.failure=true

# Generate JSON report for pipeline parsing
perfsage.slo.report.format=json

# Output to workspace directory
perfsage.slo.report.dir=${user.dir}/test-results
```

## JMeter Integration

### Using with JMeter CLI

```bash
# Run with SLO reporting
jmeter -n -t test-plan.jmx -Juser.properties=user.properties -l results.jtl
```

### Using in CI/CD Pipeline

```yaml
# GitHub Actions example
- name: Run Performance Tests
  run: |
    jmeter -n -t perf-tests.jmx -l results.jtl -p user.properties
    if [ $? -ne 0 ]; then
      echo "SLO violations detected!"
      exit 1
    fi
```
