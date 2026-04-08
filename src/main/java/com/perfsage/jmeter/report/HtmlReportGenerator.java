package com.perfsage.jmeter.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfsage.jmeter.SLOAnalysisResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone HTML report with Chart.js (CDN). Open in a browser after the test — this is the primary
 * visual "UI" for headless {@code jmeter -n} runs.
 */
public class HtmlReportGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    public void write(SLOAnalysisResult result, Path htmlFile) throws IOException {
        Map<String, Object> chartPayload = buildChartPayload(result);
        String b64 = Base64.getEncoder().encodeToString(
                mapper.writeValueAsString(chartPayload).getBytes(StandardCharsets.UTF_8));

        String statusClass = Boolean.TRUE.equals(result.getAllSlosPassed()) ? "pass" : "fail";
        String statusText = Boolean.TRUE.equals(result.getAllSlosPassed()) ? "All SLO checks passed" : "Some SLO checks failed";

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>PerfSage SLO Report</title>
                  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
                  <style>
                    :root {
                      --bg: #0f1419;
                      --card: #1a2332;
                      --accent: #3d8bfd;
                      --pass: #3fb950;
                      --fail: #f85149;
                      --text: #e6edf3;
                      --muted: #8b949e;
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      font-family: ui-sans-serif, system-ui, Segoe UI, Roboto, Helvetica, Arial, sans-serif;
                      background: linear-gradient(165deg, #0a0e14 0%%, var(--bg) 40%%, #111820 100%%);
                      color: var(--text);
                      min-height: 100vh;
                    }
                    .wrap { max-width: 1100px; margin: 0 auto; padding: 2rem 1.25rem 3rem; }
                    header {
                      border-radius: 16px;
                      padding: 1.75rem 2rem;
                      background: radial-gradient(ellipse at top right, rgba(61,139,253,0.25), transparent 55%%), var(--card);
                      border: 1px solid rgba(255,255,255,0.06);
                      margin-bottom: 1.5rem;
                    }
                    h1 { margin: 0 0 0.35rem; font-size: 1.75rem; letter-spacing: -0.02em; }
                    .sub { color: var(--muted); font-size: 0.95rem; }
                    .badge {
                      display: inline-block;
                      margin-top: 1rem;
                      padding: 0.35rem 0.85rem;
                      border-radius: 999px;
                      font-weight: 600;
                      font-size: 0.85rem;
                    }
                    .badge.pass { background: rgba(63,185,80,0.15); color: var(--pass); }
                    .badge.fail { background: rgba(248,81,73,0.15); color: var(--fail); }
                    .grid {
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                      gap: 1rem;
                      margin-bottom: 1.5rem;
                    }
                    .card {
                      background: var(--card);
                      border-radius: 12px;
                      padding: 1.15rem 1.25rem;
                      border: 1px solid rgba(255,255,255,0.06);
                    }
                    .card h3 { margin: 0 0 0.5rem; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.08em; color: var(--muted); }
                    .card .num { font-size: 1.65rem; font-weight: 700; }
                    .charts {
                      display: grid;
                      grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
                      gap: 1.25rem;
                      margin-bottom: 1.5rem;
                      align-items: stretch;
                    }
                    @media (max-width: 900px) { .charts { grid-template-columns: 1fr; } }
                    .chart-box {
                      background: var(--card);
                      border-radius: 12px;
                      padding: 1.25rem 1.25rem 1rem;
                      border: 1px solid rgba(255,255,255,0.06);
                      display: flex;
                      flex-direction: column;
                      min-height: 0;
                    }
                    .chart-box h4 {
                      margin: 0 0 0.75rem;
                      font-size: 0.8rem;
                      font-weight: 600;
                      color: var(--text);
                      letter-spacing: 0.02em;
                    }
                    .chart-canvas-wrap {
                      position: relative;
                      flex: 1;
                      min-height: 300px;
                      width: 100%%;
                    }
                    .chart-canvas-wrap.tall { min-height: calc(80px + var(--n, 5) * 44px); }
                    .eval-scroll { overflow-x: auto; margin-top: 0.5rem; -webkit-overflow-scrolling: touch; }
                    table { width: 100%%; border-collapse: collapse; font-size: 0.9rem; table-layout: fixed; }
                    th, td { text-align: left; padding: 0.65rem 0.75rem; border-bottom: 1px solid rgba(255,255,255,0.08); vertical-align: top; }
                    th:nth-child(1), td:nth-child(1) { word-break: break-word; }
                    th:nth-child(2), td:nth-child(2) { width: 7.5rem; }
                    th:nth-child(3), td:nth-child(3) { width: 6.5rem; }
                    th:nth-child(4), td:nth-child(4) { width: 5rem; }
                    th:nth-child(5), td:nth-child(5) { width: 4.5rem; }
                    th { color: var(--muted); font-weight: 600; font-size: 0.75rem; text-transform: uppercase; }
                    tr:hover td { background: rgba(255,255,255,0.02); }
                    .ok { color: var(--pass); font-weight: 600; }
                    .bad { color: var(--fail); font-weight: 600; }
                    .hint-foot {
                      margin-top: 2rem;
                      padding: 1rem 1.25rem;
                      border-radius: 12px;
                      background: rgba(61,139,253,0.08);
                      border: 1px solid rgba(61,139,253,0.2);
                      font-size: 0.88rem;
                      color: var(--muted);
                    }
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <header>
                      <h1>PerfSage SLO Report</h1>
                      <p class="sub">End-of-test view — samples were evaluated when the run finished (Backend Listener).</p>
                      <span class="badge %s">%s</span>
                    </header>
                    <div class="grid">
                      <div class="card"><h3>Total samples</h3><div class="num">%s</div></div>
                      <div class="card"><h3>Success / errors</h3><div class="num">%s / %s</div></div>
                      <div class="card"><h3>Avg response (ms)</h3><div class="num">%s</div></div>
                      <div class="card"><h3>Success rate</h3><div class="num">%.2f%%</div></div>
                    </div>
                    <div class="charts">
                      <div class="chart-box" id="p99ChartBox">
                        <h4>p99 latency by sampler</h4>
                        <div class="chart-canvas-wrap tall" id="p99Wrap"><canvas id="p99Chart"></canvas></div>
                      </div>
                      <div class="chart-box">
                        <h4>Sample outcome mix</h4>
                        <div class="chart-canvas-wrap"><canvas id="mixChart"></canvas></div>
                      </div>
                    </div>
                    <div class="card" style="margin-bottom:1.5rem;">
                      <h3>SLO evaluations</h3>
                      <div class="eval-scroll">%s</div>
                    </div>
                    <div class="card" style="margin-bottom:1.5rem;">
                      <h3>Anomalies &amp; insights</h3>
                      %s
                    </div>
                    <div class="hint-foot">
                      <strong>About &quot;aiHint&quot; in JSON:</strong> hints are from PerfSage&apos;s bundled <code>static_catalog</code> — not GPT.
                      No API keys or BYOK are required today. LLM-backed hints can be added later as an optional integration.
                    </div>
                  </div>
                  <script>
                    const sloChartData = JSON.parse(atob('%s'));
                    const labelsShort = sloChartData.labelsShort || sloChartData.labels;
                    const labelsFull = sloChartData.labelsFull || sloChartData.labels;
                    const p99s = sloChartData.p99;
                    const passed = sloChartData.passedFlags;
                    const n = labelsShort.length;
                    document.documentElement.style.setProperty('--n', String(Math.max(n, 3)));
                    const barColors = passed.map(function (p) { return p ? 'rgba(63,185,80,0.85)' : 'rgba(248,81,73,0.85)'; });
                    new Chart(document.getElementById('p99Chart'), {
                      type: 'bar',
                      data: {
                        labels: labelsShort,
                        datasets: [{
                          label: 'p99 (ms)',
                          data: p99s,
                          backgroundColor: barColors,
                          borderRadius: 4,
                          barThickness: 'flex',
                          maxBarThickness: 28
                        }]
                      },
                      options: {
                        indexAxis: 'y',
                        responsive: true,
                        maintainAspectRatio: false,
                        interaction: { mode: 'nearest', axis: 'y', intersect: false },
                        plugins: {
                          legend: { display: false },
                          tooltip: {
                            backgroundColor: 'rgba(22,27,34,0.95)',
                            titleColor: '#e6edf3',
                            bodyColor: '#8b949e',
                            borderColor: 'rgba(255,255,255,0.1)',
                            borderWidth: 1,
                            callbacks: {
                              title: function (items) {
                                var i = items[0].dataIndex;
                                return labelsFull[i] || labelsShort[i];
                              },
                              label: function (ctx) {
                                return 'p99 latency: ' + ctx.raw + ' ms';
                              }
                            }
                          }
                        },
                        scales: {
                          x: {
                            title: { display: true, text: 'Milliseconds', color: '#8b949e', font: { size: 11 } },
                            ticks: { color: '#8b949e', padding: 8 },
                            grid: { color: 'rgba(255,255,255,0.06)' },
                            border: { display: false }
                          },
                          y: {
                            ticks: { color: '#8b949e', autoSkip: false, font: { size: 11 }, padding: 12 },
                            grid: { display: false },
                            border: { display: false }
                          }
                        },
                        layout: { padding: { left: 4, right: 12, top: 4, bottom: 8 } }
                      }
                    });
                    new Chart(document.getElementById('mixChart'), {
                      type: 'doughnut',
                      data: {
                        labels: ['Success', 'Errors'],
                        datasets: [{
                          data: [sloChartData.totalSuccess, sloChartData.totalErrors],
                          backgroundColor: ['rgba(63,185,80,0.85)', 'rgba(248,81,73,0.85)'],
                          borderWidth: 0
                        }]
                      },
                      options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        cutout: '58%%',
                        plugins: {
                          legend: {
                            position: 'bottom',
                            labels: { color: '#8b949e', padding: 16, boxWidth: 12, font: { size: 11 } }
                          },
                          tooltip: {
                            backgroundColor: 'rgba(22,27,34,0.95)',
                            titleColor: '#e6edf3',
                            bodyColor: '#8b949e'
                          }
                        },
                        layout: { padding: { top: 8, bottom: 12 } }
                      }
                    });
                  </script>
                </body>
                </html>
                """.formatted(
                statusClass,
                statusText,
                nz(result.getTotalSamples()),
                nz(result.getTotalSuccess()),
                nz(result.getTotalErrors()),
                result.getAggregateAvgResponseTime() != null
                        ? String.format("%.1f", result.getAggregateAvgResponseTime()) : "—",
                result.getAggregateSuccessRate() != null ? result.getAggregateSuccessRate() : 0.0,
                buildEvalTable(result),
                buildAnomalyHtml(result),
                b64
        );

        Files.createDirectories(htmlFile.getParent() != null ? htmlFile.getParent() : Path.of("."));
        Files.writeString(htmlFile, html, StandardCharsets.UTF_8);
    }

    private static String nz(Long v) {
        return v == null ? "0" : Long.toString(v);
    }

    private Map<String, Object> buildChartPayload(SLOAnalysisResult result) {
        List<String> labelsFull = new ArrayList<>();
        List<String> labelsShort = new ArrayList<>();
        List<Long> p99 = new ArrayList<>();
        List<Boolean> passedFlags = new ArrayList<>();

        if (result.getLabelMetrics() != null && !result.getLabelMetrics().isEmpty()) {
            for (Map.Entry<String, SLOAnalysisResult.LabelMetrics> e : result.getLabelMetrics().entrySet()) {
                String full = e.getKey();
                labelsFull.add(full);
                labelsShort.add(shortenLabel(full, 42));
                SLOAnalysisResult.LabelMetrics m = e.getValue();
                long p = m.getP99ResponseTime() != null ? m.getP99ResponseTime() : 0L;
                p99.add(p);
                String latencySloId = "p99_latency:" + full;
                boolean ok = result.getSloEvaluations() != null && result.getSloEvaluations().stream()
                        .filter(ev -> latencySloId.equals(ev.getSloId()))
                        .map(SLOAnalysisResult.SLOEvaluation::isPassed)
                        .findFirst()
                        .orElse(true);
                passedFlags.add(ok);
            }
        } else {
            labelsFull.add("(no samples)");
            labelsShort.add("(no samples)");
            p99.add(0L);
            passedFlags.add(true);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("labelsFull", labelsFull);
        map.put("labelsShort", labelsShort);
        map.put("labels", labelsShort);
        map.put("p99", p99);
        map.put("passedFlags", passedFlags);
        map.put("totalSuccess", result.getTotalSuccess() != null ? result.getTotalSuccess() : 0L);
        map.put("totalErrors", result.getTotalErrors() != null ? result.getTotalErrors() : 0L);
        return map;
    }

    private static String shortenLabel(String s, int maxLen) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String t = s.strip();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, Math.max(1, maxLen - 1)) + "\u2026";
    }

    private String buildEvalTable(SLOAnalysisResult result) {
        if (result.getSloEvaluations() == null || result.getSloEvaluations().isEmpty()) {
            return "<p class=\"sub\">No SLO rows.</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"slo-table\"><thead><tr><th>SLO</th><th>Metric</th><th>Target</th><th>Actual</th><th>Status</th></tr></thead><tbody>");
        for (SLOAnalysisResult.SLOEvaluation ev : result.getSloEvaluations()) {
            String st = ev.isPassed() ? "<span class=\"ok\">PASS</span>" : "<span class=\"bad\">FAIL</span>";
            sb.append("<tr><td>")
                    .append(escape(ev.getSloId()))
                    .append("</td><td>")
                    .append(escape(ev.getMetricType()))
                    .append("</td><td>")
                    .append(ev.getTarget() != null ? escape(String.valueOf(ev.getTarget())) : "—")
                    .append(" ")
                    .append(escape(ev.getUnit() != null ? ev.getUnit() : ""))
                    .append("</td><td>")
                    .append(ev.getActualValue() != null ? escape(String.format("%.2f", ev.getActualValue())) : "—")
                    .append("</td><td>")
                    .append(st)
                    .append("</td></tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String buildAnomalyHtml(SLOAnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.getAnomalies() != null && !result.getAnomalies().isEmpty()) {
            sb.append("<ul style=\"margin:0.5rem 0 0 1.1rem;color:#e6edf3;\">");
            for (SLOAnalysisResult.AnomalyFinding a : result.getAnomalies()) {
                sb.append("<li style=\"margin-bottom:0.35rem;\"><strong>")
                        .append(escape(a.getSeverity()))
                        .append("</strong> — ")
                        .append(escape(a.getMessage()))
                        .append("</li>");
            }
            sb.append("</ul>");
        } else {
            sb.append("<p class=\"sub\">No statistical anomalies flagged for this run.</p>");
        }
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            sb.append("<p style=\"margin-top:1rem;color:var(--muted);\"><strong>Suggestions:</strong></p><ul>");
            for (String s : result.getSuggestions()) {
                sb.append("<li>").append(escape(s)).append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
