# PerfSage SLO Reporter

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://opensource.org/licenses/MIT)
[![PerfSage Website](https://img.shields.io/badge/PerfSage-Website-purple?style=for-the-badge)](https://perfsage.com)

> AI-powered SLO compliance reporter for Apache JMeter. Automatically analyze test results and generate intelligent performance reports.

---

## What is PerfSage SLO Reporter?

PerfSage SLO Reporter is a JMeter plugin that bridges the gap between raw performance test data and actionable insights. Instead of manually exporting JMeter results to spreadsheets and spending hours analyzing them, this plugin automatically evaluates your test results against defined SLO thresholds and generates professional reports with AI-enhanced recommendations.

## Features

- **SLO Threshold Definition** - Define response time, error rate, and throughput SLOs
- **Automatic Compliance Checking** - Real-time evaluation during test execution
- **AI Anomaly Detection** - Identifies unusual patterns in response times and errors
- **Smart Report Generation** - HTML, PDF, and Markdown report exports
- **CI/CD Integration** - Exit with non-zero code on SLO violations
- **Slack/Email Notifications** - Automated alerts for test failures
- **Trend Analysis** - Compare results across multiple test runs

## Planned Architecture

```
perfsage-slo-reporter/
  src/
    main/
      java/
        com/perfsage/jmeter/
          slo/
            SLOConfig.java           # SLO threshold definitions
            SLOEvaluator.java        # Core SLO compliance engine
            AnomalyDetector.java     # AI-based anomaly detection
            ReportGenerator.java     # Multi-format report generation
          listeners/
            SLOListener.java         # JMeter test listener
          gui/
            SLOConfigGUI.java        # GUI for SLO configuration
  pom.xml
```

## How It Works

1. **Configure SLOs** in the JMeter GUI (or via XML)
2. **Run your JMeter tests** as usual
3. **SLO Reporter analyzes** results in real-time
4. **Get instant feedback** on compliance status
5. **Export beautiful reports** for stakeholders

## Installation (Coming Soon)

```bash
# Clone the repository
git clone https://github.com/perfsage/perfsage-slo-reporter.git
cd perfsage-slo-reporter

# Build with Maven
mvn clean package

# Copy the JAR to JMeter's lib/ext directory
cp target/perfsage-slo-reporter-*.jar $JMETER_HOME/lib/ext/
```

## SLO Configuration Example

```xml
<slo name="Homepage Response Time">
  <threshold unit="ms" operator="lte">500</threshold>
  <percentile>95</percentile>
</slo>
<slo name="API Error Rate">
  <threshold unit="percent" operator="lte">1.0</threshold>
</slo>
<slo name="Throughput">
  <threshold unit="tps" operator="gte">1000</threshold>
</slo>
```

## Roadmap

- [ ] Core SLO evaluation engine
- [ ] JMeter listener implementation
- [ ] GUI configuration panel
- [ ] AI anomaly detection (using embedded ML model)
- [ ] HTML report templates
- [ ] PDF export
- [ ] CI/CD exit codes
- [ ] Slack webhook integration
- [ ] Email notifications
- [ ] Historical trend comparison

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## License

MIT License - see [LICENSE](LICENSE) for details.

---

**PerfSage** - AI-Powered Performance Engineering | [perfsage.com](https://perfsage.com)
