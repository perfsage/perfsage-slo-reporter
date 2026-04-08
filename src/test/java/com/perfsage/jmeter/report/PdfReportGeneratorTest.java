package com.perfsage.jmeter.report;

import com.perfsage.jmeter.SLOAnalysisResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfReportGeneratorTest {

    @Test
    void writesNonEmptyPdf() throws Exception {
        SLOAnalysisResult result = new SLOAnalysisResult();
        result.setHintSource("static_catalog");
        result.setTotalSamples(3L);
        result.setTotalSuccess(3L);
        result.setTotalErrors(0L);
        result.setAllSlosPassed(true);

        Path pdf = Files.createTempFile("perfsage-", ".pdf");
        try {
            new PdfReportGenerator().write(result, pdf);
            assertTrue(Files.size(pdf) > 200);
        } finally {
            Files.deleteIfExists(pdf);
        }
    }
}
