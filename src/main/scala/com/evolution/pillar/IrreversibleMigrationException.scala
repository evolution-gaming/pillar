package com.evolution.pillar

class IrreversibleMigrationException(
  migration: IrreversibleMigration,
) extends RuntimeException(
  s"Migration ${ migration.authoredAt.toEpochMilli }: ${ migration.description } is not reversible",
)
