package in.ashwanthkumar.vamana2.aws

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest
import in.ashwanthkumar.vamana2.core._

import scala.collection.JavaConverters._

class CloudWatchCollector(client: AmazonCloudWatchClient) extends Collector {
  override def collectMetrics(namespace: String, metrics: List[String]): List[Metric] = {
    metrics.map(metric => {
      val result = client.getMetricStatistics(
        new GetMetricStatisticsRequest()
          .withNamespace(namespace)
          .withMetricName(metric)
      )
      val points = result.getDatapoints.asScala
        .map(datapoint => Point(datapoint.getSum, datapoint.getTimestamp.getTime))
        .toList
      Metric(result.getLabel, points)
    })
  }
}

object CloudWatchCollector {
  CollectorFactory.register(new CloudWatchCollector(new AmazonCloudWatchClient))
}
