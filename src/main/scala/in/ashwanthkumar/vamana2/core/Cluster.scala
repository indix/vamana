package in.ashwanthkumar.vamana2.core

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

case class MetricsConfig(demand: List[String], supply: List[String], source: String, namespace: String, scalar: String)
object MetricsConfig {
  def fromConfig(config: Config) = {
    MetricsConfig(
      demand = config.getStringList("demand").asScala.toList,
      supply = config.getStringList("supply").asScala.toList,
      source = config.getString("source"),
      namespace = config.getString("namespace"),
      scalar = config.getString("scalar")
    )
  }
}

case class Cluster(name: String, asg: String, minNodes: Int, maxNodes: Int, metricsConfig: MetricsConfig)
object Cluster {
  def fromConfig(config: Config) = {
    val metricsConfig = MetricsConfig.fromConfig(config.getConfig("metrics"))
    Cluster(
      name = config.getString("name"),
      asg = config.getString("asg"),
      minNodes = config.getInt("min-nodes"),
      maxNodes = config.getInt("max-nodes"),
      metricsConfig = metricsConfig
    )
  }
}

case class VamanaConfiguration(clusters: List[Cluster])

object ConfigReader {
  private val NAMESPACE = "vamana"
  def load(config: Config): VamanaConfiguration = {
    val globalConfig = config.getConfig(NAMESPACE)
    val clusters = globalConfig
      .getConfigList("clusters").asScala
      .map(Cluster.fromConfig)
      .toList

    VamanaConfiguration(clusters)
  }

  def load(name: String): VamanaConfiguration = load(ConfigFactory.load(name))
  def read(name: String): VamanaConfiguration = load(ConfigFactory.parseFile(new File(name)))
}
