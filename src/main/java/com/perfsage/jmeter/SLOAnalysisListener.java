package com.perfsage.jmeter;

import com.perfsage.jmeter.SLOConfig;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jmeter.visualizers.backend.SamplerMetric;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JMeter Backend Listener implementation that collects sample results
 * and performs SLO analysis at the end of the test.
 * Outputs a JSON report file containing analysis metrics.
 */
public class SLOAnalysisListener implements BackendListenerClient, SampleListener {

    private static final String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir");
    private static final String DEFAULT_OUTPUT_FILE = "slo-report.json";

    private final Map<String, SampleStats> sampleStats = new ConcurrentHashMap<>();
    private final Map<String, Integer> failureReasons = new ConcurrentHashMap<>();
    private final List<SampleEvent> allEvents = Collections.synchronizedList(new ArrayList<>());

    private String outputDir;
    private String outputFile;
    private SLOConfig sloConfig;
    private int targetRps;
    private boolean generateHtml;
    private int percentileThreshold;
    private int analysisWindowSeconds;

    private static class SampleStats {
        long totalSamples = 0;
        long successCount = 0;
        long failureCount = 0;
        long totalResponseTime = 0;
        long maxResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        List<Long> responseTimes = new ArrayList<>();

        synchronized void add(long responseTime, boolean success, String failureMessage) {
            totalSamples++;
            totalResponseTime += responseTime;
            if (success) successCount++;
            else failureCount++;
            maxResponseTime = Math.max(maxResponseTime, responseTime);
            minResponseTime = Math.min(minResponseTime, responseTime);
            responseTimes.add(responseTime);
        }

        long getAvgResponseTime() {
            return totalSamples > 0 ? totalResponseTime / totalSamples : 0;
        }

        long getSuccessRate() {
            return totalSamples > 0 ? (successCount * 100) / totalSamples : 0;
        }
    }

    private static class PercentileCalculator {
        public static long calculate(List<Long> sortedTimes, double percentile) {
            if (sortedTimes.isEmpty()) return 0;
            int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
            index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
            return sortedTimes.get(index);
        }

        public static Map<String, Long> calculateAll(List<Long> times) {
            List<Long> sorted = new ArrayList<>(times);
            Collections.sort(sorted);

            Map<String, Long> result = new LinkedHashMap<>();
            result.put("p50", calculate(sorted, 50));
            result.put("p90", calculate(sorted, 90));
            result.put("p95", calculate(sorted, 95));
            result.put("p99", calculate(sorted, 99));
            result.put("p99.9", calculate(sorted, 99.9));
            result.put("max", sorted.isEmpty() ? 0 : sorted.get(sorted.size() - 1));
            return result;
        }
    }

    // ============================================
    // JMeter Test Lifecycle Methods
    // ============================================

    @Override
    public void setupTest(BackendListenerContext context) {
        outputDir = context.getParameter("outputDir", DEFAULT_OUTPUT_DIR);
        outputFile = context.getParameter("outputFile", DEFAULT_OUTPUT_FILE);
        targetRps = Integer.parseInt(context.getParameter("targetRps", "100"));
        generateHtml = Boolean.parseBoolean(context.getParameter("generateHtml", "false"));
        percentileThreshold = Integer.parseInt(context.getParameter("percentileThreshold", "99"));
        analysisWindowSeconds = Integer.parseInt(context.getParameter("analysisWindowSeconds", "60"));

        System.out.println("[PerfSage] SLO Analysis Listener initialized");
        System.out.println("[PerfSage] Output: " + outputDir + "/" + outputFile);
        System.out.println("[PerfSage] Target RPS: " + targetRps);
        System.out.println("[PerfSage] Percentile threshold: p" + percentileThreshold);
    }

    @Override
    public void handleSampleResults(List<SampleEvent> events, BackendListenerContext context) {
        for (SampleEvent event : events) {
            allEvents.add(event);

            SampleResult result = event.getResult();
            String label = result.getSampleLabel();
            long responseTime = result.getTime();
            boolean success = result.isSuccessful();
            String failureMessage = result.getResponseMessage();

            sampleStats.computeIfAbsent(label, k -> new SampleStats())
                    .add(responseTime, success, failureMessage);

            if (!success && failureMessage != null && !failureMessage.isEmpty()) {
                failureReasons.compute(failureMessage, (k, v) -> (v == null) ? 1 : v + 1);
            }
        }
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
        System.out.println("[PerfSage] Starting SLO analysis...");

        long totalSamples = 0;
        long totalSuccess = 0;
        long totalFailure = 0;
        List<SampleEvent> timeWindowEvents = new ArrayList<>();

        long now = System.currentTimeMillis();
        long windowStart = now - (analysisWindowSeconds * 1000L);

        for (SampleEvent event : allEvents) {
            if (event.getTime() >= windowStart) {
                timeWindowEvents.add(event);
            }
        }

        for (Map.Entry<String, SampleStats> entry : sampleStats.entrySet()) {
            SampleStats stats = entry.getValue();
            totalSamples += stats.totalSamples;
            totalSuccess += stats.successCount;
            totalFailure += stats.failureCount;
        }

        long overallSuccessRate = totalSamples > 0 ? (totalSuccess * 100) / totalSamples : 0;
        double actualRps = (double) totalSamples / analysisWindowSeconds;

        SLOAnalysisResult result = new SLOAnalysisResult();
        result.setTimestamp(Instant.now().toString());
        result.setTestSummary(totalSamples, overallSuccessRate);

        long criticalP99 = 0;
        List<SLOAnalysisResult.SLOEvaluation> evaluations = new ArrayList<>();

        for (Map.Entry<String, SampleStats> entry : sampleStats.entrySet()) {
            String label = entry.getKey();
            SampleStats stats = entry.getValue();

            Map<String, Long> percentiles = PercentileCalculator.calculateAll(stats.responseTimes);
            long p99 = percentiles.get("p99");

            if (p99 > criticalP99) criticalP99 = p99;

            SLOAnalysisResult.MetricSummary summary = new SLOAnalysisResult.MetricSummary();
            summary.setLabel(label);
            summary.setSamples(stats.totalSamples);
            summary.setSuccessRate((double) stats.getSuccessRate());
            summary.setAvgResponseTime(stats.getAvgResponseTime());
            summary.setPercentiles(percentiles);
            result.getSummary().put(label, summary);

            SLOAnalysisResult.SLOEvaluation evaluation = new SLOAnalysisResult.SLOEvaluation();
            evaluation.setLabel(label);
            evaluation.setMetric("p99 latency");
            evaluation.setTarget(500L);
            evaluation.setActual(p99);
            evaluation.setPass(p99 <= 500);
            evaluation.setSeverity(p99 > 1000 ? "critical" : p99 > 500 ? "warning" : "ok");
            evaluation.setAiHint("Consider optimizing database queries and adding caching layer");
            evaluations.add(evaluation);
        }

        SLOAnalysisResult.SLOEvaluation rpsEvaluation = new SLOAnalysisResult.SLOEvaluation();
        rpsEvaluation.setLabel("throughput");
        rpsEvaluation.setMetric("sustained RPS");
        rpsEvaluation.setTarget((double) targetRps);
        rpsEvaluation.setActual(actualRps);
        rpsEvaluation.setPass(actualRps >= targetRps);
        rpsEvaluation.setSeverity(actualRps < targetRps * 0.8 ? "critical" : actualRps < targetRps ? "warning" : "ok");
        rpsEvaluation.setAiHint("Scale horizontally by adding more application pods");
        evaluations.add(rpsEvaluation);

        SLOAnalysisResult.SLOEvaluation errorEvaluation = new SLOAnalysisResult.SLOEvaluation();
        errorEvaluation.setLabel("error_rate");
        errorEvaluation.setMetric("error rate");
        errorEvaluation.setTarget(99.0);
        errorEvaluation.setActual((double) overallSuccessRate);
        errorEvaluation.setPass(overallSuccessRate >= 99.0);
        errorEvaluation.setSeverity(overallSuccessRate < 95.0 ? "critical" : overallSuccessRate < 99.0 ? "warning" : "ok");
        errorEvaluation.setAiHint("Add circuit breakers and implement retry policies");
        evaluations.add(errorEvaluation);

        result.setEvaluations(evaluations);

        List<SLOAnalysisResult.AiSuggestion> suggestions = new ArrayList<>();
        if (overallSuccessRate < 99.0) {
            SLOAnalysisResult.AiSuggestion suggestion = new SLOAnalysisResult.AiSuggestion();
            suggestion.setPriority("high");
            suggestion.setTitle("Reduce error rate");
            suggestion.setDescription("Implement retry mechanisms and circuit breakers");
            suggestions.add(suggestion);
        }
        if (criticalP99 > 500) {
            SLOAnalysisResult.AiSuggestion suggestion = new SLOAnalysisResult.AiSuggestion();
            suggestion.setPriority("medium");
            suggestion.setTitle("Improve response latency");
            suggestion.setDescription("Add Redis caching and optimize database queries");
            suggestions.add(suggestion);
        }
        if (actualRps < targetRps) {
            SLOAnalysisResult.AiSuggestion suggestion = new SLOAnalysisResult.AiSuggestion();
            suggestion.setPriority("medium");
            suggestion.setTitle("Scale application resources");
            suggestion.setDescription("Increase pod replicas or upgrade instance types");
            suggestions.add(suggestion);
        }
        result.setAiSuggestions(suggestions);

        try {
            Path outputPath = Paths.get(outputDir, outputFile);
            String json = result.toJson();
            Files.writeString(outputPath, json);
            System.out.println("[PerfSage] Report written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("[PerfSage] Failed to write report: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[PerfSage] Analysis complete!");
    }

    // ============================================
    // SampleListener Implementation
    // ============================================

    @Override
    public void sampleStarted(SampleEvent e) {}

    @Override
    public void sampleStopped(SampleEvent e) {}

    @Override
    public void sampleOccurred(SampleEvent e) {}

    // ============================================
    // Lifecycle Methods (no-op for this implementation)
    // ============================================

    @Override
    public void teardownTest() {}

    @Override
    public void addSample(SampleResult result) {
        String label = result.getSampleLabel();
        long responseTime = result.getTime();
        boolean success = result.isSuccessful();
        String failureMessage = result.getResponseMessage();

        sampleStats.computeIfAbsent(label, k -> new SampleStats())
                .add(responseTime, success, failureMessage);
    }

    @Override
    public void testStarted(String host) {}

    @Override
    public void testEnded(String host) {}

    @Override
    public void testStarted() {}

    @Override
    public void testEnded() {}
}
