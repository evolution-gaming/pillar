package com.evolution.pillar

class ReportingRegistry(
  reporter: Reporter,
  wrapped: Registry,
) extends Registry(migrations = wrapped.all.map(new ReportingMigration(reporter, _)))
