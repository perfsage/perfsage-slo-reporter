package com.perfsage.jmeter.report;

import com.perfsage.jmeter.SLOAnalysisResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Compact one-page PDF summary for attaching to tickets or CI artifacts.
 */
public class PdfReportGenerator {

    public void write(SLOAnalysisResult result, Path pdfFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float y = page.getMediaBox().getHeight() - 56;
            float x = 48;
            float leading = 16;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText("PerfSage SLO Report");
                cs.endText();
                y -= 28;

                cs.setFont(PDType1Font.HELVETICA, 11);
                y = textLine(cs, x, y, leading, "Hint source: "
                        + (result.getHintSource() != null ? result.getHintSource() : "static_catalog")
                        + " (no LLM API)");
                y = textLine(cs, x, y, leading, "All SLOs passed: " + Boolean.TRUE.equals(result.getAllSlosPassed()));
                y -= 8;
                y = textLine(cs, x, y, leading, "Samples: " + nz(result.getTotalSamples())
                        + "  Success: " + nz(result.getTotalSuccess())
                        + "  Errors: " + nz(result.getTotalErrors()));
                if (result.getAggregateAvgResponseTime() != null) {
                    y = textLine(cs, x, y, leading, String.format("Avg response: %.2f ms", result.getAggregateAvgResponseTime()));
                }
                if (result.getAggregateSuccessRate() != null) {
                    y = textLine(cs, x, y, leading, String.format("Success rate: %.2f%%", result.getAggregateSuccessRate()));
                }
                y -= 12;
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = textLine(cs, x, y, leading, "SLO checks");
                cs.setFont(PDType1Font.HELVETICA, 10);
                List<SLOAnalysisResult.SLOEvaluation> evs = result.getSloEvaluations();
                if (evs != null) {
                    for (SLOAnalysisResult.SLOEvaluation ev : evs) {
                        String line = trim(ev.getSloId(), 72) + " -> " + (ev.isPassed() ? "PASS" : "FAIL");
                        y = textLine(cs, x, y, leading, line);
                        if (y < 80) {
                            break;
                        }
                    }
                }
                y -= 8;
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                y = textLine(cs, x, y, leading, "Anomalies");
                cs.setFont(PDType1Font.HELVETICA, 10);
                List<SLOAnalysisResult.AnomalyFinding> an = result.getAnomalies();
                if (an != null && !an.isEmpty()) {
                    for (SLOAnalysisResult.AnomalyFinding a : an) {
                        y = textLine(cs, x, y, leading, trim(a.getMessage(), 95));
                        if (y < 56) {
                            break;
                        }
                    }
                } else {
                    y = textLine(cs, x, y, leading, "None flagged.");
                }
            }
            if (pdfFile.getParent() != null) {
                Files.createDirectories(pdfFile.getParent());
            }
            doc.save(pdfFile.toFile());
        }
    }

    private static float textLine(PDPageContentStream cs, float x, float y, float leading, String text)
            throws IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(sanitizePdf(text));
        cs.endText();
        return y - leading;
    }

    private static String sanitizePdf(String s) {
        if (s == null) {
            return "";
        }
        return s.replace('\r', ' ').replace('\n', ' ');
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String nz(Long v) {
        return v == null ? "0" : v.toString();
    }
}
