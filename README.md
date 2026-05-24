# Java Cadence Samples
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcadence-workflow%2Fcadence-java-samples.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcadence-workflow%2Fcadence-java-samples?ref=badge_shield)

These samples demonstrate various capabilities of Java Cadence client and server. You can learn more about Cadence at:
* [Cadence Service](https://github.com/uber/cadence)
* [Cadence Java Client](https://github.com/uber/cadence-java-client)
* [Go Cadence Client](https://github.com/uber-go/cadence-client)

## Overview of the Samples

* **HelloWorld Samples**

    The following samples demonstrate:

  * **HelloActivity**: a single activity workflow
  * **HelloActivityRetry**: how to retry an activity
  * **HelloAsync**: how to call activities asynchronously and wait for them using Promises
  * **HelloAsyncLambda**: how to run part of a workflow asynchronously in a separate task (thread)
  * **HelloAsyncActivityCompletion**: an asynchronous activity implementation
  * **HelloChild**: a child workflow
  * **HelloException**: exception propagation and wrapping
  * **HelloQuery**: a query
  * **HelloSignal**: sending and handling a signal
  * **HelloPeriodic**: a sample workflow that executes an activity periodically forever
  * **HelloSearchAttributes**: how to use search attributes
  * **HelloCron**: a cron workflow 

* **FileProcessing** demonstrates task routing features. The sample workflow downloads a file, processes it, and uploads
    the result to a destination. The first activity can be picked up by any worker. However, the second and third activities
    must be executed on the same host as the first one.

* **Custom Workflow Controls** ([`com.uber.cadence.samples.query`](src/main/java/com/uber/cadence/samples/query/)) — workflow queries that return **markdown** for Cadence Web (Markdoc buttons that **signal** workflows or **start** new workflows). **Requires Cadence Web v4.0.14+.** Copy-paste run instructions: [query samples README](src/main/java/com/uber/cadence/samples/query/README.md).

* **DataConverter Samples** — three independent custom `DataConverter` patterns that transparently transform every workflow input, output, and activity parameter. Each lives in its own package and is fully standalone, so you can copy any one of them into your own project:
    * **Compression** ([`com.uber.cadence.samples.compression`](src/main/java/com/uber/cadence/samples/compression/)) — gzip-over-JSON; typically 60-80% size reduction for repetitive payloads. [README](src/main/java/com/uber/cadence/samples/compression/README.md).
    * **Encryption** ([`com.uber.cadence.samples.encryption`](src/main/java/com/uber/cadence/samples/encryption/)) — AES-256-GCM so payloads in Cadence history are unreadable without the key. [README](src/main/java/com/uber/cadence/samples/encryption/README.md).
    * **Claim-check offload** ([`com.uber.cadence.samples.claimcheck`](src/main/java/com/uber/cadence/samples/claimcheck/)) — payloads above a threshold are stored in an external `BlobStore` (S3, GCS, Azure Blob, MinIO, local disk); only a small reference travels through history. [README](src/main/java/com/uber/cadence/samples/claimcheck/README.md).

## Get the Samples

Run the following commands:

      git clone https://github.com/uber/cadence-java-samples
      cd cadence-java-samples

## Import into IntelliJ

In the IntelliJ user interface, navigate to **File**->**New**->**Project from Existing Sources**.

Select the cloned directory. In the **Import Project page**, select **Import project from external model**,
choose **Gradle** and then click **Next**->**Finish**.

## Build the Samples

      ./gradlew build

## Run Cadence Server

Run Cadence Server using Docker Compose:

    curl -O https://raw.githubusercontent.com/uber/cadence/master/docker/docker-compose.yml
    docker-compose up

If this does not work, see the instructions for running Cadence Server
at https://github.com/uber/cadence/blob/master/README.md.

## Register the Domain

To register the *samples-domain* domain, run the following command once before running any samples:

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.common.RegisterDomain

Or using Cadence CLI:

```
cadence --domain samples-domain domain register
```

## See Cadence UI

The Cadence Server running in a docker container includes a Web UI.

Connect to [http://localhost:8088](http://localhost:8088).

Enter the *samples-domain* domain. You'll see a "No Results" page. After running any sample, change the filter in the
top right corner from "Open" to "Closed" to see the list of the completed workflows.

Click on a *RUN ID* of a workflow to see more details about it. Try different view formats to get a different level of
details about the execution history.

For **query responses rendered as markdown** (Custom Workflow Controls), use **Cadence Web v4.0.14 or newer** and follow the [query samples README](src/main/java/com/uber/cadence/samples/query/README.md).

## Install Cadence CLI

[Command Line Interface Documentation](https://mfateev.github.io/cadence/docs/08_cli)

## Run the samples

Each sample has specific requirements for running it. The following sections contain information about
how to run each of the samples after you've built them using the preceding instructions.

Don't forget to check unit tests found under src/test/java!

### Hello World

To run the hello world samples:

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloActivity
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloActivityRetry
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloAsync
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloAsyncActivityCompletion
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloAsyncLambda
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloChild
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloException
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloPeriodic
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloQuery
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloSignal
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloSearchAttributes
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.hello.HelloCron

### File Processing

This sample has two executables. Execute each command in a separate terminal window. The first command
runs the worker that hosts the workflow and activities implementation. To demonstrate that activities
execute together, we recommend that you run more than one instance of this worker.

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.fileprocessing.FileProcessingWorker

The second command starts workflows. Each invocation starts a new workflow execution.

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.fileprocessing.FileProcessingStarter

### Custom Workflow Controls (Markdoc query responses)

These samples need **Cadence Web v4.0.14+**. Run a **worker** in one terminal, then a **starter** in another. See [src/main/java/com/uber/cadence/samples/query/README.md](src/main/java/com/uber/cadence/samples/query/README.md) for full copy-paste steps.

Worker (task list `query`):

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.QueryWorker

Starters (pick one per run):

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.MarkdownQueryStarter
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.LunchVoteStarter
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.query.OrderFulfillmentStarter

In Cadence Web, open the workflow → **Query** tab → run query **`Signal`**, **`options`**, or **`dashboard`** (matching the starter you used).

### DataConverter Samples

Three independent samples demonstrating custom `DataConverter` implementations. Each sample is self-contained in its own package with its own worker, starter, task list, and README. Pick one to run, or run all three in parallel — they share nothing.

#### Compression (gzip-over-JSON)

See [src/main/java/com/uber/cadence/samples/compression/README.md](src/main/java/com/uber/cadence/samples/compression/README.md).

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.compression.CompressionWorker
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.compression.CompressionStarter

#### Encryption (AES-256-GCM)

See [src/main/java/com/uber/cadence/samples/encryption/README.md](src/main/java/com/uber/cadence/samples/encryption/README.md) for the `CADENCE_ENCRYPTION_KEY` env var.

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.encryption.EncryptionWorker
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.encryption.EncryptionStarter

#### Claim-check offload

See [src/main/java/com/uber/cadence/samples/claimcheck/README.md](src/main/java/com/uber/cadence/samples/claimcheck/README.md) for swap-in instructions for S3, GCS, Azure Blob, and MinIO.

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.claimcheck.ClaimCheckWorker
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.claimcheck.ClaimCheckStarter

### Trip Booking

Cadence implementation of the [Camunda BPMN trip booking example](https://github.com/berndruecker/trip-booking-saga-java)

Demonstrates Cadence approach to SAGA.

To run:

    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.bookingsaga.TripBookingSaga

### Sprint Boot Application

Example of how to start a cadence worker service using Spring Boot Framework

To run:

    # Start Cadence Server
    # see https://github.com/uber/cadence/tree/master/docker
    # register domain
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.common.RegisterDomain
    ./gradlew -q execute -PmainClass=com.uber.cadence.samples.spring.CadenceSamplesApplication

Apache 2.0 License, please see [LICENSE](https://github.com/cadence-workflow/cadence-java-samples/blob/master/LICENSE.txt) for details.

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcadence-workflow%2Fcadence-java-samples.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcadence-workflow%2Fcadence-java-samples?ref=badge_large)