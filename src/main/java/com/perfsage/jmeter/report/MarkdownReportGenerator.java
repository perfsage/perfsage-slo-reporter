package com.perfsage.jmeter.report;

import com.perfsage.jmeter.SLOAnalysisResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Git-friendly Markdown summary of the same data as JSON/HTML.
 */
public class MarkdownReportGenerator {

    public void write(SLOAnalysisResult result, Path mdFile) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# PerfSage SLO Report\n\n");
        md.append("- **Hint source:** `").append(result.getHintSource() != null ? result.getHintSource() : "static_catalog")
                .append("` (bundled templates; no LLM API calls)\n");
        md.append("- **All SLOs passed:** ").append(Boolean.TRUE.equals(result.getAllSlosPassed())).append("\n\n");

        md.append("## Totals\n\n");
        md.append("| Metric | Value |\n|---|---|\n");
        md.append("| Samples | ").append(nz(result.getTotalSamples())).append(" |\n");
        md.append("| Success | ").append(nz(result.getTotalSuccess())).append(" |\n");
        md.append("| Errors | ").append(nz(result.getTotalErrors())).append(" |\n");
        md.append("| Avg RT (ms) | ")
                .append(result.getAggregateAvgResponseTime() != null
                        ? String.format("%.2f", result.getAggregateAvgResponseTime()) : "—")
                .append(" |\n");
        md.append("| Success rate | ")
                .append(result.getAggregateSuccessRate() != null
                        ? String.format("%.2f%%", result.getAggregateSuccessRate()) : "—")
                .append(" |\n\n");

        md.append("## SLO evaluations\n\n");
        md.append("| SLO | Metric | Target | Actual | Pass |\n|---|---|---:|---:|:---:|\n");
        if (result.getSloEvaluations() != null) {
            for (SLOAnalysisResult.SLOEvaluation ev : result.getSloEvaluations()) {
                md.append("| ")
                        .append(safe(ev.getSloId()))
                        .append(" | ")
                        .append(safe(ev.getMetricType()))
                        .append(" | ")
                        .append(ev.getTarget() != null ? ev.getTarget() : "—")
                        .append(" ")
                        .append(safe(ev.getUnit()))
                        .append(" | ")
                        .append(ev.getActualValue() != null ? String.format("%.2f", ev.getActualValue()) : "—")
                        .append(" | ")
                        .append(ev.isPassed() ? "yes" : "no")
                        .append(" |\n");
            }
        }
        md.append("\n");

        md.append("## Anomalies\n\n");
        if (result.getAnomalies() != null && !result.getAnomalies().isEmpty()) {
            for (SLOAnalysisResult.AnomalyFinding a : result.getAnomalies()) {
                md.append("- **").append(safe(a.getSeverity())).append("** (").append(safe(a.getCategory()))
                        .append("): ").append(safe(a.getMessage())).append("\n");
            }
        } else {
            md.append("_None flagged._\n");
        }
        md.append("\n## Suggestions\n\n");
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            for (String s : result.getSuggestions()) {
                md.append("- ").append(safe(s)).append("\n");
            }
        } else {
            md.append("_None._\n");
        }

        Files.createDirectories(mdFile.getParent() != null ? mdFile.getParent() : Path.of("."));
        Files.writeString(mdFile, md.toString(), StandardCharsets.UTF_8);
    }

    private static String nz(Long v) {
        return v == null ? "0" : v.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("|", "\\|").replace("\n", " ");
    }
}
