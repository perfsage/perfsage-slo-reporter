# PerfSage SLO Reporter

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://opensource.org/licenses/MIT)
[![CI Build](https://img.shields.io/github/actions/workflow/status/perfsage/perfsage-slo-reporter/ci.yml?branch=main&style=flat-square&logo=github)](https://github.com/perfsage/perfsage-slo-reporter/actions)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-Coming%20Soon-blue.svg?style=flat-square)](https://search.maven.org/search?q=com.perfsage)
[![PerfSage](https://img.shields.io/badge/PerfSage-Website-purple?style=for-the-badge)](https://perfsage.com)

> **AI-powered SLO compliance reporter for Apache JMeter.** Automatically analyze test results and generate intelligent performance reports.

---

## What is PerfSage SLO Reporter?

PerfSage SLO Reporter is a JMeter plugin that bridges the gap between raw performance test data and actionable insights. Instead of manually exporting JMeter results to spreadsheets and spending hours analyzing them, this plugin automatically evaluates your test results against defined SLO thresholds and generates professional reports with AI-enhanced recommendations.

## Features

| Feature | Description |
|---------|-------------|
| SLO Threshold Definition | Define response time, error rate, and throughput SLOs |
| Automatic Compliance Checking | Real-time evaluation during test execution |
| AI Anomaly Detection | Identifies unusual patterns in response times and errors |
| Smart Report Generation | HTML, PDF, and Markdown report exports |
| CI/CD Integration | Exit with non-zero code on SLO violations |
| Slack/Email Notifications | Automated alerts for test failures |
| Trend Analysis | Compare results across multiple test runs |

## Quick Start

### 1. Installation

```bash
# Clone and build
git clone https://github.com/perfsage/perfsage-slo-reporter.git
cd perfsage-slo-reporter
mvn clean package

# Install to JMeter
cp target/perfsage-slo-reporter-*.jar $JMETER_HOME/lib/ext/
```

### 2. Configure SLOs

Add to your `$JMETER_HOME/bin/user.properties`:

```properties
perfsage.slo.enabled=true
perfsage.slo.response.name=Response Time
perfsage.slo.response.unit=ms
perfsage.slo.response.operator=lte
perfsage.slo.response.threshold=500
perfsage.slo.response.percentile=95

perfsage.slo.error.name=Error Rate
perfsage.slo.error.unit=percent
perfsage.slo.error.operator=lte
perfsage.slo.error.threshold=1.0
```

### 3. Run Your Tests

```bash
jmeter -n -t your-test.jmx -l results.jtl
```

## Documentation

| Document | Description |
|----------|-------------|
| [SETUP.md](SETUP.md) | Installation and verification guide |
| [CONFIG.md](CONFIG.md) | Configuration reference and options |
| [EXAMPLES.md](EXAMPLES.md) | Practical examples and use cases |
| [CONTRIBUTING.md](CONTRIBUTING.md) | How to contribute |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design and architecture |
| [CHANGELOG.md](CHANGELOG.md) | Version history |
| [PUBLISHING.md](PUBLISHING.md) | Publishing to Maven Central |

## Sample Configurations

Sample configuration files are available in the [`samples/`](samples/) directory:

- [`samples/slo.properties`](samples/slo.properties) - Sample SLO configuration

## Technology Stack

- **Java 17+** - Core plugin implementation
- **Apache JMeter** - Load testing framework
- **Maven** - Build and dependency management
- **GitHub Actions** - CI/CD pipeline

## Requirements

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8 or higher
- Apache JMeter 5.6 or higher

## Status

This project is currently in active development. The core SLO evaluation engine and JMeter listener are being built. Track progress in [Issues](https://github.com/perfsage/perfsage-slo-reporter/issues).

## License

MIT License - see [LICENSE](LICENSE) for details.

---

**PerfSage** - AI-Powered Performance Engineering | [perfsage.com](https://perfsage.com) | hello@perfsage.com
