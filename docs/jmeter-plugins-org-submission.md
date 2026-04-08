# Listing on jmeter-plugins.org (official catalog)

Third-party plugins are indexed in the [jmeter-plugins](https://github.com/undera/jmeter-plugins) repository. Community entries often live in [`site/dat/repo/various.json`](https://github.com/undera/jmeter-plugins/blob/master/site/dat/repo/various.json) (one JSON array; each element is a plugin descriptor).

## Before you open a PR

1. Publish a **GitHub Release** with a versioned fat JAR (this repo’s `.github/workflows/release.yml` does that when you push tag `v` + `project.version`).
2. Ensure the **`downloadUrl`** in your descriptor matches the release asset URL exactly.
3. Prefer a stable **`screenshotUrl`** (for example the GitHub Pages asset this project deploys, or `raw.githubusercontent.com`).

## Steps

1. Fork [undera/jmeter-plugins](https://github.com/undera/jmeter-plugins).
2. Open `site/dat/repo/various.json` and insert **one new object** into the top-level array (add a comma after the preceding `}` if needed).
3. Use the same field names as in [Plugin Repository Descriptor Format](https://jmeter-plugins.org/wiki/PluginRepositoryDescriptorFormat/).
4. Run JSON validation locally (`jq empty site/dat/repo/various.json`).
5. Open a PR against `master` with a short description and a link to this project’s README or releases.

## Snippet to merge (copy fields; verify URLs after release)

The block below is a single plugin object. Paste it into the `various.json` array and fix commas.

```json
{
  "id": "perfsage-slo-reporter",
  "name": "PerfSage SLO Reporter",
  "description": "A JMeter plugin by PerfSage for SLO-based reporting and performance analysis.",
  "screenshotUrl": "https://perfsage.github.io/perfsage-slo-reporter/assets/perfsage-slo-reporter-screenshot.png",
  "helpUrl": "https://perfsage.com/docs/perfsage-slo-reporter",
  "vendor": "perfsage.com",
  "markerClass": "com.perfsage.jmeter.SLOAnalysisListener",
  "componentClasses": [
    "com.perfsage.jmeter.SLOAnalysisListener",
    "com.perfsage.jmeter.SLOReporter"
  ],
  "versions": {
    "0.1.0": {
      "changes": "Initial public release",
      "downloadUrl": "https://github.com/perfsage/perfsage-slo-reporter/releases/download/v0.1.0/perfsage-slo-reporter-0.1.0.jar",
      "libs": {},
      "depends": []
    }
  }
}
```

Maintainers may request changes; follow their review.
