package com.evolution.pillar

import org.slf4j.Logger

import java.time.Instant

class Slf4jReporter(logger: Logger) extends Reporter {

  override def migrating(session: Session, dateRestriction: Option[Instant]): Unit = {
    logger.info(s"applying outstanding migrations, date restriction: $dateRestriction")
  }

  override def applying(migration: Migration): Unit = {
    logger.info(s"applying migration: ${ migration.key }")
  }

  override def reversing(migration: Migration): Unit = {
    logger.info(s"reversing migration: ${ migration.key }")
  }

  override def destroying(session: Session, keyspace: String): Unit = {
    logger.info(s"destroying keyspace: $keyspace")
  }

  override def creatingKeyspace(
    session: Session,
    keyspace: String,
    replicationStrategy: ReplicationStrategy,
  ): Unit = {
    logger.info(s"creating keyspace if not exists: $keyspace")
  }

  override def creatingMigrationsTable(
    session: Session,
    keyspace: String,
    appliedMigrationsTableName: String,
  ): Unit = {
    logger.info(s"creating migrations table if not exists: $appliedMigrationsTableName in keyspace $keyspace")
  }
}
