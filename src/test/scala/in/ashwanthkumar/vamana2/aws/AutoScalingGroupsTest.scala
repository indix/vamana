package in.ashwanthkumar.vamana2.aws

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient
import com.amazonaws.services.autoscaling.model.{AutoScalingGroup, DescribeAutoScalingGroupsRequest, DescribeAutoScalingGroupsResult, UpdateAutoScalingGroupRequest}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers.{be, contain, convertToAnyShouldWrapper, have}

import scala.collection.JavaConverters._

class AutoScalingGroupsTest extends FlatSpec {
  val mockClient = mock(classOf[AmazonAutoScalingClient])
  "AutoScalingGroup" should "make describeASG request for currentNodes" in {
    val request = ArgumentCaptor.forClass(classOf[DescribeAutoScalingGroupsRequest])

    val mockResult = new DescribeAutoScalingGroupsResult()
      .withAutoScalingGroups(new AutoScalingGroup().withDesiredCapacity(1))
    when(mockClient.describeAutoScalingGroups(request.capture())).thenReturn(mockResult)
    val awsAutoScalingGroup = new AutoScalingGroups(mockClient)
    val currentNodeCount = awsAutoScalingGroup.currentNodes("test-cluster")

    currentNodeCount should be(1)
    request.getValue.getAutoScalingGroupNames.asScala should have size 1
    request.getValue.getAutoScalingGroupNames.asScala should contain("test-cluster")
  }

  it should "throw error when name doesn't yield any auto scaling groups" in {
    val request = ArgumentCaptor.forClass(classOf[DescribeAutoScalingGroupsRequest])

    val mockResult = new DescribeAutoScalingGroupsResult()
    when(mockClient.describeAutoScalingGroups(request.capture())).thenReturn(mockResult)
    val awsAutoScalingGroup = new AutoScalingGroups(mockClient)

    intercept[RuntimeException] {
      awsAutoScalingGroup.currentNodes("test-cluster")
    }.getMessage should be("Autoscaling Group test-cluster not found")
  }

  it should "throw error when name yeilds more than 1 ASG" in {
    val request = ArgumentCaptor.forClass(classOf[DescribeAutoScalingGroupsRequest])

    val mockResult = new DescribeAutoScalingGroupsResult()
      .withAutoScalingGroups(new AutoScalingGroup(), new AutoScalingGroup())
    when(mockClient.describeAutoScalingGroups(request.capture())).thenReturn(mockResult)
    val awsAutoScalingGroup = new AutoScalingGroups(mockClient)

    intercept[RuntimeException] {
      awsAutoScalingGroup.currentNodes("test-cluster")
    }.getMessage should be("Multiple AutoScaling groups found for test-cluster")
  }

  it should "return true for #supportsScaleTo" in {
    new AutoScalingGroups(mockClient).supportsScaleTo should be(true)
  }

  it should "make UpdateAutoScalingGroupRequest on scaleTo" in {
    val request = ArgumentCaptor.forClass(classOf[UpdateAutoScalingGroupRequest])
    doNothing().when(mockClient).updateAutoScalingGroup(request.capture())
    val awsAutoScalingGroup = new AutoScalingGroups(mockClient)
    awsAutoScalingGroup.scaleTo("test-cluster", 1)

    request.getValue.getAutoScalingGroupName should be("test-cluster")
    request.getValue.getDesiredCapacity should be(1)
  }

  it should "throw exceptions for scaleUp and scaleDown" in {
    val aws = new AutoScalingGroups(mockClient)
    intercept[RuntimeException] {
      aws.scaleDown("test-cluster", 1)
    }.getMessage should be("use scaleTo, I don't support scaleDown")

    intercept[RuntimeException] {
      aws.scaleUp("test-cluster", 1)
    }.getMessage should be("use scaleTo, I dont' support scaleUp")
  }
}
