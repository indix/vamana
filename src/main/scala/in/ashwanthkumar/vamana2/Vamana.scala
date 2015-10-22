package in.ashwanthkumar.vamana2

import com.typesafe.scalalogging.slf4j.Logger
import in.ashwanthkumar.vamana2.core._
import org.slf4j.LoggerFactory

object Vamana extends App {
  val logger = Logger(LoggerFactory.getLogger(getClass))
  val configFile = args(0)
  val vamanaConfig = ConfigReader.read(configFile)

  vamanaConfig.clusters.foreach(implicit cluster => {
    val collector = CollectorFactory.get(cluster.collector)
    val scalar = ScalarFactory.get(cluster.scalar).asInstanceOf[Scalar[Demand, Supply]]
    val autoscalar = AutoScalarRegistry.get(cluster.autoscalar)
    val context = Context(autoscalar, cluster)
    val metricsConfig = cluster.metricsConfig

    // Step 1 - Collect Demand and Supply Metrics using a Collector
    logInfo(s"Fetching Demand metrics")
    val demandMetrics = collector.collectMetrics(metricsConfig.demand, metricsConfig)
    logInfo(s"Fetching Supply metrics")
    val supplyMetrics = collector.collectMetrics(metricsConfig.supply, metricsConfig)

    // Step 2 - Determine the required number of nodes using the Scalar
    logInfo(s"Using ${cluster.scalar} as scalar implementation")
    val demand = scalar.demand(demandMetrics)
    val supply = scalar.supply(supplyMetrics)
    val newNodesCount = scalar.requiredNodes(demand, supply, context)
    logInfo(s"Scalar has deduced the new number of nodes for the cluster to be $newNodesCount nodes")

    // Step 3 - Update the autoscalar accordingly
    logInfo(s"Updating the current cluster size to $newNodesCount")
    autoscalar.scaleTo(cluster.asId, newNodesCount)
  })

  def logInfo(message: String)(implicit cluster: Cluster): Unit = {
    logger.info(s"[${cluster.name}] $message")
  }
}
