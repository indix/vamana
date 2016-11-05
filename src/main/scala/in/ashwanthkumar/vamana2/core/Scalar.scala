package in.ashwanthkumar.vamana2.core

import com.typesafe.config.Config

import scala.collection.mutable

trait Demand {
  def quantity: Double
}

trait Supply {
  def available: Double
}

case class Context(currentSize: Int, cluster: Cluster)
object Context {
  def apply(autoScalar: AutoScalar, cluster: Cluster): Context = {
    Context(autoScalar.currentNodes(cluster.asId), cluster)
  }
}

trait Scalar[D <: Demand, S <: Supply] {
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
  def requiredNodes(demand: D, supply: S, ctx: Context): Int

  /**
   * Compute Demand from the demand metrics
   * @param metrics
   * @return
   */
  def demand(metrics: List[Metric]): D

  /**
   * Compute Supply from the supply metrics
   * @param metrics
   * @return
   */
  def supply(metrics: List[Metric]): S
}

object ScalarFactory {
  def get(name: String, scalarConfig: Option[Config] = None) = {
    val classSpec: Class[_] = Class.forName(name).asSubclass(classOf[Scalar[_, _]])
    scalarConfig match {
      case Some(config) =>
        try {
          classSpec.getConstructor(classOf[Config]).newInstance(config)
        } catch {
          case notfound: NoSuchMethodException => classSpec.newInstance()
        }

      case None => classSpec.newInstance()
    }
  }
}
