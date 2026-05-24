# Contributing to Cadence Java Samples

We'd love your help in making Cadence great. There are a variety of ways to contribute:

## Ways to Contribute

* **Try out Cadence** and share your feedback via [CNCF Slack](https://slack.cncf.io/) or [GitHub Issues](https://github.com/uber/cadence-java-samples/issues)
* **Fix or report bugs** by opening [GitHub Issues](https://github.com/uber/cadence-java-samples/issues) or submitting a Pull Request
* **Add new samples** that demonstrate Cadence features or common use cases
* **Improve documentation** by fixing typos, adding examples, or clarifying instructions
* **Share your Cadence success stories** with the community

## General Contributing Guide

Please review our shared [Cadence Contributing Guide](https://github.com/cadence-workflow/contributing) for:
* Code of Conduct
* Contributor License Agreement (CLA)
* General contribution guidelines
* Development workflow
* Testing requirements

## Development Setup

### Prerequisites

* Java 8 or later
* Gradle (included via wrapper)
* Docker and Docker Compose (for running Cadence Server locally)

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Code Style

This project uses Google Java Format. The build will automatically format your code, but you can also run:

```bash
./gradlew googleJavaFormat
```

## Adding New Samples

When adding new samples:

1. Place your sample code in the appropriate package under `src/main/java/com/uber/cadence/samples/`
2. Add corresponding unit tests in `src/test/java/com/uber/cadence/samples/`
3. Update the main [README.md](README.md) with instructions on how to run your sample
4. If your sample is complex, consider adding a dedicated README.md in the sample's directory

## Submitting Changes

1. Fork the repository
2. Create a feature branch from `master`
3. Make your changes following the code style guidelines
4. Add tests for your changes
5. Ensure all tests pass: `./gradlew test`
6. Commit your changes with a descriptive commit message
7. Push to your fork and submit a Pull Request

## Getting Help

If you have questions or need help:

* Join the `#cadence` channel on [CNCF Slack](https://slack.cncf.io/)
* Open a [GitHub Issue](https://github.com/uber/cadence-java-samples/issues)
* Check the [Cadence documentation](https://cadenceworkflow.io/docs/)

## License

By contributing to Cadence Java Samples, you agree that your contributions will be licensed under the [MIT License](LICENSE.txt).

