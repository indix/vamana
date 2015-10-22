package in.ashwanthkumar.vamana2.apps

import in.ashwanthkumar.vamana2.core._
import org.joda.time.DateTime

case class HDemand(map: Double, reduce: Double) extends Demand {
  def quantity = map + reduce
}
case class HSupply(map: Double, reduce: Double) extends Supply {
  def available = map + reduce
}

/**
 * Initial version of HadoopScalar that scales up the cluster to ClusterConfiguration.maxSize (if there's demand)
 * or scales down the cluster to ClusterConfiguration.minSize.
 *
 * We monitor for scale down as no demand for the last 30 min.
 *
 */
class HadoopScalar extends Scalar[HDemand, HSupply] {
  /**
   * @inheritdoc
   */
  override def requiredNodes(demand: HDemand, supply: HSupply, ctx: Context): Int = {
    // If the demand is nothing, scale down to cluster min size
    // If the cluster is running with min capacity and demand > supply, scale it up to max size
    // else keep the cluster intact
    if (demand.quantity == 0.0) ctx.cluster.minNodes
    else if (ctx.currentSize == ctx.cluster.maxNodes) ctx.currentSize
    else if (demand.quantity > supply.available && ctx.currentSize < ctx.cluster.maxNodes) ctx.cluster.maxNodes
    else ctx.currentSize
  }

  /**
   * @inheritdoc
   */
  override def demand(metrics: List[Metric]): HDemand = {
    val (mapDemandOverLastNmin, reduceDemandOverLastNmin) = mapAndReduceMetrics(metrics, "map_demand", "reduce_demand", 30)
    HDemand(mapDemandOverLastNmin, reduceDemandOverLastNmin)
  }

  /**
   * @inheritdoc
   */
  override def supply(metrics: List[Metric]): HSupply = {
    val (mapDemandOverLastNmin, reduceDemandOverLastNmin) = mapAndReduceMetrics(metrics, "map_supply", "reduce_supply", 30)
    HSupply(mapDemandOverLastNmin, reduceDemandOverLastNmin)
  }

  private def mapAndReduceMetrics(metrics: List[Metric], mapMetric: String, reduceMetric: String, minute: Int): (Double, Double) = {
    val metricsInDemand = metrics.map(_.name).toSet
    require(Set(mapMetric, reduceMetric).subsetOf(metricsInDemand), "we need " + mapMetric + " and " + reduceMetric)

    val mapDemand = metrics.filter(_.name == mapMetric).head
    val reduceDemand = metrics.filter(_.name == reduceMetric).head

    val now = DateTime.now()
    val duration = minute * 60 * 1000 // min in millis
    val mapDemandOverLastNmin = mapDemand.points
        .filter(p => math.abs(now.getMillis - p.timestamp) <= duration)
        .map(_.value)
        .sum
    val reduceDemandOverLastNmin = reduceDemand.points
      .filter(p => math.abs(now.getMillis - p.timestamp) <= duration)
      .map(_.value)
      .sum
    (mapDemandOverLastNmin, reduceDemandOverLastNmin)
  }
}

object HadoopScalar {
  def apply(): HadoopScalar = new HadoopScalar
}
