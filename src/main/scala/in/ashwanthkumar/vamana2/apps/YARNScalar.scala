package in.ashwanthkumar.vamana2.apps

import com.typesafe.scalalogging.slf4j.Logger
import in.ashwanthkumar.vamana2.Vamana._
import in.ashwanthkumar.vamana2.core._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration

case class CDemand(containersPending: Double) extends Demand {
  def quantity = containersPending
}
case class CSupply(containersAllocated: Double) extends Supply {
  def available = containersAllocated
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
    else if (demand.quantity > supply.available || ctx.currentSize > ctx.cluster.maxNodes) ctx.cluster.maxNodes
    else ctx.currentSize
  }

  /**
   * @inheritdoc
   */
  override def demand(metrics: List[Metric]): CDemand = {
    val containersPending = containerMetrics(metrics, "containers_pending")
    CDemand(containersPending.sum)
  }

  /**
   * @inheritdoc
   */
  override def supply(metrics: List[Metric]): CSupply = {
    val containersAllocated = containerMetrics(metrics, "containers_allocated")
    CSupply(containersAllocated.sum)
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
