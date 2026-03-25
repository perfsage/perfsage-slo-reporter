package com.perfsage.jmeter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents an SLO (Service Level Objective) configuration.
 * Defines target metrics and thresholds for performance testing.
 */
public class SLOConfig {

    /** Name/ID of this SLO */
    @JsonProperty("name")
    private String name;

    /** Description of the SLO */
    @JsonProperty("description")
    private String description;

    /** SLOs list for this configuration */
    @JsonProperty("slos")
    private List<SLODefinition> slos;

    /** Optional: Target error budget percentage (0-100) */
    @JsonProperty("errorBudgetPercent")
    private Double errorBudgetPercent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SLODefinition> getSlos() {
        return slos;
    }

    public void setSlos(List<SLODefinition> slos) {
        this.slos = slos;
    }

    public Double getErrorBudgetPercent() {
        return errorBudgetPercent;
    }

    public void setErrorBudgetPercent(Double errorBudgetPercent) {
        this.errorBudgetPercent = errorBudgetPercent;
    }

    /**
     * A single SLO definition with metric type, target, and threshold.
     */
    public static class SLODefinition {

        /** SLO ID/name */
        @JsonProperty("id")
        private String id;

        /** Metric type: RESPONSE_TIME, THROUGHPUT, ERROR_RATE, SUCCESS_RATE */
        @JsonProperty("metricType")
        private String metricType;

        /** Optional: label or sampler name to filter */
        @JsonProperty("label")
        private String label;

        /** Comparison operator: LTE (<=), GTE (>=), EQ (=), LT (<), GT (>) */
        @JsonProperty("operator")
        private String operator;

        /** Target value */
        @JsonProperty("target")
        private Double target;

        /** Unit: ms, req/s, percent */
        @JsonProperty("unit")
        private String unit;

        /** Optional: percentile for response time (e.g., 95, 99) */
        @JsonProperty("percentile")
        private Integer percentile;

        /** Whether this SLO is critical */
        @JsonProperty("critical")
        private Boolean critical;

        /** AI-generated suggestion prompt hint */
        @JsonProperty("aiHint")
        private String aiHint;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMetricType() {
            return metricType;
        }

        public void setMetricType(String metricType) {
            this.metricType = metricType;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
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

        public Integer getPercentile() {
            return percentile;
        }

        public void setPercentile(Integer percentile) {
            this.percentile = percentile;
        }

        public Boolean getCritical() {
            return critical != null ? critical : false;
        }

        public void setCritical(Boolean critical) {
            this.critical = critical;
        }

        public String getAiHint() {
            return aiHint;
        }

        public void setAiHint(String aiHint) {
            this.aiHint = aiHint;
        }
    }
}
