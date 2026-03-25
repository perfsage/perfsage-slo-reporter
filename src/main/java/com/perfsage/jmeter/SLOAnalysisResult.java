package com.perfsage.jmeter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the analysis result produced by the SLO Reporter.
 * Contains computed metrics, SLO evaluations, and AI suggestions.
 * Written as a JSON report file.
 */
public class SLOAnalysisResult {

    /** Version of the analysis format */
    @JsonProperty("version")
    private String version = "1.0.0";

    /** Name of the SLO config used */
    @JsonProperty("configName")
    private String configName;

    /** Test end timestamp (Unix epoch ms) */
    @JsonProperty("testEndTime")
    private Long testEndTime;

    /** Total number of samples */
    @JsonProperty("totalSamples")
    private Long totalSamples;

    /** Total successful samples */
    @JsonProperty("totalSuccess")
    private Long totalSuccess;

    /** Total error samples */
    @JsonProperty("totalErrors")
    private Long totalErrors;

    /** Aggregate average response time (ms) */
    @JsonProperty("aggregateAvgResponseTime")
    private Double aggregateAvgResponseTime;

    /** Aggregate error rate (%) */
    @JsonProperty("aggregateErrorRate")
    private Double aggregateErrorRate;

    /** Aggregate success rate (%) */
    @JsonProperty("aggregateSuccessRate")
    private Double aggregateSuccessRate;

    /** Per-label metrics */
    @JsonProperty("labelMetrics")
    private Map<String, LabelMetrics> labelMetrics = new HashMap<>();

    /** SLO evaluations */
    @JsonProperty("sloEvaluations")
    private List<SLOEvaluation> sloEvaluations = new ArrayList<>();

    /** AI-generated suggestions */
    @JsonProperty("suggestions")
    private List<String> suggestions = new ArrayList<>();

    /** Whether all SLOs passed */
    @JsonProperty("allSlosPassed")
    private Boolean allSlosPassed;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Long getTestEndTime() {
        return testEndTime;
    }

    public void setTestEndTime(Long testEndTime) {
        this.testEndTime = testEndTime;
    }

    public Long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(Long totalSamples) {
        this.totalSamples = totalSamples;
    }

    public Long getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(Long totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public Long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public Double getAggregateAvgResponseTime() {
        return aggregateAvgResponseTime;
    }

    public void setAggregateAvgResponseTime(Double aggregateAvgResponseTime) {
        this.aggregateAvgResponseTime = aggregateAvgResponseTime;
    }

    public Double getAggregateErrorRate() {
        return aggregateErrorRate;
    }

    public void setAggregateErrorRate(Double aggregateErrorRate) {
        this.aggregateErrorRate = aggregateErrorRate;
    }

    public Double getAggregateSuccessRate() {
        return aggregateSuccessRate;
    }

    public void setAggregateSuccessRate(Double aggregateSuccessRate) {
        this.aggregateSuccessRate = aggregateSuccessRate;
    }

    public Map<String, LabelMetrics> getLabelMetrics() {
        return labelMetrics;
    }

    public void setLabelMetrics(Map<String, LabelMetrics> labelMetrics) {
        this.labelMetrics = labelMetrics;
    }

    public List<SLOEvaluation> getSloEvaluations() {
        return sloEvaluations;
    }

    public void setSloEvaluations(List<SLOEvaluation> sloEvaluations) {
        this.sloEvaluations = sloEvaluations;
        // Compute allSlosPassed
        if (sloEvaluations != null && !sloEvaluations.isEmpty()) {
            this.allSlosPassed = sloEvaluations.stream()
                    .noneMatch(e -> !e.isPassed());
        }
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean getAllSlosPassed() {
        return allSlosPassed;
    }

    public void setAllSlosPassed(Boolean allSlosPassed) {
        this.allSlosPassed = allSlosPassed;
    }

    /**
     * Metrics for a single label/sampler.
     */
    public static class LabelMetrics {

        @JsonProperty("successCount")
        private Integer successCount;

        @JsonProperty("errorCount")
        private Integer errorCount;

        @JsonProperty("avgResponseTime")
        private Double avgResponseTime;

        @JsonProperty("minResponseTime")
        private Long minResponseTime;

        @JsonProperty("maxResponseTime")
        private Long maxResponseTime;

        @JsonProperty("p90ResponseTime")
        private Long p90ResponseTime;

        @JsonProperty("p95ResponseTime")
        private Long p95ResponseTime;

        @JsonProperty("p99ResponseTime")
        private Long p99ResponseTime;

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Integer errorCount) {
            this.errorCount = errorCount;
        }

        public Double getAvgResponseTime() {
            return avgResponseTime;
        }

        public void setAvgResponseTime(Double avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
        }

        public Long getMinResponseTime() {
            return minResponseTime;
        }

        public void setMinResponseTime(Long minResponseTime) {
            this.minResponseTime = minResponseTime;
        }

        public Long getMaxResponseTime() {
            return maxResponseTime;
        }

        public void setMaxResponseTime(Long maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
        }

        public Long getP90ResponseTime() {
            return p90ResponseTime;
        }

        public void setP90ResponseTime(Long p90ResponseTime) {
            this.p90ResponseTime = p90ResponseTime;
        }

        public Long getP95ResponseTime() {
            return p95ResponseTime;
        }

        public void setP95ResponseTime(Long p95ResponseTime) {
            this.p95ResponseTime = p95ResponseTime;
        }

        public Long getP99ResponseTime() {
            return p99ResponseTime;
        }

        public void setP99ResponseTime(Long p99ResponseTime) {
            this.p99ResponseTime = p99ResponseTime;
        }
    }

    /**
     * Evaluation result for a single SLO.
     */
    public static class SLOEvaluation {

        @JsonProperty("sloId")
        private String sloId;

        @JsonProperty("metricType")
        private String metricType;

        @JsonProperty("target")
        private Double target;

        @JsonProperty("unit")
        private String unit;

        @JsonProperty("operator")
        private String operator;

        @JsonProperty("actualValue")
        private Double actualValue;

        @JsonProperty("passed")
        private Boolean passed;

        @JsonProperty("critical")
        private Boolean critical;

        @JsonProperty("skipped")
        private Boolean skipped;

        @JsonProperty("aiHint")
        private String aiHint;

        public String getSloId() {
            return sloId;
        }

        public void setSloId(String sloId) {
            this.sloId = sloId;
        }

        public String getMetricType() {
            return metricType;
        }

        public void setMetricType(String metricType) {
            this.metricType = metricType;
        }

        public Double getTarget() {
            return target;
        }

        public void setTarget(Double target) {
            this.target = target;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Double getActualValue() {
            return actualValue;
        }

        public void setActualValue(Double actualValue) {
            this.actualValue = actualValue;
        }

        public Boolean isPassed() {
            return passed != null ? passed : false;
        }

        public void setPassed(Boolean passed) {
            this.passed = passed;
        }

        public Boolean isCritical() {
            return critical != null ? critical : false;
        }

        public void setCritical(Boolean critical) {
            this.critical = critical;
        }

        public Boolean isSkipped() {
            return skipped != null ? skipped : false;
        }

        public void setSkipped(Boolean skipped) {
            this.skipped = skipped;
        }

        public String getAiHint() {
            return aiHint;
        }

        public void setAiHint(String aiHint) {
            this.aiHint = aiHint;
        }
    }
}
