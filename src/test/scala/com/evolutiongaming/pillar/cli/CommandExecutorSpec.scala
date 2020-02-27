package com.evolutiongaming.pillar.cli

import java.time.Instant

import com.datastax.driver.core.Session
import com.evolutiongaming.pillar._
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

class CommandExecutorSpec extends AnyFunSpec with BeforeAndAfter with MockitoSugar {
  describe("#execute") {
    val session = mock[Session]
    val keyspace = "myks"
    val registry = mock[Registry]
    val reporter = mock[Reporter]
    val migrator = mock[Migrator]
    val appliedMigrationsTableName = "applied_migrations"
    val simpleStrategy = SimpleStrategy()
    val networkTopologyStrategy = NetworkTopologyStrategyTestData.networkTopologyStrategy
    val migratorConstructor = mock[(Registry, Reporter, String) => Migrator]
    when(migratorConstructor.apply(registry, reporter, appliedMigrationsTableName)).thenReturn(migrator)
    val executor = new CommandExecutor()(migratorConstructor)

    describe("an initialize action") {
      val commandSimple = Command(Initialize, session, keyspace, None, registry, simpleStrategy, appliedMigrationsTableName)

      executor.execute(commandSimple, reporter)

      it("initializes a simple strategy") {
        verify(migrator).initialize(session, keyspace, simpleStrategy)
      }

      val commandNetwork = Command(Initialize, session, keyspace, None, registry, networkTopologyStrategy, appliedMigrationsTableName)

      executor.execute(commandNetwork, reporter)

      it("initializes a network topology strategy") {
        verify(migrator).initialize(session, keyspace, networkTopologyStrategy)
      }
    }

    describe("a migrate action without date restriction") {
      val command = Command(Migrate, session, keyspace, None, registry, simpleStrategy, appliedMigrationsTableName)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, None)
      }
    }

    describe("a migrate action with date restriction") {
      val date = Instant.now
      val command = Command(Migrate, session, keyspace, Some(date.toEpochMilli), registry, simpleStrategy, appliedMigrationsTableName)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, Some(date))
      }
    }
  }
}
