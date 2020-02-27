package com.evolutiongaming.pillar

import com.datastax.driver.core.exceptions.InvalidQueryException
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.evolutiongaming.pillar.cli.App
import com.typesafe.config.ConfigFactory
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, GivenWhenThen}

class PillarCommandLineAcceptanceSpec extends AnyFeatureSpec
  with CassandraSpec
  with GivenWhenThen
  with BeforeAndAfter
  with Matchers
  with AcceptanceAssertions {

  val keyspaceName = "pillar_acceptance_test"
  val environment = "acceptance_test"
  val dataStoreName = "faker"

  //create a configuration pointing to the embedded cassandra, falling back to the default configuration
  lazy val config = ConfigFactory.parseString(
    s"""
       |pillar.$dataStoreName {
       |    $environment {
       |        cassandra-port: $port
       |    }
       |}
     """.stripMargin).withFallback(ConfigFactory.load())

  before {
    try {
      session.execute("DROP KEYSPACE %s".format(keyspaceName))
    } catch {
      case _: InvalidQueryException => // ok
    }
  }

  Feature("The operator can initialize a keyspace") {
    info("As an application operator")
    info("I want to initialize a Cassandra keyspace")
    info("So that I can manage the keyspace schema")

    Scenario("initialize a non-existent keyspace") {
      Given("a non-existent keyspace")

      When("the migrator initializes the keyspace")
      App(configuration = config).run(Array("-e", environment, "initialize", dataStoreName))

      Then("the keyspace contains a applied_migrations column family")
      assertEmptyAppliedMigrationsTable()
    }
  }

  Feature("The operator can apply migrations") {
    info("As an application operator")
    info("I want to migrate a Cassandra keyspace from an older version of the schema to a newer version")
    info("So that I can run an application using the schema")

    Scenario("all migrations") {
      Given("an initialized, empty, keyspace")
      App(configuration = config).run(Array("-e", environment, "initialize", dataStoreName))

      Given("a migration that creates an events table")
      Given("a migration that creates a views table")

      When("the migrator migrates the schema")
      App(configuration = config).run(Array("-e", environment, "-d", "src/test/resources/pillar/migrations", "migrate", dataStoreName))

      Then("the keyspace contains the events table")
      session.execute(QueryBuilder.select().from(keyspaceName, "events")).all().size() should equal(0)

      And("the keyspace contains the views table")
      session.execute(QueryBuilder.select().from(keyspaceName, "views")).all().size() should equal(0)

      And("the applied_migrations table records the migrations")
      session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations")).all().size() should equal(4)
    }
  }
}
