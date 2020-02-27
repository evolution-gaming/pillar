package com.evolutiongaming.pillar.cli

import java.io.File

import com.datastax.driver.core.{Cluster, ConsistencyLevel, QueryOptions}
import com.evolutiongaming.pillar.config.ConnectionConfiguration
import com.evolutiongaming.pillar.{PrintStreamReporter, ReplicationStrategyBuilder, Reporter, _}
import com.typesafe.config.{Config, ConfigFactory}

object App {
  def apply(reporter: Reporter = new PrintStreamReporter(System.out),
            configuration: Config = ConfigFactory.load()): App = {
    new App(reporter, configuration)
  }

  def main(arguments: Array[String]): Unit = {
    try {
      App().run(arguments)
    } catch {
      case exception: Exception =>
        System.err.println(exception.getMessage)
        System.exit(1)
    }

    System.exit(0)
  }
}

class App(reporter: Reporter, configuration: Config) {

  def run(arguments: Array[String]): Unit = {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val registry = Registry.fromDirectory(new File(commandLineConfiguration.migrationsDirectory, commandLineConfiguration.dataStore), reporter)
    val dataStoreName = commandLineConfiguration.dataStore
    val environment = commandLineConfiguration.environment

    val cassandraConfiguration = new ConnectionConfiguration(dataStoreName, environment, configuration)

    val cluster: Cluster = createCluster(cassandraConfiguration)

    val session = commandLineConfiguration.command match {
      case Initialize => cluster.connect()
      case _ => cluster.connect(cassandraConfiguration.keyspace)
    }

    val replicationOptions = try {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, dataStoreName, environment)
    } catch {
      case e: Exception => throw e
    }

    val command = Command(
      commandLineConfiguration.command,
      session,
      cassandraConfiguration.keyspace,
      commandLineConfiguration.timeStampOption,
      registry,
      replicationOptions,
      cassandraConfiguration.appliedMigrationsTableName
    )

    try {
      CommandExecutor().execute(command, reporter)
    } finally {
      session.close()
    }
  }

  private def createCluster(connectionConfiguration:ConnectionConfiguration): Cluster = {
    val queryOptions = new QueryOptions()
    queryOptions.setConsistencyLevel(ConsistencyLevel.QUORUM)

    val clusterBuilder = Cluster.builder()
      .addContactPoints(connectionConfiguration.seedAddress:_*)
      .withPort(connectionConfiguration.port)
      .withQueryOptions(queryOptions)
    connectionConfiguration.auth.foreach(clusterBuilder.withAuthProvider)

    connectionConfiguration.sslConfig.foreach(_.setAsSystemProperties())
    if (connectionConfiguration.useSsl)
      clusterBuilder.withSSL()

    clusterBuilder.build()
  }
}
