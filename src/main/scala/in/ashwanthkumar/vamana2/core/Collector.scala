package in.ashwanthkumar.vamana2.core

import scala.collection.mutable

case class Point(value: Double, timestamp: Long)
case class Metric(name: String, points: List[Point])

trait Collector {
  def collectMetrics(namespace: String, metrics: List[String]): List[Metric]
}

object CollectorFactory {
  private lazy val implementations = mutable.Map[String, Collector]()

  def register(collector: Collector): Unit = {
    implementations.put(collector.getClass.getCanonicalName, collector)
  }

  def get(name: String) = implementations(name)
}