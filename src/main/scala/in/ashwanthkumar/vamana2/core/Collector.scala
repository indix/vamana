package in.ashwanthkumar.vamana2.core

case class Point(value: Double, timestamp: Long)
case class Metric(name: String, points: List[Point])

trait Collector {
  def collectMetrics(namespace: String, metrics: List[String]): List[Metric]
}
