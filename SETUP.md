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

# Copy the JAR to JMeter's lib/ext directory
cp target/perfsage-slo-reporter-*.jar $JMETER_HOME/lib/ext/

# Restart JMeter
```

### Option 2: Download JAR from Releases

```bash
# Download the latest release JAR
curl -L -o perfsage-slo-reporter.jar https://github.com/perfsage/perfsage-slo-reporter/releases/latest/download/perfsage-slo-reporter.jar

# Copy to JMeter's lib/ext directory
cp perfsage-slo-reporter.jar $JMETER_HOME/lib/ext/

# Restart JMeter
```

## Verification

To verify the installation:

1. Open JMeter GUI
2. Right-click on your Thread Group
3. Go to `Add` → `Listener`
4. You should see `PerfSage SLO Reporter` in the list

## Configuration

See [CONFIG.md](CONFIG.md) for detailed configuration options.

## Quick Start

1. Add the SLO Listener to your test plan
2. Configure your SLO thresholds in the listener GUI or properties file
3. Run your JMeter test
4. Check the console output and generated reports

See [EXAMPLES.md](EXAMPLES.md) for sample configurations and test plans.
