package com.evolutiongaming.pillar

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.testcontainers.cassandra.CassandraContainer

/**
 * A mixin for repo test specs that require a Cassandra instance for testing.
 *
 * When tests are run with this mixed in, it will attempt to start a Cassandra container
 * via Testcontainers, with its native transport bound to a random host port, which will
 * then be usable by the unit test code.
 *
 * The `session` is then available for use by the implementor.
 */
trait CassandraSpec extends ScalaFutures with BeforeAndAfterAll {
  this: Suite =>

  // testing against latest Cassandra 4
  private val cassandraDockerImage = "cassandra:4.1.12"

  private lazy val cassandraContainer = {
    val container = new CassandraContainer(cassandraDockerImage)
    container.start()
    container
  }

  // These must be lazy to ensure correct init order
  protected final lazy val port = cassandraContainer.getContactPoint.getPort

  private lazy val cluster: Cluster = {
    Cluster.builder().addContactPoint("127.0.0.1").withPort(port).build()
  }

  // Appropriate consistency level for a single-node Cassandra instance
  private val SingleNodeConsistencyLevel = ConsistencyLevel.LOCAL_ONE

  protected final lazy val session = new Session(cluster.connect(), SingleNodeConsistencyLevel)

  protected def session(keyspace: String) = new Session(cluster.connect(keyspace), SingleNodeConsistencyLevel)

  override protected def afterAll(): Unit = {
    cluster.close()
    cassandraContainer.stop()
    super.afterAll()
  }

}
