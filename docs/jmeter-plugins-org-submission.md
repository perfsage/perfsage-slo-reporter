# Listing on jmeter-plugins.org (official catalog)

Third-party plugins are indexed in the [jmeter-plugins](https://github.com/undera/jmeter-plugins) repository. Community entries often live in [`site/dat/repo/various.json`](https://github.com/undera/jmeter-plugins/blob/master/site/dat/repo/various.json) (one JSON array; each element is a plugin descriptor).

## Before you open a PR

1. Publish a **GitHub Release** with a versioned fat JAR (this repo’s `.github/workflows/release.yml` does that when you push tag `v` + `project.version`).
2. Ensure the **`downloadUrl`** in your descriptor matches the release asset URL exactly.
3. Prefer a stable **`screenshotUrl`**: this project uses **`raw.githubusercontent.com`** so the image resolves without GitHub Pages.

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
  "screenshotUrl": "https://raw.githubusercontent.com/perfsage/perfsage-slo-reporter/main/blog/images/perfsage-slo-report-sample.png",
  "helpUrl": "https://github.com/perfsage/perfsage-slo-reporter/blob/main/README.md",
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

## Suggested PR to `undera/jmeter-plugins`

Use **base branch `master`**. Keep the same JSON formatting style as the rest of `various.json` (indentation/spacing) to reduce diff noise.

**Title**

```text
Add PerfSage SLO Reporter to various.json
```

**Description** (paste and adjust)

```markdown
Adds community plugin **perfsage-slo-reporter** to the catalog.

- **Repo:** https://github.com/perfsage/perfsage-slo-reporter
- **Release / JAR:** https://github.com/perfsage/perfsage-slo-reporter/releases/tag/v0.1.0
- **License:** MIT
- Descriptor matches [Plugin Repository Descriptor Format](https://jmeter-plugins.org/wiki/PluginRepositoryDescriptorFormat/).

`downloadUrl` points at the GitHub Release asset; `screenshotUrl` uses `raw.githubusercontent.com` for a stable image URL.
```

After your PR is merged upstream, users can install from the default Plugins Manager catalog without a custom `jpgc.repo.address` entry.

## Keep in sync with this repository

The canonical hosted descriptor for **custom** installs is [`repo/perfsage-plugins.json`](../repo/perfsage-plugins.json) on `main`. When you cut a new release, update **both** that file and your `various.json` entry (new key under `versions`, correct `downloadUrl`).

## Close your GitHub PR on `perfsage-slo-reporter` (if still open)

If `fixing-build-issues` (or another branch) was merged into `main` locally and pushed, the GitHub PR may still show as open:

1. Open the PR on GitHub.
2. If GitHub detected the merge, use **Close pull request** and comment *Merged via local fast-forward; `main` is up to date.*
3. Or use **Merge pull request** on GitHub if your `main` does not yet include the branch (avoid duplicate merges; compare `main` to the PR branch first).
