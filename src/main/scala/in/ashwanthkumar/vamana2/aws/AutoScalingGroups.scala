package in.ashwanthkumar.vamana2.aws

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.autoscaling.model.{DescribeAutoScalingGroupsRequest, UpdateAutoScalingGroupRequest}
import in.ashwanthkumar.vamana2.core.{AutoScalarRegistry, AutoScalar}

import scala.collection.JavaConverters._

class AutoScalingGroups(client: AmazonAutoScalingClient) extends AutoScalar {
  /**
   * @inheritdoc
   */
  override def currentNodes(cluster: String): Int = {
    val asgs = client.describeAutoScalingGroups(
      new DescribeAutoScalingGroupsRequest()
        .withAutoScalingGroupNames(cluster)
    ).getAutoScalingGroups.asScala.toList

    asgs match {
      case x if x.isEmpty => throw new RuntimeException(s"Autoscaling Group $cluster not found")
      case x if x.size > 1 => throw new RuntimeException(s"Multiple AutoScaling groups found for $cluster")
      case x :: Nil => x.getDesiredCapacity
    }
  }
  /**
   * @inheritdoc
   */
  override def supportsScaleTo: Boolean = true

  /**
   * @inheritdoc
   */
  override def scaleTo(cluster: String, totalNodes: Int): Unit = {
    client.updateAutoScalingGroup(new UpdateAutoScalingGroupRequest()
      .withAutoScalingGroupName(cluster)
      .withDesiredCapacity(totalNodes)
    )
  }

  /**
   * @inheritdoc
   */
  override def scaleDown(cluster: String, nodes: Int): Unit = throw new RuntimeException("use scaleTo, I don't support scaleDown")

  /**
   * @inheritdoc
   */
  override def scaleUp(cluster: String, newNodes: Int): Unit = throw new RuntimeException("use scaleTo, I dont' support scaleUp")
}

object AutoScalingGroups {
  AutoScalarRegistry.register(new AutoScalingGroups(new AmazonAutoScalingClient))
}
