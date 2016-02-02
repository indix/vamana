package in.ashwanthkumar.vamana2.apps

import in.ashwanthkumar.vamana2.core._
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{convertToAnyShouldWrapper, be}

class HadoopScalarTest extends FlatSpec {

  "HadoopScalar" should "return  maxNodes when there's demand and cluster is not in full capacity" in {
    val scalar = new HadoopScalar()
    val mockContext = context(currentSize = 1, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(HDemand(1000.0, 500.0), HSupply(100.0, 50.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "return the minNodes when there's no demand" in {
    val scalar = new HadoopScalar()
    val mockContext = context(currentSize = 5, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(HDemand(0.0, 0.0), HSupply(100.0, 50.0), mockContext)
    numberOfNodes should be(1)
  }

  it should "return the currentSize when there's demand and cluster is already at full capacity" in {
    val scalar = new HadoopScalar()
    val mockContext = context(currentSize = 10, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(HDemand(100.0, 50.0), HSupply(100.0, 50.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "compute sum for demand from metrics" in {
    val scalar = new HadoopScalar()
    val metrics = List(
      Metric("map_demand", List(Point(10.0, 1l), Point(15.0, 2l))),
      Metric("reduce_demand", List(Point(10.0, 1l), Point(15.0, 2l)))
    )
    scalar.demand(metrics) should be(HDemand(25.0, 25.0))
  }

  it should "pick the latest metrics for supply" in {
    val scalar = new HadoopScalar()
    val metrics = List(
      Metric("map_supply", List(Point(10.0, 1l), Point(15.0, 2l))),
      Metric("reduce_supply", List(Point(10.0, 1l), Point(15.0, 2l)))
    )
    scalar.supply(metrics) should be(HSupply(15.0, 15.0))
  }

  def context(currentSize: Int, minNodes: Int, maxNodes: Int): Context = {
    Context(currentSize,
      Cluster("foo", "bar-asg", minNodes, maxNodes,
        MetricsConfig(Nil, Nil, None, Map(), 6 * 60), "scalar", "collector", "autoscalar")
    )
  }
}
