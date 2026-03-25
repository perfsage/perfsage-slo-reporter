package com.perfsage.jmeter.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration loader for SLO Reporter.
 * Reads SLO thresholds and reporting settings from properties file.
 */
public class SLOConfiguration {

    private String configPath;
    private Properties properties;
    private double errorRateThreshold;
    private long latencyP99Threshold;
    private long latencyP95Threshold;
    private int availabilityTarget;
    private String reportOutputDir;
    private String reportOutputFile;
    private boolean generateHtml;
    private boolean enableAiSuggestions;
    private int analysisWindowSeconds;

    public SLOConfiguration(String configPath) {
        this.configPath = configPath;
        this.properties = new Properties();
        loadDefaults();
        if (configPath != null && !configPath.isEmpty()) {
            loadFromFile(configPath);
        }
    }

    private void loadDefaults() {
        errorRateThreshold = 1.0;
        latencyP99Threshold = 500L;
        latencyP95Threshold = 300L;
        availabilityTarget = 99;
        reportOutputDir = System.getProperty("user.dir");
        reportOutputFile = "slo-report.json";
        generateHtml = false;
        enableAiSuggestions = true;
        analysisWindowSeconds = 60;
    }

    private void loadFromFile(String path) {
        try {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                properties.load(Files.newInputStream(p));
                errorRateThreshold = Double.parseDouble(
                        properties.getProperty("slo.error.rate.threshold", "1.0"));
                latencyP99Threshold = Long.parseLong(
                        properties.getProperty("slo.latency.p99.threshold.ms", "500"));
                latencyP95Threshold = Long.parseLong(
                        properties.getProperty("slo.latency.p95.threshold.ms", "300"));
                availabilityTarget = Integer.parseInt(
                        properties.getProperty("slo.availability.target", "99"));
                reportOutputDir = properties.getProperty("report.output.dir", reportOutputDir);
                reportOutputFile = properties.getProperty("report.output.file", reportOutputFile);
                generateHtml = Boolean.parseBoolean(
                        properties.getProperty("report.generate.html", "false"));
                enableAiSuggestions = Boolean.parseBoolean(
                        properties.getProperty("report.ai.suggestions.enabled", "true"));
                analysisWindowSeconds = Integer.parseInt(
                        properties.getProperty("analysis.window.seconds", "60"));
            }
        } catch (IOException e) {
            System.err.println("[PerfSage] Failed to load config: " + e.getMessage());
        }
    }

    // Getters
    public double getErrorRateThreshold() { return errorRateThreshold; }
    public long getLatencyP99Threshold() { return latencyP99Threshold; }
    public long getLatencyP95Threshold() { return latencyP95Threshold; }
    public int getAvailabilityTarget() { return availabilityTarget; }
    public String getReportOutputDir() { return reportOutputDir; }
    public String getReportOutputFile() { return reportOutputFile; }
    public boolean isGenerateHtml() { return generateHtml; }
    public boolean isEnableAiSuggestions() { return enableAiSuggestions; }
    public int getAnalysisWindowSeconds() { return analysisWindowSeconds; }

    // Setters
    public void setErrorRateThreshold(double errorRateThreshold) {
        this.errorRateThreshold = errorRateThreshold;
    }

    public void setLatencyP99Threshold(long latencyP99Threshold) {
        this.latencyP99Threshold = latencyP99Threshold;
    }

    public void setLatencyP95Threshold(long latencyP95Threshold) {
        this.latencyP95Threshold = latencyP95Threshold;
    }

    public void setAvailabilityTarget(int availabilityTarget) {
        this.availabilityTarget = availabilityTarget;
    }

    public void setReportOutputDir(String reportOutputDir) {
        this.reportOutputDir = reportOutputDir;
    }

    public void setReportOutputFile(String reportOutputFile) {
        this.reportOutputFile = reportOutputFile;
    }

    public void setGenerateHtml(boolean generateHtml) {
        this.generateHtml = generateHtml;
    }

    public void setEnableAiSuggestions(boolean enableAiSuggestions) {
        this.enableAiSuggestions = enableAiSuggestions;
    }

    public void setAnalysisWindowSeconds(int analysisWindowSeconds) {
        this.analysisWindowSeconds = analysisWindowSeconds;
    }
}
