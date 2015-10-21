package in.ashwanthkumar.vamana2.aws

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.{Dimension, GetMetricStatisticsRequest}
import in.ashwanthkumar.vamana2.core._

import scala.collection.JavaConverters._

class CloudWatchCollector(client: AmazonCloudWatchClient) extends Collector {
  override def collectMetrics(metrics: List[String], config: MetricsConfig): List[Metric] = {
    metrics.map(metric => {
      val dimensions = config.dimensions.map(tuple => {
        val (key, value) = tuple
        new Dimension().withName(key).withValue(value)
      }).asJavaCollection

      val request = new GetMetricStatisticsRequest()
        .withMetricName(metric)
        .withDimensions(dimensions)
      config.namespace.map(request.withNamespace)

      val result = client.getMetricStatistics(request)
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
