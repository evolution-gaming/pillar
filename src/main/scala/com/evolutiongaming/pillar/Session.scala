package com.evolutiongaming.pillar

import com.datastax.driver.core.{Cluster, ConsistencyLevel, ResultSet, SimpleStatement, Statement, Session => CassandraSession}

class Session(cassandraSession: CassandraSession, consistencyLevel: ConsistencyLevel) {

  def execute(query: String): ResultSet = execute(new SimpleStatement(query))

  def execute(statement: Statement): ResultSet =
    cassandraSession.execute(statement.setConsistencyLevel(consistencyLevel))

  def getCluster: Cluster = cassandraSession.getCluster
}
