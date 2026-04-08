package com.perfsage.jmeter.analysis;

import com.perfsage.jmeter.SLOAnalysisResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleAnomalyDetectorTest {

    @Test
    void flagsTailLatencyWhenP99FarAboveAverage() {
        Map<String, SLOAnalysisResult.LabelMetrics> labels = new LinkedHashMap<>();
        SLOAnalysisResult.LabelMetrics m = new SLOAnalysisResult.LabelMetrics();
        m.setSuccessCount(100);
        m.setErrorCount(0);
        m.setAvgResponseTime(50.0);
        m.setP99ResponseTime(800L);
        m.setMaxResponseTime(900L);
        labels.put("slow-tail", m);

        List<SLOAnalysisResult.AnomalyFinding> findings =
                SimpleAnomalyDetector.analyze(labels, 500.0, 0.0);

        assertFalse(findings.isEmpty());
        assertTrue(findings.stream().anyMatch(f -> f.getCategory().equals("TAIL_LATENCY")));
    }

    @Test
    void flagsElevatedErrorsPerLabel() {
        Map<String, SLOAnalysisResult.LabelMetrics> labels = new LinkedHashMap<>();
        SLOAnalysisResult.LabelMetrics m = new SLOAnalysisResult.LabelMetrics();
        m.setSuccessCount(10);
        m.setErrorCount(5);
        m.setAvgResponseTime(100.0);
        m.setP99ResponseTime(200L);
        m.setMaxResponseTime(250L);
        labels.put("flaky", m);

        List<SLOAnalysisResult.AnomalyFinding> findings =
                SimpleAnomalyDetector.analyze(labels, 500.0, 0.0);

        assertTrue(findings.stream().anyMatch(f -> f.getCategory().equals("ERROR_BURST")));
    }
}
