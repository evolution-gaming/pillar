package de.kaufhof.pillar

import com.datastax.driver.core.Session
import org.scalatest.{FunSpec, Matchers}
import org.mockito.MockitoSugar

class ReportingMigrationSpec extends FunSpec with Matchers with MockitoSugar {
  val reporter = mock[Reporter]
  val wrapped = mock[Migration]
  val migration = new ReportingMigration(reporter, wrapped)
  val session = mock[Session]
  val appliedMigrationsTableName = "applied_migrations"

  describe("#executeUpStatement") {
    migration.executeUpStatement(session, appliedMigrationsTableName)

    it("reports the applying action") {
      verify(reporter).applying(wrapped)
    }

    it("delegates to the wrapped migration") {
      verify(wrapped).executeUpStatement(session, appliedMigrationsTableName)
    }
  }

  describe("#executeDownStatement") {
    migration.executeDownStatement(session, appliedMigrationsTableName)

    it("reports the reversing action") {
      verify(reporter).reversing(wrapped)
    }

    it("delegates to the wrapped migration") {
      verify(wrapped).executeDownStatement(session, appliedMigrationsTableName)
    }
  }
}
