package com.perfsage.jmeter.analysis;

import com.perfsage.jmeter.SLOAnalysisResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phase-1 anomaly insights: heuristics on aggregated per-label metrics (no external ML services).
 */
public final class SimpleAnomalyDetector {

    private static final AtomicInteger ID = new AtomicInteger();

    private SimpleAnomalyDetector() {
    }

    public static List<SLOAnalysisResult.AnomalyFinding> analyze(
            Map<String, SLOAnalysisResult.LabelMetrics> labels,
            double latencyThresholdMs,
            double aggregateErrorRatePercent) {

        List<SLOAnalysisResult.AnomalyFinding> out = new ArrayList<>();
        if (labels == null || labels.isEmpty()) {
            return out;
        }

        for (Map.Entry<String, SLOAnalysisResult.LabelMetrics> e : labels.entrySet()) {
            String label = e.getKey();
            SLOAnalysisResult.LabelMetrics m = e.getValue();
            if (m == null) {
                continue;
            }

            int ok = m.getSuccessCount() != null ? m.getSuccessCount() : 0;
            int bad = m.getErrorCount() != null ? m.getErrorCount() : 0;
            int total = ok + bad;
            if (total > 0 && bad > 0) {
                double er = (bad * 100.0) / total;
                if (er >= 5.0 || bad >= 3) {
                    out.add(finding("ERROR_BURST", "HIGH",
                            String.format("Label \"%s\" error rate %.1f%% (%d/%d samples failed).",
                                    label, er, bad, total),
                            label));
                }
            }

            Long p99 = m.getP99ResponseTime();
            Double avg = m.getAvgResponseTime();
            if (p99 != null && avg != null && avg > 0 && p99 > avg * 3) {
                out.add(finding("TAIL_LATENCY", "MEDIUM",
                        String.format("Label \"%s\" shows a heavy tail: p99 %d ms vs avg %.0f ms.",
                                label, p99, avg),
                        label));
            }

            if (p99 != null && p99 > latencyThresholdMs * 1.2) {
                out.add(finding("TAIL_LATENCY", p99 > latencyThresholdMs * 2 ? "HIGH" : "MEDIUM",
                        String.format("Label \"%s\" p99 %d ms exceeds latency budget (~%.0f ms).",
                                label, p99, latencyThresholdMs),
                        label));
            }

            Long max = m.getMaxResponseTime();
            if (max != null && p99 != null && max > p99 + 500) {
                out.add(finding("SPIKE", "LOW",
                        String.format("Label \"%s\" max %d ms is well above p99 %d ms (possible spikes).",
                                label, max, p99),
                        label));
            }
        }

        if (aggregateErrorRatePercent > 1.0) {
            out.add(finding("GLOBAL_ERRORS", "HIGH",
                    String.format("Aggregate error rate %.2f%% across all labels.", aggregateErrorRatePercent),
                    null));
        }

        return out;
    }

    private static SLOAnalysisResult.AnomalyFinding finding(
            String category, String severity, String message, String label) {
        SLOAnalysisResult.AnomalyFinding f = new SLOAnalysisResult.AnomalyFinding();
        f.setId("anom-" + ID.incrementAndGet());
        f.setCategory(category);
        f.setSeverity(severity);
        f.setMessage(message);
        f.setRelatedLabel(label);
        return f;
    }
}
