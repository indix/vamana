package in.ashwanthkumar.vamana2.aws

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.{StandardUnit, Statistic, Dimension, GetMetricStatisticsRequest}
import in.ashwanthkumar.vamana2.core._
import org.joda.time.DateTime

import scala.collection.JavaConverters._

class CloudWatchCollector(client: AmazonCloudWatchClient) extends Collector {

  def this() {
   this(new AmazonCloudWatchClient())
  }

  override def collectMetrics(metrics: List[String], config: MetricsConfig): List[Metric] = {
    metrics.map(metric => {
      val dimensions = config.dimensions.map(tuple => {
        val (key, value) = tuple
        new Dimension().withName(key).withValue(value)
      }).asJavaCollection

      val timeNow = now
      val request = new GetMetricStatisticsRequest()
        .withMetricName(metric)
        .withDimensions(dimensions)
        .withStartTime(timeNow.minusMinutes(config.durationInMinutes).toDate)
        .withEndTime(timeNow.toDate)
      config.namespace.map(request.withNamespace)

      val result = client.getMetricStatistics(request)
      val points = result.getDatapoints.asScala
        .map(datapoint => Point(datapoint.getSum, datapoint.getTimestamp.getTime))
        .toList
      Metric(result.getLabel, points)
    })
  }

  def now = DateTime.now()
}

object CloudWatchCollector {
  def apply(): CloudWatchCollector = new CloudWatchCollector(new AmazonCloudWatchClient)
}
