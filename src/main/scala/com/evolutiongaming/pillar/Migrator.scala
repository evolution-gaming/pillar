package com.evolutiongaming.pillar

import java.util.Date

import com.datastax.driver.core.{ResultSet, Session}

object Migrator {
  def apply(registry: Registry, appliedMigrationsTableName: String): Migrator = {
    new CassandraMigrator(registry, appliedMigrationsTableName)
  }

  def apply(registry: Registry, reporter: Reporter, appliedMigrationsTableName: String): Migrator = {
    new ReportingMigrator(reporter, apply(registry, appliedMigrationsTableName), appliedMigrationsTableName)
  }
}

trait Migrator {
  def migrate(session: Session, dateRestriction: Option[Date] = None): Unit

  def initialize(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet

  def createKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet

  def createMigrationsTable(session: Session, keyspace: String): ResultSet

  def destroy(session: Session, keyspace: String): ResultSet
}
