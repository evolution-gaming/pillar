package com.evolution.pillar

class InvalidMigrationException(val errors: Map[String, String]) extends RuntimeException
