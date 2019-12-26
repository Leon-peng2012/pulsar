---
id: client-libraries-cpp
title: Pulsar C++ Client
sidebar_label: C++
---

## Supported platforms

The Pulsar C++ client has been successfully tested on **MacOS** and **Linux**.

## Linux

### Installing the RPM and Debian packages

> Pulsar ships pre-built RPM and Debian packages from 2.1.0 release. You can choose to download
> and install those packages instead of building them yourself.

#### RPM

| Link | Crypto files |
|------|--------------|
| [client]({{pulsar:dist_rpm:client}}) | [asc]({{pulsar:dist_rpm:client}}.asc), [sha512]({{pulsar:dist_rpm:client}}.sha512) |
| [client-debuginfo]({{pulsar:dist_rpm:client-debuginfo}}) | [asc]({{pulsar:dist_rpm:client-debuginfo}}.asc),  [sha512]({{pulsar:dist_rpm:client-debuginfo}}.sha512) |
| [client-devel]({{pulsar:dist_rpm:client-devel}}) | [asc]({{pulsar:dist_rpm:client-devel}}.asc),  [sha512]({{pulsar:dist_rpm:client-devel}}.sha512) |

To install RPM packages, download and install them using the following command:

```bash
$ rpm -ivh apache-pulsar-client*.rpm
```

#### Debain

| Link | Crypto files |
|------|--------------|
| [client]({{pulsar:deb:client}}) | [asc]({{pulsar:dist_deb:client}}.asc), [sha512]({{pulsar:dist_deb:client}}.sha512) |
| [client-devel]({{pulsar:deb:client-devel}}) | [asc]({{pulsar:dist_deb:client-devel}}.asc),  [sha512]({{pulsar:dist_deb:client-devel}}.sha512) |

To install Debain packages, download and install them using the following command:

```bash
$ apt install ./apache-pulsar-client*.deb
```

### Building the RPM and Debian packages

> To build RPM and Debian packages of latest master, follow the instructions as below.
> All the instructions are run at the root directory of your cloned Pulsar
> repository.

There are recipes that build the RPM and Debian packages containing a
statically linked `libpulsar.so` / `libpulsar.a` with all required
dependencies.

To build the C++ library packages, first build the Java packages:

```shell
mvn install -DskipTests
```

#### RPM

To build RPM packages, use the following command:

```shell
pulsar-client-cpp/pkg/rpm/docker-build-rpm.sh
```

The RPM packages will be built inside a Docker container and be placed
in `pulsar-client-cpp/pkg/rpm/RPMS/x86_64/`.

| Package name | Content |
|-----|-----|
| pulsar-client | Shared library `libpulsar.so` |
| pulsar-client-devel | Static library `libpulsar.a` and C++ and C headers |
| pulsar-client-debuginfo | Debug symbols for `libpulsar.so` |

#### Debain

To build Debian packages, use the following command:

```shell
pulsar-client-cpp/pkg/deb/docker-build-deb.sh
```

Tge Debian packages will be created at `pulsar-client-cpp/pkg/deb/BUILD/DEB/`

| Package name | Content |
|-----|-----|
| pulsar-client | Shared library `libpulsar.so` |
| pulsar-client-dev | Static library `libpulsar.a` and C++ and C headers |

## MacOS

Pulsar releases are available in the [Homebrew](https://brew.sh/) core repository. You can install the C++ client 
library by using the following command:

```shell
brew install libpulsar
```

The RPM and Debain packages will be installed with the library and headers.

## Protocol URLs


To connect to Pulsar using client libraries, you need to specify a Pulsar protocol URL.

Pulsar protocol URLs are assigned to specific clusters, which use the pulsar URI scheme and the default port of 6650. Here is an example for localhost:

```http
pulsar://localhost:6650
```

A URL for a production Pulsar cluster is shown as below:
```http
pulsar://pulsar.us-west.example.com:6650
```

When TLS authentication is enabled, the URL is shown as below:
```http
pulsar+ssl://pulsar.us-west.example.com:6651
```

## Consumer

```c++
Client client("pulsar://localhost:6650");

Consumer consumer;
Result result = client.subscribe("my-topic", "my-subscribtion-name", consumer);
if (result != ResultOk) {
    LOG_ERROR("Failed to subscribe: " << result);
    return -1;
}

Message msg;

while (true) {
    consumer.receive(msg);
    LOG_INFO("Received: " << msg
            << "  with payload '" << msg.getDataAsString() << "'");

    consumer.acknowledge(msg);
}

client.close();
```


## Producer

```c++
Client client("pulsar://localhost:6650");

Producer producer;
Result result = client.createProducer("my-topic", producer);
if (result != ResultOk) {
    LOG_ERROR("Error creating producer: " << result);
    return -1;
}

// Publish 10 messages to the topic
for (int i = 0; i < 10; i++){
    Message msg = MessageBuilder().setContent("my-message").build();
    Result res = producer.send(msg);
    LOG_INFO("Message sent: " << res);
}
client.close();
```

## Authentication

```cpp
ClientConfiguration config = ClientConfiguration();
config.setUseTls(true);
config.setTlsTrustCertsFilePath("/path/to/cacert.pem");
config.setTlsAllowInsecureConnection(false);
config.setAuth(pulsar::AuthTls::create(
            "/path/to/client-cert.pem", "/path/to/client-key.pem"););

Client client("pulsar+ssl://my-broker.com:6651", config);
```
