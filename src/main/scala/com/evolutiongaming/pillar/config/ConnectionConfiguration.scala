package com.evolutiongaming.pillar.config

import com.datastax.driver.core.{AuthProvider, PlainTextAuthProvider}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueType}

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

/**
  * Configuration for connection to cassandra.
  */
class ConnectionConfiguration(dataStoreName: String, environment: String, appConfig: Config) {

  val connectionConfig: Config = appConfig
    .getConfig(s"pillar.$dataStoreName.$environment")
    .withFallback(ConfigFactory.load("cassandraConnectionReference.conf"))

  val keyspace = connectionConfig.getString("cassandra-keyspace-name")
  val seedAddress = ConfigHelper.readAsStringArray(connectionConfig, "cassandra-seed-address")

  val port = connectionConfig.getInt("cassandra-port")

  val useSsl = connectionConfig.getBoolean("use-ssl")

  import ConfigHelper.toOptionalConfig

  val auth = Auth(connectionConfig.getOptionalConfig("auth"))
  val appliedMigrationsTableName = connectionConfig.getOptionalString("applied-migrations-table-name").getOrElse("applied_migrations")

  val sslConfig: Option[SslConfig] = SslConfig(connectionConfig.getOptionalConfig("ssl-options"))

}

class OptionalConfig(config: Config) {
  def getOptionalConfig(path: String): Option[Config] = {
    if (config.hasPath(path)) {
      Some(config.getConfig(path))
    } else {
      None
    }
  }

  def getOptionalString(path: String): Option[String] = {
    if (config.hasPath(path)) {
      Some(config.getString(path))
    } else {
      None
    }
  }
}

object ConfigHelper {
  implicit def toOptionalConfig(config: Config): OptionalConfig = {
    new OptionalConfig(config)
  }

  // Maintain backward compatibility with specification of a single String value instead of an array
  def readAsStringArray(config: Config, path: String) : List[String] = {
    val value = config.getValue(path)
    if (value.valueType() == ConfigValueType.LIST) {
      value.unwrapped().asInstanceOf[java.util.List[Object]].asScala.toList.map(a => a.asInstanceOf[String])
    } else {
      List(value.unwrapped().asInstanceOf[String])
    }
  }
}

abstract sealed class Auth

case class PlaintextAuth(username: String, password: String) extends Auth

object Auth {
  def apply(config: Option[Config]): Option[AuthProvider] = {
    config.map(config => new PlainTextAuthProvider(config.getString("username"), config.getString("password")))
  }
}

case class TrustStoreConfig(trustStorePath: String, trustStorePassword: String, trustStoreType: String = "JKS") {
  def setAsSystemProperties() = {
    System.setProperty("javax.net.ssl.trustStore", trustStorePath)
    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword)
    System.setProperty("javax.net.ssl.trustStoreType", trustStoreType)
  }
}

case class KeyStoreConfig(keyStorePath: String, keyStorePassword: String, keyStoreType: String = "JKS") {
  def setAsSystemProperties(): Unit = {
    System.setProperty("javax.net.ssl.keyStore", keyStorePath)
    System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword)
    System.setProperty("javax.net.ssl.keyStoreType", keyStoreType)
    ()
  }
}

case class SslConfig(val keyStoreConfig: Option[KeyStoreConfig], val trustStoreConfig: Option[TrustStoreConfig]) {
  def setAsSystemProperties(): Unit = {
    keyStoreConfig.foreach(_.setAsSystemProperties())
    trustStoreConfig.foreach(_.setAsSystemProperties())
  }
}

object SslConfig {

  import ConfigHelper._

  def apply(config: Option[Config]): Option[SslConfig] = {
    config.map(config => {
      val keyStoreConfig: Option[KeyStoreConfig] = for (
        path <- config.getOptionalString("key-store-path");
        password <- config.getOptionalString("key-store-password"))
        yield KeyStoreConfig(path, password, config.getOptionalString("key-store-type").getOrElse("JKS"))

      val trustStoreConfig: Option[TrustStoreConfig] = for (
        path <- config.getOptionalString("trust-store-path");
        password <- config.getOptionalString("trust-store-password"))
        yield TrustStoreConfig(path, password, config.getOptionalString("trust-store-type").getOrElse("JKS"))

      new SslConfig(keyStoreConfig, trustStoreConfig)

    })
  }
}
