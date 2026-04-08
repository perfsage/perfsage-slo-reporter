# Changelog

All notable changes to PerfSage SLO Reporter will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-04-08

### Added
- GitHub Actions release workflow (tag `v*` → attach versioned fat JAR) and Pages workflow for JMeter Plugins Manager descriptor + screenshot asset
- Public plugin repository descriptor at `repo/perfsage-plugins.json`
- Initial release of PerfSage SLO Reporter for Apache JMeter
- SLOReporter class: Main JMeter listener that collects samples and generates SLO analysis reports
- SLOAnalysisListener class: BackendListener implementation for real-time metrics streaming
- SLOConfig model: JSON-based SLO configuration with support for RESPONSE_TIME, ERROR_RATE, SUCCESS_RATE, and THROUGHPUT metrics
- SLOAnalysisResult model: Comprehensive result model with per-label metrics, percentiles (P90/P95/P99), and AI-powered suggestions
- SLOConfiguration: Config loader for threshold settings
- Support for critical vs non-critical SLOs with custom AI hints
- Percentile-based response time analysis (P90, P95, P99)
- JSON report output with aggregate and per-label metrics
- Sample SLO configuration for e-commerce checkout flow
- JMeter SPI service registration for automatic plugin discovery
- plugin.properties for JMeter plugin registration

### Project Structure
- Maven-based build with Java 17
- Dependencies on JMeter 5.6.3, Jackson 2.17.2, SLF4J, JUnit 5
- JAR packaging with Shade plugin for fat JAR
- MIT License
