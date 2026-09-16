# Pillar

[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)
[![Build Status](https://github.com/evolution-gaming/pillar/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/evolution-gaming/pillar/actions/workflows/ci.yml?query=branch%3Amaster)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.evolution/pillar_2.13)](https://central.sonatype.com/artifact/com.evolution/pillar_2.13)

Pillar is a Scala library which manages migrations for your [Cassandra][cassandra] data stores.

[cassandra]:http://cassandra.apache.org

Pillar grew from a desire to automatically manage Cassandra schema as code. Managing schema as code
enables automated build and deployment, a foundational practice for an organization striving to
achieve [Continuous Delivery][cd].

Pillar is to Cassandra what [Rails ActiveRecord][ar] migrations or [Play Evolutions][evolutions] are
to relational databases with one key difference: Pillar is completely independent from any
application development framework.

[cd]:http://en.wikipedia.org/wiki/Continuous_delivery

[ar]:https://github.com/rails/rails/tree/master/activerecord

[evolutions]:http://www.playframework.com/documentation/2.0/Evolutions

Forked from https://github.com/Galeria-Kaufhof/pillar as that project is not maintained anymore. As
many upgrades are done, some of the functionality we do not require has been dropped - most notably
the command line interface, which is gone as of 6.0.0. Pillar is a library only.

## Installation

### Prerequisites

1. Java 17 runtime environment
1. Scala 2.13 or Scala 3 (3.3 LTS)
1. Cassandra server - Pillar uses the DataStax Java driver 3.x, the tests run against Cassandra 4.1

### Packages

Pillar is available at Maven Central under the GroupId `com.evolution` and ArtifactId `pillar_2.13`
or `pillar_3`.

```sbt
libraryDependencies += "com.evolution" %% "pillar" % latestVersion
```

## Usage

### Terminology

Migration
: A single change to a data store. Migrations have a description and a time stamp indicating the
time at which it was authored. Migrations are applied in ascending order of that time stamp and
reversed in descending order.

### Migration Files

Migration files contain metadata about the migration, a [CQL][cql] statement used to apply the
migration and, optionally, a [CQL][cql] statement used to reverse the migration. Each file describes
one migration. You probably want to name your files according to time stamp and description,
1370028263_creates_views_table.cql, for example. Pillar reads and parses all files in the migrations
directory, regardless of file name; it does not descend into subdirectories.

The `authoredAt` property is a number of milliseconds since the epoch. Only its ordering matters to
Pillar.

[cql]:http://cassandra.apache.org/doc/cql3/CQL.html

Pillar supports reversible, irreversible and reversible with a no-op down statement migrations. Here
are examples of each:

Reversible migrations have up and down properties.

    -- description: creates views table
    -- authoredAt: 1370028263
    -- up:

    CREATE TABLE views (
      id uuid PRIMARY KEY,
      url text,
      person_id int,
      viewed_at timestamp
    )

    -- down:

    DROP TABLE views

Irreversible migrations have an up property but no down property.

    -- description: creates events table
    -- authoredAt: 1370023262
    -- up:

    CREATE TABLE events (
      batch_id text,
      occurred_at uuid,
      event_type text,
      payload blob,
      PRIMARY KEY (batch_id, occurred_at, event_type)
    )

Reversible migrations with no-op down statements have an up property and an empty down property.

    -- description: adds user_agent to views table
    -- authoredAt: 1370028264
    -- up:

    ALTER TABLE views
    ADD user_agent text

    -- down:

Each migration may optionally specify multiple stages. Stages are executed in the order specified.

    -- description: creates users and groups tables
    -- authoredAt: 1469630066000
    -- up:

    -- stage: 1
    CREATE TABLE groups (
      id uuid,
      name text,
      PRIMARY KEY (id)
    )

    -- stage: 2
    CREATE TABLE users (
      id uuid,
      group_id uuid,
      username text,
      password text,
      PRIMARY KEY (id)
    )


    -- down:

    -- stage: 1
    DROP TABLE users

    -- stage: 2
    DROP TABLE groups

Migrations are read from a directory of your choice with `Registry.fromDirectory`. Alternatively,
migrations can be defined in code with `Migration(...)` and passed to `Registry` directly - see
`PillarLibraryAcceptanceSpec` for examples.

### Library

Pillar is integrated into your application as a library. You create the Cassandra `Cluster`
yourself, wrap a driver session into a Pillar `Session` and hand it to a `Migrator`:

```scala
import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import com.evolution.pillar.*

import java.io.File

object Main extends App {
  val queryConsistencyLevel = ConsistencyLevel.QUORUM
  val contactPoint = "127.0.0.1"
  val keyspace = "my_app_keyspace"
  val keyspaceReplicationStrategy = SimpleStrategy(replicationFactor = 1)

  val cluster = Cluster.builder()
    .addContactPoints(contactPoint)
    .build()

  // reads migration file from the ./migrations directory
  val registry = Registry.fromDirectory(new File("migrations"))
  // logs the progress to INFO by default
  val migrator = Migrator.make(registry = registry)

  try {
    val keyspaceCreateSession = new Session(cluster.connect(), queryConsistencyLevel)

    // creates the keyspace and the applied migrations table
    migrator.initialize(
      session = keyspaceCreateSession,
      keyspace = keyspace,
      replicationStrategy = keyspaceReplicationStrategy,
    )

    val keyspaceMigrateSession = new Session(cluster.connect(keyspace), queryConsistencyLevel)

    // applies all migrations not yet recorded in the applied migrations table
    migrator.migrate(keyspaceMigrateSession)
  } finally {
    cluster.close()
  }
}
```

Take a look at the acceptance spec suite for more details.

## Release Notes

### 6.0.0

Breaking changes:

* The artifact group ID is changed from `com.evolutiongaming` to `com.evolution` - this is the first
  release published to Maven Central
* The root package is changed from `com.evolutiongaming.pillar` to `com.evolution.pillar`, to avoid
  runtime class clashes between the Maven Central and the older non-Maven-Central versions
* The command line interface is removed - no `pillar` executable, no
  `com.evolutiongaming.pillar.cli`
  package and no native packager distribution. It had been broken for a long time; Pillar is a
  library only. The `application.conf` shipped in the published artifact is gone as well - the
  configuration is supplied by your application
* Java 17 is now the minimum runtime, up from Java 11
* Cross-compilation to Scala 2.12 is dropped - Pillar cross-compiles to Scala 2.13 and Scala 3 LTS
  (3.3.x) only
* `ConnectionConfiguration` and the Typesafe Config support it relied on are removed, along with
  `ReplicationStrategyBuilder` and `ConfigurationException`. Connection settings were only ever
  needed by the command line interface; build the Cassandra `Cluster` in your application instead.
  The `com.typesafe:config` dependency is gone.
* `PrintStreamReporter` is replaced by `Slf4jReporter`, which `Migrator.make` installs by default -
  migration progress is logged at INFO rather than written to stdout. `org.slf4j:slf4j-api` is a new
  runtime dependency; supply your own SLF4J binding.
* `Migrator.apply` is deprecated in favour of `Migrator.make`, which defaults the reporter and the
  applied migrations table name.

Other changes:

* Cassandra driver 3.8.0 -> 3.11.5 (the last 3.x release)

### 5.0.1

* Consistency level can now be explicitly configured (the default is still `QUORUM`)

### 5.0.0

* A lot of drastic changes due to forking stale project
* Remove support for Red Hat packages
* Update SBT to 1.3.8 and related SBT plugins
* Cross-compile for only Scala 2.12 and 2.13
* Update Cassandra driver and test libraries
* Change root package to `com.evolutiongaming`
* Require Java 8 as it is minimal for Scala 2.13
* Use `Instant` instead of `Date`

### 4.1.0

* Cross-compile for scala 2.11 and 2.12
* Replace fpm with native-packager

### 4.0.0

* Added the option to specify the name of the applied_migrations table in which the migrations are
  stored. This is useful when using pillar in a muli-module setup where the services have their own
  non shared tables but live both in the same keyspace and should be deployed independently from
  each other

### 3.3.0

* initialize-method split up into two methods (createKeyspace and createMigrationsTable).

### 3.2.0

* travis.yml file
* travis build status in README
* add replication strategy
  support [#1]:https://github.com/Galeria-Kaufhof/pillar/commit/e7429d52b21fb75a52c0756dc53abf930080a4e3
* tweaked scaladoc for CassandraSpec
* quorum
  consistency [#2]:https://github.com/Galeria-Kaufhof/pillar/commit/2a956146c6ed6d3137ba59ecb3752718c03882a9
* add replication strategy support [#9]:https://github.com/Galeria-Kaufhof/pillar/pull/9
* small bugfixes

### 3.1.0

* Allow authentication and ssl connections (convoi)
* Small bugfixes

### 3.0.0

* change package structure to de.kaufhof (MarcoPriebe)

### 2.1.1

* Update to sbt-sonatype dependency to version 1.1 (MarcoPriebe)
* Update to Scala to version 2.11.6 (MarcoPriebe)

### 2.1.0

* Update to Cassandra dependency to version 3.0.0 (MarcoPriebe)

### 2.0.1

* Update a argot dependency to version 1.0.3 (magro)

### 2.0.0

* Allow configuration of Cassandra port (fkoehler)
* Rework Migrator interface to allow passing a Session object when integrating Pillar as a library
  (magro, comeara)

### 1.0.3

* Clarify documentation (pvenable)
* Update Datastax Cassandra driver to version 2.0.2 (magro)
* Update Scala to version 2.10.4 (magro)
* Add cross-compilation to Scala version 2.11.1 (magro)
* Shutdown cluster in migrate & initialize (magro)
* Transition support from StreamSend to Chris O'Meara (comeara)

### 1.0.1

* Add a "destroy" method to drop a keyspace (iamsteveholmes)
