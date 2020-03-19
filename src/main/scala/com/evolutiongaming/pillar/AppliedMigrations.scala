package com.evolutiongaming.pillar

import java.time.Instant

import com.datastax.driver.core.querybuilder.QueryBuilder

import scala.jdk.CollectionConverters._

object AppliedMigrations {
  def apply(session: Session, registry: Registry, appliedMigrationsTableName: String): AppliedMigrations = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from(appliedMigrationsTableName))
    new AppliedMigrations(results.all().asScala.map {
      row => registry(MigrationKey(row.getTimestamp("authored_at").toInstant, row.getString("description")))
    }.toSeq)
  }
}

class AppliedMigrations(applied: Seq[Migration]) {
  def length: Int = applied.length

  def apply(index: Int): Migration = applied.apply(index)

  def iterator: Iterator[Migration] = applied.iterator

  def authoredAfter(date: Instant): Seq[Migration] = applied.filter(migration => migration.authoredAfter(date))

  def contains(other: Migration): Boolean = applied.contains(other)
}
