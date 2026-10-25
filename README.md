![MirraNET Logo](https://raw.githubusercontent.com/LinzN/Stem/master/stem-core/src/main/resources/header-logo.png)

# system-chain

> Host monitoring for [STEM](https://github.com/LinzN/Stem): CPU temperature, network latency and nightly system updates.

system-chain watches the machine STEM runs on and the Linux servers around it. It warns when the host CPU
gets too hot, measures the network latency and updates configured servers every night. Other plugins
(for example [web-api](https://github.com/LinzN/web-api)) read the latest values from it.

---

## Features

| Module          | Interval          | What it does                                                                                     |
|-----------------|-------------------|--------------------------------------------------------------------------------------------------|
| Temperature     | every 20 seconds  | Runs a command via SSH, takes the hottest value and raises information blocks at 95, 98 and 100 °C |
| Network         | every 10 seconds  | Runs a ping command via SSH and stores the average round trip time                               |
| System update   | daily at 02:30    | Runs the STEM script `update_linux-system` for every configured server                           |

- Information blocks (display + user notification) for high CPU temperature and failed updates
- `SystemUpdateEvent(exitCode, hostname, port)` is fired after every server update
- Every module can be switched off separately

---

## Requirements

| Requirement | Version                         |
|-------------|---------------------------------|
| STEM        | 1.2.0 or newer (`stem-api`)     |
| Plugins     | none                            |

---

## Installation

1. Build the plugin with `mvn clean package` (or take the build from Jenkins).
2. Copy `target/system-chain.jar` into the `plugins` folder of STEM.
3. Start STEM once to create the configuration files in `plugins/system-chain/`.
4. Adjust the configuration and restart STEM (plugins are only loaded at startup).

---

## Configuration

### `config.yml`

| Key                  | Default | Description                         |
|----------------------|---------|-------------------------------------|
| `module.temperature` | `true`  | Enable the CPU temperature module   |
| `module.network`     | `true`  | Enable the network latency module   |
| `module.update`      | `true`  | Enable the nightly system update    |

### `temperatureScheduler.yml`

SSH target and command used to read the CPU temperatures. Every output line that contains a number is
treated as one temperature value, the highest one is used. Example:

```yaml
hostname: 10.0.0.1
username: stem
port: 22
command: "sensors | grep Core | awk '{print $3}'"
```

### `networkScheduler.yml`

SSH target and ping command. The average is read from the ping summary line
(`rtt min/avg/max/mdev = 1.2/2.3/3.4/0.1 ms`). Example:

```yaml
hostname: 10.0.0.1
username: stem
port: 22
command: "ping -c 3 1.1.1.1 | tail -1"
```

### `systemUpdateScheduler.yml`

Servers that are updated every night at 02:30. Example:

```yaml
systems:
  webserver:
    hostname: 10.0.0.20
    port: 22
  nas:
    hostname: 10.0.0.30
    port: 2222
```

---

## API for other plugins

| Method                                 | Description                                                  |
|----------------------------------------|--------------------------------------------------------------|
| `NetworkScheduler.getLastPing()`       | Average ping in ms, `-1` if the last measurement failed      |
| `TemperatureScheduler.getHottestCore()`| Hottest CPU value of the last measurement in °C              |

### Events

| Event               | Fired when                                  |
|---------------------|---------------------------------------------|
| `SystemUpdateEvent` | an update of a configured server finished   |

---

## License

Copyright (c) MirraNET, Niklas Linz. Licensed under the
[GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.html).
