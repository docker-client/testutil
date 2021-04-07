[![Build Status](https://img.shields.io/github/workflow/status/docker-client/testutil/Publish?style=for-the-badge)](https://github.com/docker-client/testutil/actions)
[![Maven Central](https://img.shields.io/maven-central/v/de.gesellix/testutil.svg?style=for-the-badge&maxAge=86400)](https://search.maven.org/search?q=g:de.gesellix%20AND%20a:testutil)

# testutil

Utilities used in tests of the Docker-Client libraries

## Release Workflow

There are multiple GitHub Action Workflows for the different steps in the package's lifecycle:

- CI: Builds and checks incoming changes on a pull request
  - triggered on every push to a non-default branch
- CD: Publishes the Gradle artifacts to GitHub Package Registry
  - triggered only on pushes to the default branch
- Release: Publishes Gradle artifacts to Sonatype and releases them to Maven Central
  - triggered on a published GitHub release using the underlying tag as artifact version, e.g. via `git tag -m "$MESSAGE" v$(date +"%Y-%m-%dT%H-%M-%S")`
