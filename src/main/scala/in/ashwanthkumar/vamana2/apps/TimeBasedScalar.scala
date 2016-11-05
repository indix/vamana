package in.ashwanthkumar.vamana2.apps

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.typesafe.config.Config
import in.ashwanthkumar.vamana2.core._
import org.joda.time.{DateTime, LocalTime, Duration}


object NADemandSupply extends Demand with Supply {
  override def quantity: Double = 0.0
  override def available: Double = 0.0
}

case class TBSConfig(weekday: String, start: LocalTime, end: LocalTime, duration: Duration)
object TBSConfig {
  def fromConfig(config: Config) = {
    TBSConfig(
      weekday = config.getString("weekday"),
      start = LocalTime.parse(config.getString("start")),
      end = LocalTime.parse(config.getString("end")),
      duration = Duration.parse(config.getString("duration"))
    )
  }
}

/**
 * Scalar implementation that scales the underlying cluster at fixed time of the day / week
 */
class TimeBasedScalar(config: TBSConfig) extends Scalar[NADemandSupply.type, NADemandSupply.type] {
  def this(config: Config) {
    this(TBSConfig.fromConfig(config))
  }


  /**
   * Compute the required number of machines for this demand and supply.
   *
   * Always return *new total capacity* of the cluster. The framework
   * takes care of scaling up or down automatically.
   *
   * @param demand
   * @param supply
   * @return
   */
  override def requiredNodes(demand: NADemandSupply.type, supply: NADemandSupply.type, ctx: Context): Int = {
    val now = DateTime.now()
    
  }
  /**
   * Compute Demand from the demand metrics
   * @param metrics
   * @return
   */
  override def demand(metrics: List[Metric]): NADemandSupply.type = NADemandSupply
  /**
   * Compute Supply from the supply metrics
   * @param metrics
   * @return
   */
  override def supply(metrics: List[Metric]): NADemandSupply.type = NADemandSupply
}
