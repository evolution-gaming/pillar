package com.evolution.pillar

import com.datastax.driver.core.Metadata
import com.datastax.driver.core.querybuilder.QueryBuilder
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

trait AcceptanceAssertions extends Matchers {
  protected def session: Session
  protected def keyspaceName: String

  protected def assertEmptyAppliedMigrationsTable(
    appliedMigrationsTableName: String = CassandraMigrator.appliedMigrationsTableNameDefault,
  ): Assertion = {
    session.execute(
      QueryBuilder.select().from(keyspaceName, appliedMigrationsTableName),
    ).all().size() should equal(0)
  }

  protected def assertKeyspaceDoesNotExist(): Assertion = {
    val metadata: Metadata = session.getCluster.getMetadata
    metadata.getKeyspace(keyspaceName) should be(null)
  }
}
