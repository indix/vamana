package in.ashwanthkumar.vamana2.core

import scala.collection.mutable

trait Demand {
  def quantity: Int
}

trait Supply {
  def available: Int
}

trait Scalar[D <: Demand, S <: Supply] {
  /**
   * Compute the required number of machines for this demand and supply
   *
   * @param demand
   * @param supply
   * @return
   */
  def requiredNodes(demand: D, supply: S, cluster: Cluster): Int

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
  private lazy val implementations = mutable.Map[String, Scalar[_, _]]()

  def registerScalar(scalar: Scalar[_, _]): Unit = {
    implementations.put(scalar.getClass.getCanonicalName, scalar)
  }

  def get(name: String) = implementations(name)
}
