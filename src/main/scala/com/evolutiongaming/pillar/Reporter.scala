package com.evolutiongaming.pillar

import java.time.Instant

trait Reporter {
  def migrating(session: Session, dateRestriction: Option[Instant]): Unit
  def applying(migration: Migration): Unit
  def reversing(migration: Migration): Unit
  def destroying(session: Session, keyspace: String): Unit
  def creatingKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): Unit
  def creatingMigrationsTable(session: Session, keyspace: String, appliedMigrationsTableName: String): Unit
}
