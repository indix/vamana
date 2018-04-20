package in.ashwanthkumar.vamana2.apps

import in.ashwanthkumar.vamana2.core._
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class YARNScalarTest extends FlatSpec {

  "YARNScalar" should "return  maxNodes when there's demand and cluster is not in full capacity" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 1, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(1000.0), CSupply(100.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "return the minNodes when there's no demand" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 5, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(0.0), CSupply(100.0), mockContext)
    numberOfNodes should be(1)
  }

  it should "return the currentSize when there's demand and cluster is already at full capacity" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 10, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(100.0), CSupply(100.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "scale down when max nodes is reduced with demand being high" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 15, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(100.0), CSupply(100.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "scale down when max nodes is reduced with demand being reduced as well" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 15, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(130.0), CSupply(150.0), mockContext)
    numberOfNodes should be(10)
  }

  it should "compute sum for demand from metrics" in {
    val scalar = new YARNScalar()
    val metrics = List(
      Metric("containers_pending", List(Point(10.0, 1l), Point(15.0, 2l)))
    )
    scalar.demand(metrics) should be(CDemand(25.0))
  }

  it should "pick the latest metrics for supply" in {
    val scalar = new YARNScalar()
    val metrics = List(
      Metric("containers_allocated", List(Point(10.0, 1l), Point(15.0, 2l)))
    )
    scalar.supply(metrics) should be(CSupply(25.0))
  }

  def context(currentSize: Int, minNodes: Int, maxNodes: Int): Context = {
    Context(currentSize,
      Cluster("foo", "bar-asg", minNodes, maxNodes,
        MetricsConfig(Nil, Nil, None, Map(), 6 * 60), "scalar", "collector", "autoscalar")
    )
  }
}
