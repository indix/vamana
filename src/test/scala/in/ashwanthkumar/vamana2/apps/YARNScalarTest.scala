package in.ashwanthkumar.vamana2.apps

import in.ashwanthkumar.vamana2.core._
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class YARNScalarTest extends FlatSpec {

  "YARNScalar" should "return  maxNodes when there's demand and cluster is not in full capacity" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 1, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(containersPending = 10000, containersAllocated = 10, activeNodes = 1), CSupply(activeNodes = 1), mockContext)
    numberOfNodes should be(10)
  }

  it should "return the minNodes when there's no demand" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 5, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(containersPending = 0.0, containersAllocated = 0.0, activeNodes = 10), CSupply(activeNodes = 10.0), mockContext)
    numberOfNodes should be(1)
  }

  it should "return the currentSize when there's demand and cluster is already at full capacity" in {
    val scalar = new YARNScalar()
    val mockContext = context(currentSize = 10, minNodes = 1, maxNodes = 10)
    val numberOfNodes = scalar.requiredNodes(CDemand(containersPending = 100.0, containersAllocated = 100.0, activeNodes = 10.0), CSupply(activeNodes = 10.0), mockContext)
    numberOfNodes should be(10)
  }

    it should "scale down when max nodes is reduced with demand being high" in {
      val scalar = new YARNScalar()
      val mockContext = context(currentSize = 15, minNodes = 1, maxNodes = 10)
      val numberOfNodes = scalar.requiredNodes(CDemand(containersPending = 0.0, containersAllocated = 100.0, activeNodes = 15.0), CSupply(activeNodes = 15.0), mockContext)
      numberOfNodes should be(10)
    }

    it should "scale down when max nodes is reduced with demand being reduced as well" in {
      val scalar = new YARNScalar()
      val mockContext = context(currentSize = 15, minNodes = 1, maxNodes = 10)
      val numberOfNodes = scalar.requiredNodes(CDemand(containersPending = 0.0, containersAllocated = 10.0, activeNodes = 15.0), CSupply(activeNodes = 15.0), mockContext)
      numberOfNodes should be(10)
    }

    "CDemand" should "return 0 when there's no pending or allocated" in {
      val demand = CDemand(containersPending = 0.0, containersAllocated =  0.0, activeNodes = 1.0)
      demand.quantity should be(0.0)
    }

    it should "return pending by allocated when nodes = 1" in {
      val demand = CDemand(containersPending = 0.0, containersAllocated =  5.0, activeNodes = 1.0)
      demand.quantity should be(1.0)
    }

    it should "return allocated + pending / nodes when nodes > 1" in {
      val demand = CDemand(containersPending = 19995.0, containersAllocated =  5.0, activeNodes = 1.0)
      demand.quantity should be(4000.0)
    }

  def context(currentSize: Int, minNodes: Int, maxNodes: Int): Context = {
    Context(currentSize,
      Cluster("foo", "bar-asg", minNodes, maxNodes,
        MetricsConfig(Nil, Nil, None, Map(), 6 * 60), "scalar", "collector", "autoscalar")
    )
  }
}
