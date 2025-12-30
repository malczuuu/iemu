# iEmu - IoT Device Emulator

Emulator application for an IoT device (with LwM2M).

## Features

1. LwM2M protocol support for objects:
   * `LwM2M Server (1)`,
   * `Device (3)`,
   * `Firmware Update (5)`,
   * `Light Control (3311)`.
2. HTTP UI for device management (REST API and Angular UI).
3. Docker build.

## Build & Run

Project requires **Java 25**.

1. Use `build` task to compile project.
   ```bash
   ./gradlew build
   ```
2. Run LwM2M server (consider Leshan Demo Server from [official repository](https://github.com/eclipse/leshan) or check
   [here](https://leshan.eclipseprojects.io) on how to use Leshan's playground).
3. Set appropriate configuration in `data/config.yml` (see `data/config-demo.yml` if using Leshan's playground). See
   also chapter about [configuration profiles](#configuration-profiles).
4. Run `jar` application.
   ```bash
   java -jar build/libs/iemu-*.jar 
   ```
   To run application with `demo` profile, use following command.
   ```bash
   java -jar build/libs/iemu-*.jar --demo 
   ```
5. Browse [client UI](http://localhost:4500/) or check in your LwM2M server if the client is connected.

No need to compile Angular, as static dist files from [`webapp`](./webapp) are included as static resources in `jar`.  

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

| config file       | java exec                                |
|-------------------|------------------------------------------|
| `config.yml`      | `java -jar build/libs/iemu-*.jar`        |
| `config-demo.yml` | `java -jar build/libs/iemu-*.jar --demo` |

## Application

Application has just one view, displaying (refreshing automatically thanks to websocket connection) management UI for
the device.

<div align="center">
  <img src="docs/img/webapp.png" />
</div>

## Technologies

- [Javalin](https://javalin.io)
- [Leshan](https://github.com/eclipse/leshan)
- [Angular](https://angular.io)
- [Docker](https://www.docker.com/) (image is not yet being published)
