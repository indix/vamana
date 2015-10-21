package in.ashwanthkumar.vamana2.core

import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper, have}

class ConfigReaderTest extends FlatSpec {
  "ConfigReader" should "read the configuration file" in {
    val vamanaConfig = ConfigReader.load("test-clusters")
    vamanaConfig.clusters should have size 1

    val cluster = vamanaConfig.clusters.head
    cluster.name should be("Hadoop1 Staging Cluster")
    cluster.asId should be("as-hadoop-staging-spot")
    cluster.maxNodes should be(5)
    cluster.minNodes should be(1)
    cluster.collector should be("in.ashwanthkumar.vamana2.aws.CloudWatchCollector")
    cluster.autoscalar should be("in.ashwanthkumar.vamana2.aws.AutoScalingGroups")
    cluster.scalar should be("in.ashwanthkumar.vamana2.examples.HadoopScalar")

    val expectedMetrics = MetricsConfig(
      demand = List("map_count_demand", "reduce_count_demand"),
      supply = List("map_count_supply", "reduce_count_supply"),
      namespace = "HadoopStaging"
    )
    cluster.metricsConfig should be(expectedMetrics)
  }
}
