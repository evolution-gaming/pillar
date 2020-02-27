package com.evolutiongaming.pillar


import java.time.Instant

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MigrationSpec extends AnyFunSpec with Matchers {
  describe(".apply") {
    describe("without a down parameter") {
      it("returns an irreversible migration") {
        Migration.apply("description", Instant.now, Seq("up")).getClass should be(classOf[IrreversibleMigration])
      }
    }

    describe("with a down parameter") {
      describe("when the down is None") {
        it("returns a reversible migration with no-op down") {
          Migration.apply("description", Instant.now, Seq("up"), None).getClass should be(classOf[ReversibleMigrationWithNoOpDown])
        }
      }

      describe("when the down is Some") {
        it("returns a reversible migration with no-op down") {
          Migration.apply("description", Instant.now, Seq("up"), Some(Seq("down"))).getClass should be(classOf[ReversibleMigration])
        }
      }
    }
  }
}
