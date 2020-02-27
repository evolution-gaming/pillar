package de.kaufhof.pillar.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/**
  * Tests for Connection Configuration.
  */
class ConnectionConfigurationTest extends AnyFunSpec with BeforeAndAfter with Matchers {
  val TRUST_STORE = "javax.net.ssl.trustStore"
  val TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword"
  val TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType"
  val KEY_STORE = "javax.net.ssl.keyStore"
  val KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword"
  val KEY_STORE_TYPE = "javax.net.ssl.keyStoreType"
  before {
    val propertiesToReset = List(
      TRUST_STORE, TRUST_STORE_PASSWORD, TRUST_STORE_TYPE,
      KEY_STORE, KEY_STORE_PASSWORD, KEY_STORE_TYPE)
    propertiesToReset.foreach(System.getProperties.remove(_))


  }
  describe("#initialize") {
    it("should allow authentication to be set") {
      val config: Config = ConfigFactory.load("authConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "development",
        config)
      configuration.auth === Some(PlaintextAuth("cassandra", "secret"))
    }
    it("should show defaults for useSsl") {
      val config: Config = ConfigFactory.load()
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "development", config)
      configuration.useSsl === false
      configuration.sslConfig === None
    }
    it("should set ssl keystore system properties when ssl is configured correctly") {
      val config: Config = ConfigFactory.load("sslKeystoreConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "ssl_with_just_keystore",
        config)
      configuration.useSsl === true
      configuration.sslConfig match {
        case Some(sslConfig) => sslConfig.setAsSystemProperties()
          System.getProperty(KEY_STORE) === "keystore.jks"
          System.getProperty(KEY_STORE_PASSWORD) === "secret"
          System.getProperty(KEY_STORE_TYPE) === "JCEKS"
          System.getProperty(TRUST_STORE) === null
          System.getProperty(TRUST_STORE_PASSWORD) === null
          System.getProperty(TRUST_STORE_TYPE) === null
        case None => fail("ssl should be configured")
      }
    }
    it("should set ssl truststore system properties when ssl is configured correctly") {
      val config: Config = ConfigFactory.load("sslKeystoreConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "ssl_with_just_truststore_and_no_type",
        config)
      configuration.useSsl === true
      configuration.sslConfig match {
        case Some(sslConfig) => sslConfig.setAsSystemProperties()
          System.getProperty(KEY_STORE) === null
          System.getProperty(KEY_STORE_PASSWORD) === null
          System.getProperty(KEY_STORE_TYPE) === null
          System.getProperty(TRUST_STORE) === "truststore.jks"
          System.getProperty(TRUST_STORE_PASSWORD) === "secret"
          System.getProperty(TRUST_STORE_TYPE) === "JKS"
        case None => fail("ssl should be configured")
      }
    }

    it("should set ssl keystore and truststore system properties when ssl is configured correctly") {
      val config: Config = ConfigFactory.load("sslKeystoreConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker",
        "ssl_with_keystore_and_truststore_and_no_keystore_type",
        config)
      configuration.useSsl === true
      configuration.sslConfig match {
        case Some(sslConfig) => sslConfig.setAsSystemProperties()
          System.getProperty(KEY_STORE) === "keystore.jks"
          System.getProperty(KEY_STORE_PASSWORD) === "secret"
          System.getProperty(KEY_STORE_TYPE) === "JKS"
          System.getProperty(TRUST_STORE) === "truststore.jks"
          System.getProperty(TRUST_STORE_PASSWORD) === "secret"
          System.getProperty(TRUST_STORE_TYPE) === "JCEKS"
        case None => fail("ssl should be configured")
      }
    }


    it("should allow ssl usage with system properties directly, meaning keystore and truststore will be set from " +
      "outside") {
      val config: Config = ConfigFactory.load("sslKeystoreConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "no_ssl_but_wanted_is_also_valid",
        config)
      configuration.useSsl === true
      configuration.sslConfig === None
    }

    it("should allow a single cassandra seed address to be read") {
      val config: Config = ConfigFactory.load("authConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "test", config)
      configuration.seedAddress should equal(List("127.0.0.1"))
    }

    it("should allow multiple cassandra seed addresses to be read") {
      val config: Config = ConfigFactory.load("authConfig.conf")
      val configuration: ConnectionConfiguration = new ConnectionConfiguration("faker", "multiple_seed_addresses_test",
        config)
      configuration.seedAddress should equal(List("127.0.0.1", "127.0.0.2", "127.0.0.3"))
    }
  }
}
