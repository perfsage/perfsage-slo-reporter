package com.perfsage.jmeter.report;

import com.perfsage.jmeter.SLOAnalysisResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlReportGeneratorTest {

    @Test
    void writesSelfContainedHtmlWithChartAndSummary() throws Exception {
        SLOAnalysisResult result = new SLOAnalysisResult();
        result.setConfigName("test");
        result.setTestEndTime(1_700_000_000_000L);
        result.setTotalSamples(10L);
        result.setTotalSuccess(10L);
        result.setTotalErrors(0L);
        result.setAggregateAvgResponseTime(42.0);
        result.setAggregateErrorRate(0.0);
        result.setAggregateSuccessRate(100.0);
        result.setHintSource("static_catalog");
        result.setSuggestions(List.of("Stay hydrated."));
        result.setAllSlosPassed(true);

        Path html = Files.createTempFile("perfsage-", ".html");
        try {
            new HtmlReportGenerator().write(result, html);
            String content = Files.readString(html);
            assertTrue(content.contains("PerfSage"));
            assertTrue(content.contains("chart.js") || content.contains("Chart"));
            assertTrue(content.contains("sloChartData"));
        } finally {
            Files.deleteIfExists(html);
        }
    }
}
