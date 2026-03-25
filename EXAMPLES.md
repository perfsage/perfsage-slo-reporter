# Examples

This document contains practical examples of PerfSage SLO Reporter configurations for common use cases.

## Table of Contents

- [E-commerce Checkout](#e-commerce-checkout)
- [API Microservices](#api-microservices)
- [Database Query Performance](#database-query-performance)
- [Frontend Page Load](#frontend-page-load)
- [CI/CD Pipeline Integration](#cicd-pipeline-integration)
- [Sample JMeter Test Plan Structure](#sample-jmeter-test-plan-structure)

---

## E-commerce Checkout

A realistic example for an e-commerce platform's checkout flow.

```properties
# Global settings
perfsage.slo.enabled=true
perfsage.slo.report.format=html,json
perfsage.slo.report.dir=${JMETER_HOME}/reports/ecommerce
perfsage.slo.exit.on.failure=true

# Checkout page response time (95th percentile < 2s)
perfsage.slo.checkout.name=Checkout Page Response Time
perfsage.slo.checkout.unit=ms
perfsage.slo.checkout.operator=lte
perfsage.slo.checkout.threshold=2000
perfsage.slo.checkout.percentile=95

# Product search response time (95th percentile < 500ms)
perfsage.slo.search.name=Product Search Response Time
perfsage.slo.search.unit=ms
perfsage.slo.search.operator=lte
perfsage.slo.search.threshold=500
perfsage.slo.search.percentile=95

# Cart operations error rate (< 0.5%)
perfsage.slo.carterr.name=Cart Operations Error Rate
perfsage.slo.carterr.unit=percent
perfsage.slo.carterr.operator=lte
perfsage.slo.carterr.threshold=0.5

# Payment gateway throughput (> 50 TPS)
perfsage.slo.payment.name=Payment Gateway Throughput
perfsage.slo.payment.unit=tps
perfsage.slo.payment.operator=gte
perfsage.slo.payment.threshold=50
```

---

## API Microservices

Example for a microservices-based API platform.

```properties
# Global settings
perfsage.slo.enabled=true
perfsage.slo.report.format=json
perfsage.slo.report.dir=${user.dir}/api-reports
perfsage.slo.exit.on.failure=true

# Auth service response time (< 100ms)
perfsage.slo.auth.name=Auth Service Response Time
perfsage.slo.auth.unit=ms
perfsage.slo.auth.operator=lte
perfsage.slo.auth.threshold=100
perfsage.slo.auth.percentile=99

# User API error rate (< 0.1%)
perfsage.slo.userapi.name=User API Error Rate
perfsage.slo.userapi.unit=percent
perfsage.slo.userapi.operator=lte
perfsage.slo.userapi.threshold=0.1

# Order service throughput (> 200 TPS)
perfsage.slo.order.name=Order Service Throughput
perfsage.slo.order.unit=tps
perfsage.slo.order.operator=gte
perfsage.slo.order.threshold=200

# Database connection errors (< 0)
perfsage.slo.dberr.name=Database Connection Errors
perfsage.slo.dberr.unit=count
perfsage.slo.dberr.operator=lte
perfsage.slo.dberr.threshold=0
```

---

## Database Query Performance

Monitoring database query performance in a data-intensive application.

```properties
perfsage.slo.enabled=true
perfsage.slo.report.format=html,csv
perfsage.slo.report.dir=${JMETER_HOME}/reports/db

# SELECT query response time (< 50ms at p95)
perfsage.slo.select.name=SELECT Query Response Time
perfsage.slo.select.unit=ms
perfsage.slo.select.operator=lte
perfsage.slo.select.threshold=50
perfsage.slo.select.percentile=95

# INSERT/UPDATE response time (< 100ms at p90)
perfsage.slo.write.name=Write Query Response Time
perfsage.slo.write.unit=ms
perfsage.slo.write.operator=lte
perfsage.slo.write.threshold=100
perfsage.slo.write.percentile=90

# Query failure rate (< 0.01%)
perfsage.slo.queryfail.name=Query Failure Rate
perfsage.slo.queryfail.unit=percent
perfsage.slo.queryfail.operator=lte
perfsage.slo.queryfail.threshold=0.01
```

---

## Frontend Page Load

Example for frontend performance monitoring using synthetic tests.

```properties
perfsage.slo.enabled=true
perfsage.slo.report.format=html
perfsage.slo.report.dir=${JMETER_HOME}/reports/frontend

# Homepage load time (< 2s)
perfsage.slo.homepage.name=Homepage Load Time
perfsage.slo.homepage.unit=ms
perfsage.slo.homepage.operator=lte
perfsage.slo.homepage.threshold=2000
perfsage.slo.homepage.percentile=90

# First Contentful Paint simulation (< 1.5s)
perfsage.slo.fcp.name=First Contentful Paint
perfsage.slo.fcp.unit=ms
perfsage.slo.fcp.operator=lte
perfsage.slo.fcp.threshold=1500
perfsage.slo.fcp.percentile=75

# Resource load error rate (< 1%)
perfsage.slo.resources.name=Resource Load Error Rate
perfsage.slo.resources.unit=percent
perfsage.slo.resources.operator=lte
perfsage.slo.resources.threshold=1
```

---

## CI/CD Pipeline Integration

Example configuration optimized for CI/CD environments.

```properties
# Fail fast on any SLO violation
perfsage.slo.enabled=true
perfsage.slo.exit.on.failure=true

# Generate machine-readable reports
perfsage.slo.report.format=json
perfsage.slo.report.dir=${user.dir}/ci-reports

# SLOs for CI validation
perfsage.slo.ci.name=CI Response Time
perfsage.slo.ci.unit=ms
perfsage.slo.ci.operator=lte
perfsage.slo.ci.threshold=1000

perfsage.slo.cierr.name=CI Error Rate
perfsage.slo.cierr.unit=percent
perfsage.slo.cierr.operator=lte
perfsage.slo.cierr.threshold=0.1
```

### GitHub Actions Workflow Example

```yaml
name: Performance Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  performance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Download JMeter
        run: |
          wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
          tar -xzf apache-jmeter-5.6.3.tgz

      - name: Build SLO Reporter
        run: |
          cd perfsage-slo-reporter
          mvn -B package
          cp target/*.jar ../apache-jmeter-5.6.3/lib/ext/

      - name: Run Performance Tests
        run: |
          cd apache-jmeter-5.6.3
          bin/jmeter -n -t ../perf-tests.jmx -l results.jtl -p ../user.properties

      - name: Upload Results
        uses: actions/upload-artifact@v4
        with:
          name: performance-results
          path: apache-jmeter-5.6.3/ci-reports/
```

---

## Sample JMeter Test Plan Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan>
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="PerfSage Demo">
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <!-- Thread Group -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Users">
        <elementProp name="ThreadGroup.main_controller" guiclass="LoopControlPanel" testclass="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">100</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">10</stringProp>
        <stringProp name="ThreadGroup.ramp_time">5</stringProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP Request Sampler -->
        <HTTPSamplerProxy guiclass="HttpSampleGui" testclass="HTTPSamplerProxy" testname="Get Homepage">
          <stringProp name="HTTPSampler.domain">example.com</stringProp>
          <stringProp name="HTTPSampler.port">80</stringProp>
          <stringProp name="HTTPSampler.path">/</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
        <hashTree/>

        <!-- PerfSage SLO Reporter Listener -->
        <ResultCollector guiclass="ResultCollector" testclass="ResultCollector" testname="PerfSage SLO Reporter">
          <boolProp name="ResultCollector.error_logging">false</boolProp>
          <objProp>
            <name>saveConfig</name>
            <value class="SampleSaveConfiguration">
              <time>true</time>
              <latency>true</latency>
            </value>
          </objProp>
          <stringProp name="filename">results.jtl</stringProp>
        </ResultCollector>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

---

## Interpreting Results

### Sample Report Output (JSON)

```json
{
  "reportId": "slo-20250110-143052",
  "timestamp": "2025-01-10T14:30:52Z",
  "overallStatus": "PARTIAL_FAILURE",
  "sloResults": [
    {
      "name": "Checkout Page Response Time",
      "status": "PASS",
      "actualValue": 847,
      "threshold": 2000,
      "unit": "ms",
      "percentile": 95
    },
    {
      "name": "Payment Gateway Throughput",
      "status": "FAIL",
      "actualValue": 42,
      "threshold": 50,
      "unit": "tps",
      "severity": "HIGH"
    }
  ],
  "recommendations": [
    "Payment gateway throughput is 16% below target. Consider scaling payment service instances.",
    "Checkout response times are healthy with good margin."
  ]
}
```
