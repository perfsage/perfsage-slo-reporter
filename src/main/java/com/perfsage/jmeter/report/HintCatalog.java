package com.perfsage.jmeter.report;

/**
 * Bundled remediation hints for reports. These are static templates — no calls to OpenAI,
 * Azure, or other LLM APIs. A future BYOK (bring-your-own-key) path can replace or augment these.
 */
public final class HintCatalog {

    public static final String SOURCE_STATIC = "static_catalog";

    private HintCatalog() {
    }

    public static String latencyHint(long p99Ms, double thresholdMs, boolean passed) {
        if (passed) {
            return String.format("p99 (%d ms) is within the %d ms budget; keep an eye on regressions in CI.",
                    p99Ms, Math.round(thresholdMs));
        }
        if (p99Ms > thresholdMs * 2) {
            return "Latency is far above budget: check backends, DB, and caching before blaming the network.";
        }
        return "Slightly over latency budget: profile slow transactions and validate thread pool / pool sizing.";
    }

    public static String throughputHint(double actualRps, double targetRps, boolean passed) {
        if (passed) {
            return String.format("Observed ~%.1f req/s vs floor %.1f req/s (using listener window math).",
                    actualRps, targetRps);
        }
        return "Throughput check missed: tune thread count, ramp, or window parameters so the metric matches reality.";
    }

    public static String availabilityHint(double successRatePercent, double targetPercent, boolean passed) {
        if (passed) {
            return String.format("Success rate %.2f%% meets the %.0f%% target.", successRatePercent, targetPercent);
        }
        return "Success rate below target: verify assertions, timeouts, and dependency health.";
    }
}
