package com.evolutiongaming.pillar

class InvalidMigrationException(val errors: Map[String,String]) extends RuntimeException
