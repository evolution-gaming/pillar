package com.evolutiongaming.pillar

import com.datastax.driver.core.{Cluster, ConsistencyLevel}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._

/**
  * A mixin for repo test specs that require a Cassandra instance for testing.
  *
  * When tests are run with this mixed in, it will attempt to start an embedded instance of Cassandra on a random port,
  * which will then be usable by the unit test code.
  *
  * The `session` is then available for use by the implementor.
  */
trait CassandraSpec extends ScalaFutures with BeforeAndAfterAll {
  this: Suite =>

  //These must be lazy to ensure correct init order
  protected lazy val port = EmbeddedCassandraServerHelper.getNativeTransportPort

  lazy val cluster = {
    startEmbeddedCassandra()
    Cluster.builder().addContactPoint("127.0.0.1").withPort(port).build()
  }

  //Appropriate consistency level for embedded Cassandra instance
  private val EmbeddedConsistencyLevel = ConsistencyLevel.LOCAL_ONE

  lazy val session = new Session(cluster.connect(), EmbeddedConsistencyLevel)

  protected def session(keyspace: String) = new Session(cluster.connect(keyspace), EmbeddedConsistencyLevel)

  protected def startEmbeddedCassandra(): Unit = try {
    //Start the Cassandra Instance
    println("Starting embedded Cassandra...")
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra_embedded.yaml", 60.seconds.toMillis)
    println(s"Cassandra running on Port $port")
  } catch {
    case e: Exception =>
      System.err.println(s"Error starting Embedded Cassandra: $e")
  }

  override protected def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    super.afterAll()
  }

}

