package com.evolutiongaming.pillar

import java.util.Date

import com.datastax.driver.core.{ResultSet, Session}

class ReportingMigration(reporter: Reporter, wrapped: Migration) extends Migration {
  val description: String = wrapped.description
  val authoredAt: Date = wrapped.authoredAt
  val up: Seq[String] = wrapped.up

  override def executeUpStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    reporter.applying(wrapped)
    wrapped.executeUpStatement(session, appliedMigrationsTableName)
  }

  def executeDownStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    reporter.reversing(wrapped)
    wrapped.executeDownStatement(session, appliedMigrationsTableName)
  }
}
