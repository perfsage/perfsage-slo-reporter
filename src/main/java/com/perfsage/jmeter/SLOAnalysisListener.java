package com.perfsage.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JMeter Backend Listener implementation that collects sample results
 * and performs SLO analysis at the end of the test.
 * Outputs a JSON report file containing analysis metrics.
 */
public class SLOAnalysisListener implements BackendListenerClient {

    private static final String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir");
    private static final String DEFAULT_OUTPUT_FILE = "slo-report.json";

    private final Map<String, SampleStats> sampleStats = new ConcurrentHashMap<>();
    private final Map<String, Integer> failureReasons = new ConcurrentHashMap<>();
    private final List<SampleResult> allResults = Collections.synchronizedList(new ArrayList<>());

    private String outputDir;
    private String outputFile;
    private int targetRps;
    private boolean generateHtml;
    private int percentileThreshold;
    private int analysisWindowSeconds;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static class SampleStats {
        long totalSamples = 0;
        long successCount = 0;
        long failureCount = 0;
        long totalResponseTime = 0;
        long maxResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        List<Long> responseTimes = new ArrayList<>();

        synchronized void add(long responseTime, boolean success) {
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
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("outputDir", DEFAULT_OUTPUT_DIR);
        args.addArgument("outputFile", DEFAULT_OUTPUT_FILE);
        args.addArgument("targetRps", "100");
        args.addArgument("generateHtml", "false");
        args.addArgument("percentileThreshold", "99");
        args.addArgument("analysisWindowSeconds", "60");
        return args;
    }

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
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        for (SampleResult result : sampleResults) {
            allResults.add(result);

            String label = result.getSampleLabel();
            long responseTime = result.getTime();
            boolean success = result.isSuccessful();

            sampleStats.computeIfAbsent(label, k -> new SampleStats())
                    .add(responseTime, success);

            String failureMessage = result.getResponseMessage();
            if (!success && failureMessage != null && !failureMessage.isBlank()) {
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

        for (Map.Entry<String, SampleStats> entry : sampleStats.entrySet()) {
            SampleStats stats = entry.getValue();
            totalSamples += stats.totalSamples;
            totalSuccess += stats.successCount;
            totalFailure += stats.failureCount;
        }

        double overallSuccessRate = totalSamples > 0 ? (totalSuccess * 100.0) / totalSamples : 0.0;
        double overallErrorRate = totalSamples > 0 ? (totalFailure * 100.0) / totalSamples : 0.0;
        double actualRps = (double) totalSamples / analysisWindowSeconds;

        SLOAnalysisResult result = new SLOAnalysisResult();
        result.setConfigName("BackendListenerContext");
        result.setTestEndTime(System.currentTimeMillis());
        result.setTotalSamples(totalSamples);
        result.setTotalSuccess(totalSuccess);
        result.setTotalErrors(totalFailure);
        result.setAggregateSuccessRate(overallSuccessRate);
        result.setAggregateErrorRate(overallErrorRate);
        result.setAggregateAvgResponseTime(computeAggregateAvgResponseTime());

        long criticalP99 = 0;
        List<SLOAnalysisResult.SLOEvaluation> evaluations = new ArrayList<>();
        Map<String, SLOAnalysisResult.LabelMetrics> labelMetrics = new LinkedHashMap<>();

        for (Map.Entry<String, SampleStats> entry : sampleStats.entrySet()) {
            String label = entry.getKey();
            SampleStats stats = entry.getValue();

            Map<String, Long> percentiles = PercentileCalculator.calculateAll(stats.responseTimes);
            long p99 = percentiles.get("p99");

            if (p99 > criticalP99) criticalP99 = p99;

            SLOAnalysisResult.LabelMetrics metrics = new SLOAnalysisResult.LabelMetrics();
            metrics.setSuccessCount((int) stats.successCount);
            metrics.setErrorCount((int) stats.failureCount);
            metrics.setAvgResponseTime((double) stats.getAvgResponseTime());
            metrics.setMinResponseTime(stats.minResponseTime == Long.MAX_VALUE ? 0L : stats.minResponseTime);
            metrics.setMaxResponseTime(stats.maxResponseTime);
            metrics.setP90ResponseTime(percentiles.get("p90"));
            metrics.setP95ResponseTime(percentiles.get("p95"));
            metrics.setP99ResponseTime(percentiles.get("p99"));
            labelMetrics.put(label, metrics);

            SLOAnalysisResult.SLOEvaluation latencyEval = new SLOAnalysisResult.SLOEvaluation();
            latencyEval.setSloId("p99_latency:" + label);
            latencyEval.setMetricType("RESPONSE_TIME");
            latencyEval.setOperator("LTE");
            latencyEval.setUnit("ms");
            latencyEval.setTarget(500.0);
            latencyEval.setActualValue((double) p99);
            latencyEval.setPassed(p99 <= 500);
            latencyEval.setCritical(p99 > 1000);
            latencyEval.setSkipped(false);
            latencyEval.setAiHint("Consider optimizing database queries and adding caching layer");
            evaluations.add(latencyEval);
        }

        SLOAnalysisResult.SLOEvaluation rpsEvaluation = new SLOAnalysisResult.SLOEvaluation();
        rpsEvaluation.setSloId("throughput:rps");
        rpsEvaluation.setMetricType("THROUGHPUT");
        rpsEvaluation.setOperator("GTE");
        rpsEvaluation.setUnit("req/s");
        rpsEvaluation.setTarget((double) targetRps);
        rpsEvaluation.setActualValue(actualRps);
        rpsEvaluation.setPassed(actualRps >= targetRps);
        rpsEvaluation.setCritical(actualRps < targetRps * 0.8);
        rpsEvaluation.setSkipped(false);
        rpsEvaluation.setAiHint("Scale horizontally by adding more application pods");
        evaluations.add(rpsEvaluation);

        SLOAnalysisResult.SLOEvaluation errorEvaluation = new SLOAnalysisResult.SLOEvaluation();
        errorEvaluation.setSloId("availability:success_rate");
        errorEvaluation.setMetricType("SUCCESS_RATE");
        errorEvaluation.setOperator("GTE");
        errorEvaluation.setUnit("percent");
        errorEvaluation.setTarget(99.0);
        errorEvaluation.setActualValue(overallSuccessRate);
        errorEvaluation.setPassed(overallSuccessRate >= 99.0);
        errorEvaluation.setCritical(overallSuccessRate < 95.0);
        errorEvaluation.setSkipped(false);
        errorEvaluation.setAiHint("Add circuit breakers and implement retry policies");
        evaluations.add(errorEvaluation);

        result.setLabelMetrics(labelMetrics);
        result.setSloEvaluations(evaluations);

        List<String> suggestions = new ArrayList<>();
        if (overallSuccessRate < 99.0) {
            suggestions.add("[HIGH] Reduce error rate: implement retry mechanisms and circuit breakers");
        }
        if (criticalP99 > 500) {
            suggestions.add("[MEDIUM] Improve response latency: add caching and optimize database queries");
        }
        if (actualRps < targetRps) {
            suggestions.add("[MEDIUM] Scale application resources: increase replicas or upgrade instance types");
        }
        if (!failureReasons.isEmpty()) {
            suggestions.add("[INFO] Top failure reasons: " + summarizeFailureReasons());
        }
        result.setSuggestions(suggestions);

        try {
            Path outputPath = Paths.get(outputDir, outputFile);
            Files.createDirectories(outputPath.getParent() != null ? outputPath.getParent() : Paths.get(DEFAULT_OUTPUT_DIR));
            objectMapper.writeValue(outputPath.toFile(), result);
            System.out.println("[PerfSage] Report written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("[PerfSage] Failed to write report: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[PerfSage] Analysis complete!");
    }

    private double computeAggregateAvgResponseTime() {
        long totalTime = 0L;
        long count = 0L;
        synchronized (allResults) {
            for (SampleResult sr : allResults) {
                totalTime += sr.getTime();
                count++;
            }
        }
        return count > 0 ? (totalTime * 1.0) / count : 0.0;
    }

    private String summarizeFailureReasons() {
        return failureReasons.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}
