package com.evolutiongaming.pillar

import java.io.File
import java.time.Instant

import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RegistrySpec extends AnyFunSpec with BeforeAndAfter with Matchers with MockitoSugar {
  describe(".fromDirectory") {
    describe("without a reporter parameter") {
      describe("with a directory that exists and has migration files") {
        it("returns a registry with migrations") {
          val registry = Registry.fromDirectory(new File("src/test/resources/pillar/migrations/faker/"))
          registry.all.size should equal(4)
        }
      }

      describe("with a directory that does not exist") {
        it("returns an empty registry") {
          val registry = Registry.fromDirectory(new File("bogus"))
          registry.all.size should equal(0)
        }
      }
    }

    describe("with a reporter parameter") {
      val reporter = mock[Reporter]
      it("returns a registry populated with reporting migrations") {
        val registry = Registry.fromDirectory(new File("src/test/resources/pillar/migrations/faker/"), reporter)
        registry.all.head.getClass should be(classOf[ReportingMigration])
      }
    }
  }

  describe("#all") {
    val now = Instant.now
    val oneSecondAgo = now.minusSeconds(1)
    val migrations = List(
      Migration("test now", now, Seq("up")),
      Migration("test just before", oneSecondAgo, Seq("up"))
    )
    val registry = new Registry(migrations)

    it("sorts migrations by their authoredAt property ascending") {
      registry.all.map(_.authoredAt) should equal(List(oneSecondAgo, now))
    }
  }
}
