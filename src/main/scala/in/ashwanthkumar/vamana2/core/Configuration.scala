package in.ashwanthkumar.vamana2.core

import java.io.File
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

case class MetricsConfig(demand: List[String], supply: List[String],
                         namespace: Option[String], dimensions: Map[String, String], durationInMinutes: Int)
object MetricsConfig {
  def fromConfig(config: Config) = {
    val dimensions = config.getConfig("dimensions")
      .entrySet().asScala
      .map(entry => entry.getKey -> entry.getValue.unwrapped().toString)
      .toMap
    MetricsConfig(
      demand = config.getStringList("demand").asScala.toList,
      supply = config.getStringList("supply").asScala.toList,
      namespace = stringOption(config, "namespace"),
      dimensions = dimensions,
      durationInMinutes = config.getDuration("range", TimeUnit.MINUTES).toInt
    )
  }

  private def stringOption(config: Config, key: String) = {
    if(config.hasPath(key)) Some(config.getString(key))
    else None
  }
}

case class Cluster(name: String, asId: String, minNodes: Int, maxNodes: Int, metricsConfig: MetricsConfig,
                   scalar: String, scalaConfig: Option[Config] = None,
                   collector: String, collectorConfig: Option[Config] = None,
                   autoscalar: String, autoscalarConfig: Option[Config] = None)
object Cluster {
  def fromConfig(config: Config) = {
    val metricsConfig = MetricsConfig.fromConfig(config.getConfig("metrics"))
    Cluster(
      name = config.getString("name"),
      asId = config.getString("as-id"),
      minNodes = config.getInt("min-nodes"),
      maxNodes = config.getInt("max-nodes"),
      metricsConfig = metricsConfig,
      scalar = config.getString("scalar"),
      collector = config.getString("collector"),
      autoscalar = config.getString("autoscalar")
    )
  }
}

case class VamanaConfiguration(clusters: List[Cluster], notifyConfig: Config)

object ConfigReader {
  private val NAMESPACE = "vamana"
  def load(config: Config): VamanaConfiguration = {
    val globalConfig = config.getConfig(NAMESPACE)
    val clusters = globalConfig
      .getConfigList("clusters").asScala
      .map(Cluster.fromConfig)
      .toList

    VamanaConfiguration(clusters, globalConfig.getConfig("notify"))
  }

  def load(name: String): VamanaConfiguration = load(ConfigFactory.load(name))
  def read(name: String): VamanaConfiguration = load(ConfigFactory.parseFile(new File(name)))
}
