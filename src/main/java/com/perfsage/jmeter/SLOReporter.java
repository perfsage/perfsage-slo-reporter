package com.perfsage.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.reporters.AbstractListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PerfSage SLO Reporter - A JMeter Reporter that analyzes test results
 * against defined SLOs (Service Level Objectives) and generates
 * intelligent performance reports with AI-powered suggestions.
 *
 * To use: Add the JAR to JMeter's lib/ext directory, then reference
 * in jmeter.properties: slo.reporter.config.path=/path/to/slo-config.json
 */
public class SLOReporter extends AbstractListener {

    private static final Logger LOG = LoggerFactory.getLogger(SLOReporter.class);
    private static final long serialVersionUID = 1L;

    public static final String CONFIG_PATH_PROPERTY = "SLOReporter.configPath";
    public static final String OUTPUT_PATH_PROPERTY = "SLOReporter.outputPath";

    private SLOConfig sloConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<SampleResult>> resultsByLabel = new HashMap<>();
    private final List<SampleResult> allResults = new ArrayList<>();

    /**
     * Load SLO configuration from JSON file.
     */
    private void loadConfig() {
        String configPath = getPropertyAsString(CONFIG_PATH_PROPERTY);
        if (configPath == null || configPath.trim().isEmpty()) {
            configPath = JMeterUtils.getPropDefault("slo.reporter.config.path", "slo-config.json");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            LOG.warn("SLO config file not found: {}. Using default empty config.", configPath);
            sloConfig = new SLOConfig();
            sloConfig.setName("Default SLO Config");
            sloConfig.setDescription("No config file found - using defaults");
            return;
        }

        try {
            sloConfig = objectMapper.readValue(configFile, SLOConfig.class);
            LOG.info("Loaded SLO config: {} - {} SLOs defined",
                    sloConfig.getName(),
                    sloConfig.getSlos() != null ? sloConfig.getSlos().size() : 0);
        } catch (IOException e) {
            LOG.error("Failed to parse SLO config file: {}", configPath, e);
            sloConfig = new SLOConfig();
        }
    }

    /**
     * Called for each sample result during test execution.
     */
    @Override
    public void sampleOccurred(SampleResult sampleResult) {
        synchronized (allResults) {
            allResults.add(sampleResult);
            String label = sampleResult.getSampleLabel();
            resultsByLabel.computeIfAbsent(label, k -> new ArrayList<>()).add(sampleResult);
        }
    }

    /**
     * Called when test run completes - generate SLO analysis report.
     */
    @Override
    public void testEnded(String host) {
        LOG.info("SLOReporter: Test ended on host: {}", host);
        analyzeAndReport();
    }

    @Override
    public void testEnded() {
        LOG.info("SLOReporter: Test ended");
        analyzeAndReport();
    }

    /**
     * Analyze all results against SLOs and generate report.
     */
    private void analyzeAndReport() {
        if (allResults.isEmpty()) {
            LOG.warn("No samples collected - skipping SLO analysis");
            return;
        }

        loadConfig();

        SLOAnalysisResult analysisResult = new SLOAnalysisResult();
        analysisResult.setConfigName(sloConfig.getName());
        analysisResult.setTestEndTime(System.currentTimeMillis());
        analysisResult.setTotalSamples(allResults.size());

        // Compute aggregate metrics
        computeAggregateMetrics(analysisResult);

        // Analyze each SLO
        if (sloConfig.getSlos() != null) {
            for (SLOConfig.SLODefinition slo : sloConfig.getSlos()) {
                evaluateSLO(slo, analysisResult);
            }
        }

        // Generate AI suggestions
        generateAISuggestions(analysisResult);

        // Write report
        writeReport(analysisResult);

        LOG.info("SLO analysis complete. Report written to: {}", getOutputPath());
    }

    /**
     * Compute aggregate metrics from all sample results.
     */
    private void computeAggregateMetrics(SLOAnalysisResult result) {
        long totalErrors = 0;
        long totalSuccess = 0;
        long totalTime = 0;

        synchronized (allResults) {
            for (SampleResult sr : allResults) {
                if (sr.isSuccessful()) {
                    totalSuccess++;
                } else {
                    totalErrors++;
                }
                totalTime += sr.getTime();
            }
        }

        result.setTotalErrors(totalErrors);
        result.setTotalSuccess(totalSuccess);

        double errorRate = (totalErrors * 100.0) / allResults.size();
        double successRate = (totalSuccess * 100.0) / allResults.size();
        double avgResponseTime = (double) totalTime / allResults.size();

        result.setAggregateErrorRate(errorRate);
        result.setAggregateSuccessRate(successRate);
        result.setAggregateAvgResponseTime(avgResponseTime);

        // Per-label metrics
        Map<String, SLOAnalysisResult.LabelMetrics> labelMetrics = new HashMap<>();
        for (Map.Entry<String, List<SampleResult>> entry : resultsByLabel.entrySet()) {
            String label = entry.getKey();
            List<SampleResult> samples = entry.getValue();
            SLOAnalysisResult.LabelMetrics metrics = computeLabelMetrics(samples);
            labelMetrics.put(label, metrics);
        }
        result.setLabelMetrics(labelMetrics);
    }

    /**
     * Compute metrics for a single label.
     */
    private SLOAnalysisResult.LabelMetrics computeLabelMetrics(List<SampleResult> samples) {
        SLOAnalysisResult.LabelMetrics metrics = new SLOAnalysisResult.LabelMetrics();

        int errors = 0;
        int success = 0;
        long totalTime = 0;
        long[] times = new long[samples.size()];

        for (int i = 0; i < samples.size(); i++) {
            SampleResult sr = samples.get(i);
            times[i] = sr.getTime();
            if (sr.isSuccessful()) {
                success++;
            } else {
                errors++;
            }
            totalTime += sr.getTime();
        }

        // Sort for percentile calculation
        java.util.Arrays.sort(times);

        metrics.setSuccessCount(success);
        metrics.setErrorCount(errors);
        metrics.setAvgResponseTime((double) totalTime / samples.size());
        metrics.setMinResponseTime(times[0]);
        metrics.setMaxResponseTime(times[times.length - 1]);

        // Percentiles
        metrics.setP90ResponseTime(percentile(times, 90));
        metrics.setP95ResponseTime(percentile(times, 95));
        metrics.setP99ResponseTime(percentile(times, 99));

        return metrics;
    }

    /**
     * Calculate percentile value from sorted array.
     */
    private long percentile(long[] sorted, int p) {
        int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        index = Math.max(0, Math.min(index, sorted.length - 1));
        return sorted[index];
    }

    /**
     * Evaluate a single SLO against collected metrics.
     */
    private void evaluateSLO(SLOConfig.SLODefinition slo, SLOAnalysisResult result) {
        SLOAnalysisResult.SLOEvaluation evaluation = new SLOAnalysisResult.SLOEvaluation();
        evaluation.setSloId(slo.getId());
        evaluation.setMetricType(slo.getMetricType());
        evaluation.setCritical(slo.getCritical());
        evaluation.setTarget(slo.getTarget());
        evaluation.setUnit(slo.getUnit());

        boolean passed = false;
        double actualValue = 0.0;

        SLOAnalysisResult.LabelMetrics labelMetrics = null;
        if (slo.getLabel() != null && !slo.getLabel().isEmpty()) {
            labelMetrics = result.getLabelMetrics().get(slo.getLabel());
        }

        switch (slo.getMetricType()) {
            case "RESPONSE_TIME":
                if (labelMetrics != null) {
                    actualValue = slo.getPercentile() != null
                            ? getSLOResult.Percentile(labelMetrics, slo.getPercentile())
                            : labelMetrics.getAvgResponseTime();
                    passed = compareValues(actualValue, slo.getOperator(), slo.getTarget());
                    evaluation.setActualValue(actualValue);
                } else {
                    actualValue = result.getAggregateAvgResponseTime();
                    passed = compareValues(actualValue, slo.getOperator(), slo.getTarget());
                    evaluation.setActualValue(actualValue);
                }
                break;

            case "ERROR_RATE":
                actualValue = labelMetrics != null
                        ? (labelMetrics.getErrorCount() * 100.0) / (labelMetrics.getSuccessCount() + labelMetrics.getErrorCount())
                        : result.getAggregateErrorRate();
                passed = compareValues(actualValue, slo.getOperator(), slo.getTarget());
                evaluation.setActualValue(actualValue);
                break;

            case "SUCCESS_RATE":
                actualValue = labelMetrics != null
                        ? (labelMetrics.getSuccessCount() * 100.0) / (labelMetrics.getSuccessCount() + labelMetrics.getErrorCount())
                        : result.getAggregateSuccessRate();
                passed = compareValues(actualValue, slo.getOperator(), slo.getTarget());
                evaluation.setActualValue(actualValue);
                break;

            case "THROUGHPUT":
                // Would need elapsed time from test - simplified for v1
                actualValue = -1;
                evaluation.setSkipped(true);
                break;

            default:
                LOG.warn("Unknown SLO metric type: {}", slo.getMetricType());
                evaluation.setSkipped(true);
        }

        evaluation.setPassed(passed);
        evaluation.setAiHint(slo.getAiHint());
        result.getSloEvaluations().add(evaluation);
    }

    /**
     * Compare actual value against target using operator.
     */
    private boolean compareValues(double actual, String operator, double target) {
        switch (operator != null ? operator.toUpperCase() : "LTE") {
            case "LTE":
            case "<=":
                return actual <= target;
            case "GTE":
            case ">=":
                return actual >= target;
            case "LT":
            case "<":
                return actual < target;
            case "GT":
            case ">":
                return actual > target;
            case "EQ":
            case "=":
                return Math.abs(actual - target) < 0.001;
            default:
                return actual <= target;
        }
    }

    /**
     * Get percentile value from label metrics.
     */
    private double getSLOResultPercentile(SLOAnalysisResult.LabelMetrics metrics, int percentile) {
        switch (percentile) {
            case 90:
                return metrics.getP90ResponseTime();
            case 95:
                return metrics.getP95ResponseTime();
            case 99:
                return metrics.getP99ResponseTime();
            default:
                return metrics.getAvgResponseTime();
        }
    }

    /**
     * Generate AI-powered suggestions based on SLO evaluations.
     */
    private void generateAISuggestions(SLOAnalysisResult result) {
        List<String> suggestions = new ArrayList<>();

        for (SLOAnalysisResult.SLOEvaluation eval : result.getSloEvaluations()) {
            if (!eval.isPassed()) {
                if (eval.getCritical()) {
                    suggestions.add(String.format(
                            "[CRITICAL] SLO '%s' FAILED: %s of %s %s. Actual: %.2f %s",
                            eval.getSloId(),
                            eval.getMetricType(),
                            eval.getOperator(),
                            eval.getTarget(),
                            eval.getActualValue(),
                            eval.getUnit()));

                    if (eval.getAiHint() != null && !eval.getAiHint().isEmpty()) {
                        suggestions.add("  AI Hint: " + eval.getAiHint());
                    }
                } else {
                    suggestions.add(String.format(
                            "[WARNING] SLO '%s' MISSED: %s of %s %s. Actual: %.2f %s",
                            eval.getSloId(),
                            eval.getMetricType(),
                            eval.getOperator(),
                            eval.getTarget(),
                            eval.getActualValue(),
                            eval.getUnit()));
                }
            }
        }

        // General recommendations
        if (result.getAggregateErrorRate() > 1.0) {
            suggestions.add(String.format(
                    "[ADVISORY] Overall error rate is %.2f%%. Consider investigating failed requests.",
                    result.getAggregateErrorRate()));
        }

        result.setSuggestions(suggestions);
    }

    /**
     * Write analysis report to output file (JSON).
     */
    private void writeReport(SLOAnalysisResult result) {
        try {
            String outputPath = getOutputPath();
            File outputFile = new File(outputPath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, result);
            LOG.info("SLO report written to: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to write SLO report", e);
        }
    }

    private String getOutputPath() {
        String path = getPropertyAsString(OUTPUT_PATH_PROPERTY);
        if (path == null || path.trim().isEmpty()) {
            path = JMeterUtils.getPropDefault("slo.reporter.output.path", "slo-report.json");
        }
        return path;
    }
}
