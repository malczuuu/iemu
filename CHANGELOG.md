# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog][keepachangelog], and this project adheres to [Semantic Versioning][semver].

## [Unreleased]

### Added

- Add light/dark theme toggle button on web UI.
- Add LwM2M connect/disconnect options from browser.

### Changed

- Transition app from Java to Kotlin, refactor insides to simplify codebase.
- Upgrade Javalin to `7.x`.
- Change CLI from `--{profileName}` to `--profile {profileName}`.
- Upgrade Angular to `22.x`, along with project dependencies.
- Update Bulma CSS to `1.x` and modernize UI.

### Fixed

- Fix main page alignment to match full width of a card on web UI.

## [1.1.0] - 2025-12-31

## Added

- Introduce Docker image on Docker Hub - [`malczuuu/iemu`](https://hub.docker.com/r/malczuuu/iemu).

## Changed

- Upgrade Java version from 8 to 17, along with Gradle build tool and project dependencies.
- Upgrade Angular (in `webapp/`) from 12 to 21, along with project dependencies.

## [1.0.1] - 2021-03-14

### Fixed

- Fix `Dockerfile` to use latest versions, matching repository configuration.

## [1.0.0] - 2021-03-14

Initial version of iEmu application.

## Added

- Add LwM2M protocol support for objects:
  * `LwM2M Server (1)`,
  * `Device (3)`,
  * `Firmware Update (5)`,
  * `Light Control (3311)`. 
- Setup with configuration profile files with program arguments.
- Add HTTP UI for device management (REST API and Angular UI). 
- Add Docker image deployment.

[keepachangelog]: https://keepachangelog.com/en/1.1.0/

[semver]: https://semver.org/spec/v2.0.0.html
