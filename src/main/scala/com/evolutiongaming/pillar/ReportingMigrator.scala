package com.evolutiongaming.pillar

import java.time.Instant

import com.datastax.driver.core.{ResultSet, Session}

class ReportingMigrator(reporter: Reporter, wrapped: Migrator, appliedMigrationsTableName: String) extends Migrator {
  override def initialize(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet = {
    createKeyspace(session, keyspace, replicationStrategy)
    createMigrationsTable(session, keyspace)
  }

  override def migrate(session: Session, dateRestriction: Option[Instant] = None): Unit = {
    reporter.migrating(session, dateRestriction)
    wrapped.migrate(session, dateRestriction)
  }

  override def destroy(session: Session, keyspace: String): ResultSet = {
    reporter.destroying(session, keyspace)
    wrapped.destroy(session, keyspace)
  }

  override def createKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): ResultSet = {
    reporter.creatingKeyspace(session, keyspace, replicationStrategy)
    wrapped.createKeyspace(session, keyspace, replicationStrategy)
  }

  override def createMigrationsTable(session: Session, keyspace: String): ResultSet = {
    reporter.creatingMigrationsTable(session, keyspace, appliedMigrationsTableName)
    wrapped.createMigrationsTable(session, keyspace)
  }
}
