package in.ashwanthkumar.vamana2.apps

import in.ashwanthkumar.vamana2.core.{MetricsConfig, Cluster, Context}
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

  def context(currentSize: Int, minNodes: Int, maxNodes: Int): Context = {
    Context(currentSize,
      Cluster("foo", "bar-asg", minNodes, maxNodes,
        MetricsConfig(Nil, Nil, None, Map(), 6 * 60), "scalar", "collector", "autoscalar")
    )
  }
}
