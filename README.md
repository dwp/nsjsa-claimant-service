# Claimant Service

## About

Job Seeker's Allowance - microservice for handling of the claimant's personal data 

### Prerequisites

* Java 8
* Maven
* Docker (for postgresql)

## DB
### Local DB

The easiest way to have a local DB up and running on your machine is to use docker
```bash
$ docker run --name dwp-jsa -e POSTGRES_PASSWORD=password -e POSTGRES_DB=dwp-jsa -p5432:5432 postgres
```

## PublicKey

In application.properties, the services.publicKey needs to be populated with a good RSA key.
To create this, and set it, run ./createPublicKey.sh.  This is a one time operation.  Please take
care not to check this change in.

