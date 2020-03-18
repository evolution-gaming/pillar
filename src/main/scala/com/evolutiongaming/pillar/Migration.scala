package com.evolutiongaming.pillar

import java.time.Instant
import java.util.Date

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.ResultSet

object Migration {
  def apply(description: String, authoredAt: Instant, up: Seq[String]): Migration = {
    new IrreversibleMigration(description, authoredAt, up)
  }

  def apply(description: String, authoredAt: Instant, up: Seq[String], down: Option[Seq[String]]): Migration = {
    down match {
      case Some(downStatement) =>
        new ReversibleMigration(description, authoredAt, up, downStatement)
      case None =>
        new ReversibleMigrationWithNoOpDown(description, authoredAt, up)
    }
  }
}

trait Migration {
  val description: String
  val authoredAt: Instant
  val up: Seq[String]

  def key: MigrationKey = MigrationKey(authoredAt, description)

  def authoredAfter(date: Instant): Boolean = {
    authoredAt.isAfter(date)
  }

  def authoredBefore(date: Instant): Boolean = {
    authoredAt.compareTo(date) <= 0
  }

  def executeUpStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    up.foreach(session.execute)
    insertIntoAppliedMigrations(session, appliedMigrationsTableName)
  }

  def executeDownStatement(session: Session, appliedMigrationsTableName: String): ResultSet

  protected def deleteFromAppliedMigrations(session: Session, appliedMigrationsTableName: String): ResultSet = {
    session.execute(QueryBuilder.
      delete().
      from(appliedMigrationsTableName).
      where(QueryBuilder.eq("authored_at", Date.from(authoredAt))).
      and(QueryBuilder.eq("description", description))
    )
  }

  private def insertIntoAppliedMigrations(session: Session, appliedMigrationsTableName: String): ResultSet = {
    session.execute(QueryBuilder.
      insertInto(appliedMigrationsTableName).
      value("authored_at", Date.from(authoredAt)).
      value("description", description).
      value("applied_at", System.currentTimeMillis())
    )
  }
}

class IrreversibleMigration(val description: String, val authoredAt: Instant, val up: Seq[String]) extends Migration {
  def executeDownStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    throw new IrreversibleMigrationException(this)
  }
}

class ReversibleMigrationWithNoOpDown(val description: String, val authoredAt: Instant, val up: Seq[String]) extends Migration {
  def executeDownStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    deleteFromAppliedMigrations(session, appliedMigrationsTableName)
  }
}

class ReversibleMigration(val description: String, val authoredAt: Instant, val up: Seq[String], val down: Seq[String]) extends Migration {
  def executeDownStatement(session: Session, appliedMigrationsTableName: String): ResultSet = {
    down.foreach(session.execute)
    deleteFromAppliedMigrations(session, appliedMigrationsTableName)
  }
}
