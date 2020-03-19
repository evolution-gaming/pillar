package com.evolutiongaming.pillar.cli

import com.evolutiongaming.pillar.{Session, Registry, ReplicationStrategy}

case class Command(action: MigratorAction, session: Session, keyspace: String, timeStampOption: Option[Long],
                   registry: Registry, replicationStrategy: ReplicationStrategy, appliedMigrationsTableName: String)
