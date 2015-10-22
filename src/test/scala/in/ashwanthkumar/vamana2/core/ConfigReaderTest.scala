package in.ashwanthkumar.vamana2.core

import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper, have}

class ConfigReaderTest extends FlatSpec {
  "ConfigReader" should "read the configuration file" in {
    val vamanaConfig = ConfigReader.load("test-clusters")
    vamanaConfig.clusters should have size 1

    val cluster = vamanaConfig.clusters.head
    cluster.name should be("Hadoop Datapipeline Cluster")
    cluster.asId should be("as-datapipeline-tt-staging-spot")
    cluster.maxNodes should be(20)
    cluster.minNodes should be(1)
    cluster.collector should be("in.ashwanthkumar.vamana2.aws.CloudWatchCollector")
    cluster.autoscalar should be("in.ashwanthkumar.vamana2.aws.AutoScalingGroups")
    cluster.scalar should be("in.ashwanthkumar.vamana2.apps.HadoopScalar")

    val expectedMetrics = MetricsConfig(
      demand = List("map_demand", "reduce_demand"),
      supply = List("map_supply", "reduce_supply"),
      namespace = Some("HadoopAutoScaling"),
      dimensions = Map(
        "Environment" -> "staging",
        "Cluster" -> "datapipeline"
      ),
      durationInMinutes = 8 * 60 // 8 hours
    )
    cluster.metricsConfig should be(expectedMetrics)
  }
}
