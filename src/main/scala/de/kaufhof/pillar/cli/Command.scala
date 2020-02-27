package de.kaufhof.pillar.cli

import com.datastax.driver.core.Session
import de.kaufhof.pillar.{Registry, ReplicationStrategy}

case class Command(action: MigratorAction, session: Session, keyspace: String, timeStampOption: Option[Long],
                   registry: Registry, replicationStrategy: ReplicationStrategy, appliedMigrationsTableName: String)
