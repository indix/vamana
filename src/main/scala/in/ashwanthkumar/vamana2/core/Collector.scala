package in.ashwanthkumar.vamana2.core

case class Point(value: Double, timestamp: Long)
case class Metric(name: String, points: List[Point]) {
  def average = points.map(_.value).sum / points.size
  def min = points.minBy(_.value)
  def max = points.maxBy(_.value)
  def sum = points.map(_.value).sum
}

trait Collector {
  def collectMetrics(metrics: List[String], config: MetricsConfig): List[Metric]
}

object CollectorFactory {
  def get(name: String) = {
    Class.forName(name).asSubclass(classOf[Collector]).newInstance()
  }
}
