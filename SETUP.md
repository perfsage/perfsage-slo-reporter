# Setup Guide

This guide walks you through installing and configuring PerfSage SLO Reporter.

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8 or higher
- Apache JMeter 5.6 or higher

## Installation

### Option 1: Build from Source

```bash
# Clone the repository
git clone https://github.com/perfsage/perfsage-slo-reporter.git
cd perfsage-slo-reporter

# Build with Maven
mvn clean package

# Copy the fat JAR to JMeter's lib/ext (exclude -sources / -javadoc if present)
VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression=project.version)
cp "target/perfsage-slo-reporter-${VERSION}.jar" "$JMETER_HOME/lib/ext/"

# Restart JMeter
```

### Option 2: Download JAR from Releases

Release assets are named `perfsage-slo-reporter-{version}.jar` (for example `perfsage-slo-reporter-0.1.0.jar` for tag `v0.1.0`).

```bash
VERSION=0.1.0
curl -fsSL -o perfsage-slo-reporter.jar \
  "https://github.com/perfsage/perfsage-slo-reporter/releases/download/v${VERSION}/perfsage-slo-reporter-${VERSION}.jar"
cp perfsage-slo-reporter.jar "$JMETER_HOME/lib/ext/"
```

### Option 3: JMeter Plugins Manager

See [README.md](README.md) (custom repository URL on GitHub Pages).

## Verification

To verify the installation:

1. Open the JMeter GUI.
2. Right-click the test plan or thread group → **Add** → **Listener** → **Backend Listener**.
3. Set **Backend Listener implementation** to `com.perfsage.jmeter.SLOAnalysisListener` (or pick it from the class dropdown if registered).
4. Run a short test and confirm `slo-report.json` / `slo-report.html` appear in your configured output directory.

## Configuration

See [CONFIG.md](CONFIG.md) for detailed configuration options.

## Quick Start

1. Add the SLO Listener to your test plan
2. Configure your SLO thresholds in the listener GUI or properties file
3. Run your JMeter test
4. Check the console output and generated reports

See [EXAMPLES.md](EXAMPLES.md) for sample configurations and test plans.
