# iEmu - IoT Device Emulator

[![Gradle Build](https://github.com/malczuuu/iemu/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/malczuuu/iemu/actions/workflows/gradle-build.yml)
[![WebApp Build](https://github.com/malczuuu/iemu/actions/workflows/webapp-build.yml/badge.svg)](https://github.com/malczuuu/iemu/actions/workflows/webapp-build.yml)
[![DockerHub](https://img.shields.io/docker/v/malczuuu/iemu?logo=docker&label=DockerHub)](https://hub.docker.com/r/malczuuu/iemu)
[![License](https://img.shields.io/github/license/malczuuu/iemu)](https://github.com/malczuuu/iemu/blob/main/LICENSE)

Emulator application for an IoT device (with LwM2M).

## Table of Contents

- [Features](#features)
- [Application](#application)
- [Configuration Profiles](#configuration-profiles)
- [Run with Docker](#run-with-docker)
- [Technologies](#technologies)
- [Repository](#repository)
- [Build from source](#build-from-source)

## Features

1. LwM2M protocol support for objects:
    * `LwM2M Server (1)`,
    * `Device (3)`,
    * `Firmware Update (5)`,
    * `Light Control (3311)`.
2. HTTP UI for device management (REST API and Angular UI).
3. Docker build.

## Application

Application has just one view, displaying (refreshing automatically thanks to websocket connection) management UI for
the device.

<div align="center">
  <img src="docs/img/webapp.png" />
</div>

## Configuration Profiles

It's possible to define multiple `.yml` files within `data/` directory and control profile name with `--{profile}`
program argument.

Consider following configuration files:

```bash
$ tree data/
data
├── config.yml
└── config-demo.yml
```

Then selecting profile name looks following:

| config file       | profile    |
|-------------------|------------|
| `config.yml`      |            |
| `config-demo.yml` | `--demo`   |

## Run with Docker

### Prerequisites

1. Run LwM2M server (consider Leshan Demo Server from [official repository][leshan] or check [here][leshan-demo-server]
   on how to use Leshan's playground).
2. Set appropriate configuration in `data/config.yml` (see `data/config-demo.yml` if using Leshan's playground). See
   also chapter about [configuration profiles](#configuration-profiles).

No need to compile Angular, as static dist files from [`webapp`](./webapp) are included as static resources in `jar`.

### Start the container

You can use pre-built Docker image - [`malczuuu/iemu`](https://hub.docker.com/r/malczuuu/iemu). Consider checking for
most recent image tags.

```
docker run --rm -p 4500:4500 malczuuu/iemu
```

To run application with `demo` profile, use following command:

```
docker run --rm --env APP_ARGS=--demo -p 4500:4500 malczuuu/iemu
```

To mount your own profiles, use `/data` volume. Predefined profiles are available in [`data/`](./data) directory for
reference.

## Technologies

- [Javalin](https://javalin.io)
- [Leshan](https://github.com/eclipse/leshan)
- [Angular](https://angular.io)
- [Docker](https://www.docker.com/)

## Repository

The repository is a monorepo:

- root project is a Java backend (Gradle project),
- `webapp/` contains Angular frontend.

## Build from source

<details>
<summary><b>Expand...</b></summary>

Project uses **Java 17**, mainly for compatibility reasons. You can change Java version in `build.gradle.kts` - project
uses **Foojay** plugin to automatically resolve and download JDKs (see `settings.gradle.kts`). Minimal version for
**Gradle 9+** is also **Java 17**.

### Run with IDE

Simply locate [`App`][app.java] class and run it's `main` method. Setting profile parameter depends on your IDE, but it
should be somewhere named "Program Arguments" (not "JVM Options").

### Run with Gradle `application` plugin

Project uses Gradle [`application`][application-plugin] plugin. To run application from Gradle, simply use `run` task.

```bash
./gradlew run
```

To run application with `demo` profile, use following command.

```bash
./gradlew run --args="--demo"
```

Then, browse [client UI](http://localhost:4500/) or check in your LwM2M server if the client is connected.

### Build and run distribution

Run `install` task to produce `build/install/` output. This project does not produce a fat jar, it relies on Gradle
`application` plugin only.

```bash
./gradlew install
```

Then, simply call `iemu` executable script from `bin/` directory. It launches application using jars from `lib/`
directory.

```bash
./build/install/iemu/bin/iemu
```

To run application with `demo` profile, use following command.

```bash
./build/install/iemu/bin/iemu --demo
```

For more info about see [Building the distribution][the-distribution] chapter.

</details>

[app.java]: ./src/main/java/io/github/malczuuu/iemu/App.java

[leshan]: https://github.com/eclipse/leshan

[leshan-demo-server]: https://leshan.eclipseprojects.io

[application-plugin]: https://docs.gradle.org/current/userguide/application_plugin.html

[the-distribution]: https://docs.gradle.org/current/userguide/application_plugin.html#sec:the_distribution
