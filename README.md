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
| Anomaly-style insights | Heuristic notes on tails, errors, and spikes (statistical v1; optional LLM hints are future/BYOK) |
| Smart Report Generation | HTML (Chart.js dashboard), Markdown, optional PDF next to JSON |
| CI/CD Integration | Exit with non-zero code on SLO violations |
| Slack/Email Notifications | Automated alerts for test failures |
| Trend Analysis | Compare results across multiple test runs |

## Quick Start

### 1. Installation

**Option A — JMeter Plugins Manager (custom repository)**

1. In GitHub: enable **Pages** for this repository (**Settings → Pages → Build: GitHub Actions**). Merge the default branch so the **Deploy plugin repository JSON** workflow runs (or run it manually).
2. In JMeter: **Options → Plugins Manager → Add repository**, paste:

   `https://perfsage.github.io/perfsage-slo-reporter/repo/perfsage-plugins.json`

3. Install **PerfSage SLO Reporter** from the list and restart JMeter if prompted.

**Option B — GitHub Releases (prebuilt JAR)**

```bash
VERSION=0.1.0
curl -fsSL -o perfsage-slo-reporter.jar \
  "https://github.com/perfsage/perfsage-slo-reporter/releases/download/v${VERSION}/perfsage-slo-reporter-${VERSION}.jar"
cp perfsage-slo-reporter.jar "$JMETER_HOME/lib/ext/"
```

**Option C — Build from source**

```bash
git clone https://github.com/perfsage/perfsage-slo-reporter.git
cd perfsage-slo-reporter
mvn clean package
VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)
cp "target/perfsage-slo-reporter-${VERSION}.jar" "$JMETER_HOME/lib/ext/"
```

Use **Backend Listener** implementation class `com.perfsage.jmeter.SLOAnalysisListener` in your test plan (see [SETUP.md](SETUP.md)).

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

After the run, open **`slo-report.html`** in a browser (same directory as `slo-report.json`). Non-GUI mode has no live Swing dashboard; the HTML file is the visual report.

## Documentation

| Document | Description |
|----------|-------------|
| [SETUP.md](SETUP.md) | Installation and verification guide |
| [docs/jmeter-plugins-org-submission.md](docs/jmeter-plugins-org-submission.md) | Adding this plugin to the official jmeter-plugins.org catalog |
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
