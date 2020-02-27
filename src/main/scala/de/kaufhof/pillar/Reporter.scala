package de.kaufhof.pillar

import java.util.Date

import com.datastax.driver.core.Session

trait Reporter {
  def migrating(session: Session, dateRestriction: Option[Date]): Unit
  def applying(migration: Migration): Unit
  def reversing(migration: Migration): Unit
  def destroying(session: Session, keyspace: String): Unit
  def creatingKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): Unit
  def creatingMigrationsTable(session: Session, keyspace: String, appliedMigrationsTableName: String): Unit
}
