package de.kaufhof.pillar.cli

import java.util.Date

import de.kaufhof.pillar.{Migrator, Registry, Reporter}

object CommandExecutor {
  implicit private val migratorConstructor: ((Registry, Reporter, String) => Migrator) = Migrator.apply

  def apply(): CommandExecutor = new CommandExecutor()
}

class CommandExecutor(implicit val migratorConstructor: (Registry, Reporter, String) => Migrator) {
  def execute(command: Command, reporter: Reporter): Unit = {
    val migrator = migratorConstructor(command.registry, reporter, command.appliedMigrationsTableName)

    command.action match {
      case Initialize => migrator.initialize(command.session, command.keyspace, command.replicationStrategy); ()
      case Migrate => migrator.migrate(command.session, command.timeStampOption.map(new Date(_)))
    }
  }
}
