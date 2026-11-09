package com.dts.result.adminreport;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminAnalyticsCsvTest {
    @Test void preventsFormulaInjectionAndEscapesCsv(){
        assertEquals("\"'=HYPERLINK()\"",AdminAnalyticsCsv.cell("=HYPERLINK()"));
        assertEquals("\"hello\"\"world\"",AdminAnalyticsCsv.cell("hello\"world"));
        assertFalse(AdminAnalyticsCsv.line("a\nb").contains("\na"));
        assertEquals((byte)0xEF,AdminAnalyticsCsv.encode("a")[0]);
    }
    @Test void aggregateExportContainsOnlyProvidedOwnerSummaries(){
        Map<String,Object> meta=Map.of("sourceService","dts-result","from","start","to","end","module","THEORY","examMode","ALL","dataStatus","READY","asOf","timestamp");
        Map<String,Object> totals=Map.of("completedAttempts",5,"passedAttempts",3,"failedAttempts",2,"activeLearners",4);
        String text=AdminAnalyticsCsv.overview(meta,totals,List.of(),List.of());
        assertTrue(text.contains("completedAttempts"));
        assertTrue(text.contains("\"5\""));
        assertFalse(text.contains("student_password"));
    }
}
