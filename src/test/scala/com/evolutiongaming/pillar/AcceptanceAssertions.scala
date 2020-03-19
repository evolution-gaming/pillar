package com.evolutiongaming.pillar

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Metadata
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

trait AcceptanceAssertions extends Matchers {
  val session: Session
  val keyspaceName: String

  protected def assertEmptyAppliedMigrationsTable(appliedMigrationsTableName: String = "applied_migrations"): Assertion = {
    session.execute(QueryBuilder.select().from(keyspaceName, appliedMigrationsTableName)).all().size() should equal(0)
  }

  protected def assertKeyspaceDoesNotExist(): Assertion = {
    val metadata: Metadata = session.getCluster.getMetadata
    metadata.getKeyspace(keyspaceName) should be(null)
  }
}
