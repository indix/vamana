package in.ashwanthkumar.vamana2.apps

import com.typesafe.scalalogging.slf4j.Logger
import in.ashwanthkumar.vamana2.core._
import org.slf4j.LoggerFactory

case class CDemand(containersPending: Double, containersAllocated: Double, activeNodes: Double) extends Demand {
  def quantity = {
    val nodes = math.max(activeNodes, 1.0)
    val containersPerNode = containersAllocated / nodes
    math.ceil((containersPending + containersAllocated) / math.max(containersPerNode, 1.0))
  }
}
case class CSupply(activeNodes: Double) extends Supply {
  def available = activeNodes
}

/**
  * Initial version of YARN Scalar that scales up the cluster to ClusterConfiguration.maxSize (if there's demand)
  * or scales down the cluster to ClusterConfiguration.minSize.
  *
  */
class YARNScalar extends Scalar[CDemand, CSupply] {
  private val log = Logger(LoggerFactory.getLogger(getClass))
  /**
    * @inheritdoc
    */
  override def requiredNodes(demand: CDemand, supply: CSupply, ctx: Context): Int = {
    log.info(s"Demand found is $demand")
    log.info(s"Supply found is $supply")

    if (demand.quantity == 0.0) ctx.cluster.minNodes
    else if (demand.quantity > supply.available) {
      // don't scale down if we're already scaled up further than required (causes jobs to fail if they're using Datanodes)
      // if we're just starting to scale up, scale up slowly
      val maxOfCurrentAndRequired = math.max(ctx.currentSize, demand.quantity.toInt)
      math.min(ctx.cluster.maxNodes, maxOfCurrentAndRequired)
    }
    else if (ctx.currentSize > ctx.cluster.maxNodes) ctx.cluster.maxNodes
    else ctx.currentSize
  }

  /**
    * @inheritdoc
    */
  override def demand(metrics: List[Metric]): CDemand = {
    val containersPending = containerMetrics(metrics, "containers_pending")
    val containersAllocated = containerMetrics(metrics, "containers_allocated")
    val activeNodes = containerMetrics(metrics, "active_nodes")
    CDemand(
      containersPending.sum,
      containersAllocated.sum,
      activeNodes.sum
    )
  }

  /**
    * @inheritdoc
    */
  override def supply(metrics: List[Metric]): CSupply = {
    val activeNodes = containerMetrics(metrics, "active_nodes")
    CSupply(activeNodes.sum)
  }

  private[apps] def containerMetrics(metrics: List[Metric], metricName: String): List[Double] = {
    require(metrics.map(_.name).contains(metricName), "we need " + metricName)

    val metric = metrics.filter(_.name == metricName).head
    metric.points.sortBy(_.timestamp)(Ordering[Long].reverse).map(_.value)
  }
}

object YARNScalar {
  def apply(): YARNScalar = new YARNScalar
}
