# Contributing to PerfSage SLO Reporter

Thank you for your interest in contributing to PerfSage SLO Reporter!

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Title**: Clear and descriptive summary
- **Environment**: Java version, JMeter version, OS
- **Steps to Reproduce**: Detailed reproduction steps
- **Expected vs Actual**: What you expected vs what happened
- **Logs**: Relevant error messages and stack traces

### Suggesting Features

Feature requests are welcome! Please include:

- **Use Case**: Why you need this feature
- **Proposed Solution**: How it should work
- **Alternatives**: Any alternative approaches you've considered

### Pull Requests

1. **Fork** the repository
2. **Create a branch** from `main`: `git checkout -b feature/your-feature`
3. **Make your changes** following the code style guidelines
4. **Add or update tests** for your changes
5. **Ensure CI passes**: All checks must pass before merging
6. **Open a Pull Request** with a clear description

## Development Setup

### Prerequisites

- JDK 17+
- Maven 3.8+
- JMeter 5.6+

### Build and Run

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/perfsage-slo-reporter.git
cd perfsage-slo-reporter

# Build the project
mvn clean package

# Run tests
mvn test

# Install to local JMeter
cp target/perfsage-slo-reporter-*.jar $JMETER_HOME/lib/ext/
```

## Code Style

- Follow the existing code formatting (4 spaces for indentation)
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `chore:` Build/config changes
- `refactor:` Code refactoring
- `test:` Test changes

Examples:
```
feat: add PDF report export format
fix: correct SLO threshold comparison for edge cases
docs: add setup guide for new users
chore: update Maven plugin versions
```

## Testing

### Unit Tests

Write unit tests for new functionality:

```java
@Test
void testSloEvaluation_passesWhenUnderThreshold() {
    // Given
    SLOEvaluation evaluation = new SLOEvaluation("Test", Unit.MS, "lte", 500.0);
    
    // When
    evaluation.evaluate(450.0);
    
    // Then
    assertTrue(evaluation.isPass());
}
```

### Integration Tests

For JMeter integration tests:

1. Create a test JMX file in `src/test/resources/`
2. Write a test that runs JMeter in non-GUI mode
3. Verify SLO reports are generated correctly

## Documentation

When adding new features, update:

- [README.md](README.md) - Overview and quick start
- [SETUP.md](SETUP.md) - Installation instructions
- [CONFIG.md](CONFIG.md) - Configuration reference
- [EXAMPLES.md](EXAMPLES.md) - Practical examples
- [ARCHITECTURE.md](ARCHITECTURE.md) - System design
- [CHANGELOG.md](CHANGELOG.md) - Version history

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

## Questions?

- Check existing [issues](https://github.com/perfsage/perfsage-slo-reporter/issues)
- Read the [documentation](https://perfsage.com/)
- Contact: hello@perfsage.com
