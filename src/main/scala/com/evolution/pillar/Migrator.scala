package com.evolution.pillar

import com.datastax.driver.core.ResultSet
import org.slf4j.LoggerFactory

import java.time.Instant

object Migrator {
  private val logger = LoggerFactory.getLogger(classOf[Migrator])

  val DefaultReporter: Reporter = new Slf4jReporter(logger)

  @deprecated("use make", since = "6.0.0")
  def apply(registry: Registry, appliedMigrationsTableName: String): Migrator = {
    new CassandraMigrator(registry, appliedMigrationsTableName)
  }

  @deprecated("use make", since = "6.0.0")
  def apply(registry: Registry, reporter: Reporter, appliedMigrationsTableName: String): Migrator = {
    new ReportingMigrator(reporter, apply(registry, appliedMigrationsTableName), appliedMigrationsTableName)
  }

  def make(
    registry: Registry,
    reporter: Option[Reporter] = Some(DefaultReporter),
    appliedMigrationsTableName: String = CassandraMigrator.appliedMigrationsTableNameDefault,
  ): Migrator = {
    reporter.fold[Migrator](
      new CassandraMigrator(
        registry,
        appliedMigrationsTableName = appliedMigrationsTableName,
      ),
    ) { reporter =>
      val cassandraMigrator = new CassandraMigrator(
        new ReportingRegistry(reporter, registry),
        appliedMigrationsTableName = appliedMigrationsTableName,
      )
      new ReportingMigrator(
        reporter,
        cassandraMigrator,
        appliedMigrationsTableName = appliedMigrationsTableName,
      )
    }
  }
}

trait Migrator {
  def migrate(session: Session, dateRestriction: Option[Instant] = None): Unit

  def initialize(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet

  def createKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet

  def createMigrationsTable(session: Session, keyspace: String): ResultSet

  def destroy(session: Session, keyspace: String): ResultSet
}
