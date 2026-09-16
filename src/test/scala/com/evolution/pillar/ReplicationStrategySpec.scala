package com.evolution.pillar

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ReplicationStrategySpec extends AnyFunSpec with Matchers {

  describe("SimpleStrategy") {
    it("defaults to a replication factor of 3") {
      SimpleStrategy().replicationFactor should equal(3)
    }

    it("renders CQL replication options") {
      SimpleStrategy(2).cql should equal("{'class' : 'SimpleStrategy', 'replication_factor' : 2}")
    }

    it("renders its CQL as toString") {
      val strategy = SimpleStrategy(2)
      strategy.toString should equal(strategy.cql)
    }

    it("rejects a replication factor below one") {
      intercept[IllegalArgumentException] {
        SimpleStrategy(0)
      }
      intercept[IllegalArgumentException] {
        SimpleStrategy(-1)
      }
    }
  }

  describe("NetworkTopologyStrategy") {
    it("renders CQL replication options for a single data center") {
      val strategy = NetworkTopologyStrategy(Seq(CassandraDataCenter("dc1", 2)))

      strategy.cql should equal("{'class' : 'NetworkTopologyStrategy', 'dc1' : 2  }")
    }

    it("renders every data center, preserving the given order") {
      val strategy = NetworkTopologyStrategy(
        Seq(CassandraDataCenter("dc2", 3), CassandraDataCenter("dc1", 2)),
      )

      strategy.cql should equal("{'class' : 'NetworkTopologyStrategy', 'dc2' : 3 , 'dc1' : 2  }")
    }

    it("renders its CQL as toString") {
      val strategy = NetworkTopologyStrategy(Seq(CassandraDataCenter("dc1", 2)))

      strategy.toString should equal(strategy.cql)
    }

    it("rejects an empty list of data centers") {
      intercept[IllegalArgumentException] {
        NetworkTopologyStrategy(Seq.empty)
      }
    }
  }

  describe("CassandraDataCenter") {
    it("rejects a replication factor below one") {
      intercept[IllegalArgumentException] {
        CassandraDataCenter("dc1", 0)
      }
      intercept[IllegalArgumentException] {
        CassandraDataCenter("dc1", -1)
      }
    }

    it("rejects an empty name") {
      intercept[IllegalArgumentException] {
        CassandraDataCenter("", 3)
      }
    }
  }
}
