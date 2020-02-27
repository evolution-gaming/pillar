package de.kaufhof.pillar

import java.io.PrintStream
import java.util.Date

import com.datastax.driver.core.Session

class PrintStreamReporter(stream: PrintStream) extends Reporter {

  override def migrating(session: Session, dateRestriction: Option[Date]): Unit = {
    stream.println(s"Migrating with date restriction $dateRestriction")
  }

  override def applying(migration: Migration): Unit = {
    stream.println(s"Applying ${migration.authoredAt.getTime}: ${migration.description}")
  }

  override def reversing(migration: Migration): Unit = {
    stream.println(s"Reversing ${migration.authoredAt.getTime}: ${migration.description}")
  }

  override def destroying(session: Session, keyspace: String): Unit = {
    stream.println(s"Destroying $keyspace")
  }

  override def creatingKeyspace(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy): Unit = {
    stream.println(s"Creating keyspace $keyspace")
  }

  override def creatingMigrationsTable(session: Session, keyspace: String, appliedMigrationsTableName: String): Unit = {
    stream.println(s"Creating migrations-table [$appliedMigrationsTableName] in keyspace $keyspace")
  }

}
