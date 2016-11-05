package in.ashwanthkumar.vamana2.core

import com.typesafe.config.Config

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
  def get(name: String, collectorConfig: Option[Config] = None) = {
    val classSpec: Class[_ <: Collector] = Class.forName(name).asSubclass(classOf[Collector])
    collectorConfig match {
      case Some(config) =>
        try {
          classSpec.getConstructor(classOf[Config]).newInstance(config)
        } catch {
          case notfound: NoSuchMethodException => classSpec.newInstance()
        }
      case None =>
        classSpec.newInstance()
    }
  }
}
