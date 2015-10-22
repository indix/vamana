package in.ashwanthkumar.vamana2.core

import scala.collection.mutable

trait AutoScalar {
  /**
   * Current number of nodes on the AutoScalar.
   * <p />
   * This number includes the desired the state that AutoScalar must satisfy, this need not match the *actual* number
   * of instances / nodes behind the AutoScalar.
   * <p />
   * Example - In case of AWS AutoScaling this number represents the "Desired" value
   */
  def currentNodes(cluster: String): Int

  /**
   * AutoScalars like AWS AutoScaling Groups supports a value called desired which would automatically take care
   * of scaling up / scaling down based on the totalNodes.
   *
   * AutoScalar#scaleTo is supposed to do that. For systems that doesn't support this
   * AutoScalar#supportsScaleTo should return false
   */
  def scaleTo(cluster: String, totalNodes: Int)

  /**
   * Does the AutoScalar supports scaleTo?
   *
   * If the AutoScalar supports scaleTo we will not be using scaleUp / scaleDown methods.
   *
   * @see AutoScalar#scaleTo
   */
  def supportsScaleTo: Boolean

  /**
   * When AutoScalar doesn't support scaleTo, scaleUp is responsible for scaling up the cluster by <em>newNodes</em>.
   * @param newNodes New number of nodes to scale up to
   */
  def scaleUp(cluster: String, newNodes: Int)

  /**
   * When AutoScalar doesn't support scaleTo, scaleDown is responsible for scaling down the cluster by <em>nodes</em>.
   * @param nodes
   */
  def scaleDown(cluster: String, nodes: Int)
}

object AutoScalarRegistry {

  def get(name: String) = Class.forName(name).asSubclass(classOf[AutoScalar]).newInstance()

}