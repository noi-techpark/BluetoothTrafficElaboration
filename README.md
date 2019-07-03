# BluetoothTrafficElaboration

Application which takes vehicular traffic data from the big data platform, collected by bluetoothboxes around the city of Bolzano, 
and makes different elaborations and saving them back to the bdp again.

## Table of contents

- [Gettings started](#getting-started)
- [Running tests](#running-tests)
- [Deployment](#deployment)
- [Information](#information)

## Getting started

These instructions will get you a copy of the project up and running
on your local machine for development and testing purposes.

### Prerequisites

To build the project, the following prerequisites must be met:

- Java JDK 1.8 or higher (e.g. [OpenJDK](https://openjdk.java.net/))
- [Maven](https://maven.apache.org/) 3.x
- A postgres database with the schema intimev2 and elaboration (bdp) already installed

### Source code

Get a copy of the repository:

```bash
git clone https://github.com/noi-techpark/BluetoothTrafficElaboration
```

Change directory:

```bash
cd BluetoothTrafficElaboration/
```

### Build

Build the project:

```bash
mvn clean package
```

## Running tests

The unit tests can be executed with the following command:

```bash
mvn clean test
```

## Deployment

This is a maven project and will produce a war that can be deployed in any j2ee container like tomcat or jetty.

Steps:

* connect to the postgres database and execute the following script that will add the intimev2.deltart function
  to an existing bdp database
  
```bash
psql ... < BluetoothTrafficElaboration/deltart.sql
```

* change the file src/main/resources/app.properties. set the variable jdbc.connectionString with the jdbc url that connect to the postgres
  database (or configure it within a CI tool)
  
```
jdbc.connectionString=jdbc:postgresql://host:port/db?user=...
```

* create the war executing the following command

```
mvn clean package
```

* deploy the bluetoothtrafficelaboration.war to a j2ee container like tomcat or jetty

* open the (simple) dashboard to check if the project is started/working

```
http(s)://host:port/bluetoothtrafficelaboration
```

## Information

### Support

For support, please contact [info@opendatahub.bz.it](mailto:info@opendatahub.bz.it).

### Contributing

If you'd like to contribute, please follow the following instructions:

- Fork the repository.

- Checkout a topic branch from the `development` branch.

- Make sure the tests are passing.

- Create a pull request against the `development` branch.

### Documentation

More documentation can be found at [https://opendatahub.readthedocs.io/en/latest/index.html](https://opendatahub.readthedocs.io/en/latest/index.html).

### License

The code in this project is licensed under the GNU AFFERO GENERAL PUBLIC LICENSE Version 3 license. See the [LICENSE.md](LICENSE.md) file for more information.
