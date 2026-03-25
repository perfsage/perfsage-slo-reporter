# Publishing to Maven Central

This guide explains how to publish PerfSage SLO Reporter to Maven Central (Sonatype OSSRH).

## Prerequisites

### 1. Sonatype OSSRH Account

1. Create an account at [https://issues.sonatype.org](https://issues.sonatype.org)
2. Create a JIRA ticket to claim the `com.perfsage` group ID
3. Wait for OSSRH to approve your group ID

### 2. GPG Key

Generate a GPG key for signing artifacts:

```bash
# Generate a new key
gpg --full-generate-key

# List your keys to find the key ID
gpg --list-keys

# Export the private key (for GitHub Secrets)
gpg --armor --export-secret-keys YOUR_KEY_ID > private.key

# Get the passphrase you used
echo "Your passphrase is: YOUR_PASSPHRASE"
```

### 3. Configure pom.xml

The `pom.xml` already includes the `publish` profile for Maven Central:

```xml
<profile>
  <id>publish</id>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <version>3.3.1</version>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals>
              <goal>jar-no-fork</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>3.11.2</version>
        <executions>
          <execution>
            <id>attach-javadocs</id>
            <goals>
              <goal>jar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-gpg-plugin</artifactId>
        <version>3.2.7</version>
        <executions>
          <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals>
              <goal>sign</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.sonatype.plugins</groupId>
        <artifactId>nexus-staging-maven-plugin</artifactId>
        <version>1.7.0</version>
        <extensions>true</extensions>
        <configuration>
          <serverId>ossrh</serverId>
          <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
          <autoReleaseAfterClose>true</autoReleaseAfterClose>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>
```

## Local Publishing (for testing)

### Step 1: Set up Maven settings.xml

Edit `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
</settings>
```

### Step 2: Run the publish profile

```bash
mvn -P publish deploy
```

This will:
1. Build the project
2. Generate source and javadoc JARs
3. Sign artifacts with GPG
4. Upload to OSSRH staging repository

## GitHub Actions Publishing

The project includes `.github/workflows/publish.yml` which automatically publishes on GitHub release.

### Required Repository Secrets

Set up the following secrets in GitHub repository settings:

| Secret Name | Description |
|-------------|-------------|
| `OSSRH_USERNAME` | Sonatype OSSRH username |
| `OSSRH_PASSWORD` | Sonatype OSSRH token |
| `GPG_PRIVATE_KEY` | GPG private key (armor format) |
| `GPG_PASSPHRASE` | GPG key passphrase |

### To Add Secrets

1. Go to `Settings` → `Secrets and variables` → `Actions`
2. Click `New repository secret`
3. Add each secret listed above

## Release Workflow

1. **Update version in pom.xml**:
   ```bash
   # Change from SNAPSHOT to release version
   mvn versions:set -DnewVersion=1.0.0
   ```

2. **Commit and tag**:
   ```bash
   git add .
   git commit -m "Release version 1.0.0"
   git tag v1.0.0
   git push origin main v1.0.0
   ```

3. **Create GitHub Release**:
   - Go to Releases page
   - Click "Create a new release"
   - Tag version: `v1.0.0`
   - Write release notes
   - Click "Publish release"

4. **CI will automatically publish** to Maven Central via the `publish.yml` workflow.

## Verify on Maven Central

After release, verify on:
- [Maven Central Search](https://search.maven.org/search?q=com.perfsage)
- [Sonatype Staging](https://s01.oss.sonatype.org/#nexus-search;quick~perfsage)

## Notes

- The artifact will appear on Maven Central within 2-24 hours after release
- Source JAR and Javadoc JAR are automatically published alongside the main artifact
- GPG signing is required for all OSSRH uploads
