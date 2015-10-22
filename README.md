[![Build Status](https://snap-ci.com/ashwanthkumar/vamana2/branch/master/build_image)](https://snap-ci.com/ashwanthkumar/vamana2/branch/master)

# Vamana2
[Vamana2](https://en.wikipedia.org/wiki/Vamana) is your buddy on AWS when you're managing systems behind AutoScaling Groups. It fills the missing gaps on ASG for scaling clusters.

## Motivation
I'm managing quite a number of Hadoop Cluster (across environments) whose TTs are backed by Auto Scaling Groups (ASG). 
Each cluster has its own usage patterns. Certain clusters run 24x7 while certain other clusters need to be up only during certain duration (when we have jobs running) and not always.
- We were forced to add Scale Up and Scale Down stages on the beginning and end of our job pipelines.
- Though using something like Anisble's ASG plugin made it trivial it was still a pain to add this everytime some one creates a new pipeline.
- It became a problem when we've more than 1 job pipelines sharing the same cluster, one's scale down shouldn't affect the other's runtime.

## Architecture
![Vamana2 Architecture](https://raw.githubusercontent.com/ashwanthkumar/vamana2/master/docs/vaman-architecture.png)

## Configuration
Sample configuration would be
```
vamana {
  clusters = [{
    # Name of the cluster
    name = "Hadoop1 Staging Cluster"

    # Identifier used by AutoScalar when resizing the cluster
    as-id = "as-hadoop-staging-spot"

    # Maximum number of nodes the cluster can scale upto
    max-nodes = 5

    # Minimum number of nodes in the cluster
    # We throw an RuntimeException if the Scalar returns less than this value
    min-nodes = 1

    metrics {
      # Metrics that represent your demand
      demand = ["map_count_demand", "reduce_count_demand"]

      # Metrics that represent your supply
      supply = ["map_count_supply", "reduce_count_supply"]

      # Namespace for your metrics (Optional)
      # Useful when using Amazon CloudWatch
      namespace = "Hadoop"

      # Dimension for your metrics (Optional)
      # Useful when using Amazon CloudWatch
      dimensions {
        name1 = "value1"
        name2 = "value2"
      }

      # Range of metrics to retrieve using collector
      range = "10m" # Range of metrics to retrieve
    }

    # Collector Implementation to use
    collector = "in.ashwanthkumar.vamana2.aws.CloudWatchCollector"

    # Autoscalar Implementation to use
    autoscalar = "in.ashwanthkumar.vamana2.aws.AutoScalingGroups"

    # Scalar Implementation to use
    scalar = "in.ashwanthkumar.vamana2.examples.HadoopScalar"
  }]
}
```

## Features
- [x] Pluggable Metric Collector
  - [x] Amazon CloudWatch
- [x] Pluggable Scalar
  - [ ] Hadoop1
- [ ] Pluggable AutoScalar
  - [x] AutoScaling on AWS
  - [ ] AutoScaler on GCE
  - [ ] [Vamana1](http://github.com/ashwanthkumar/vamana) for others?

## References / Inspirations
- http://techblog.netflix.com/2013/11/scryer-netflixs-predictive-auto-scaling.html - Closed source
- http://www.qubole.com/blog/product/industrys-first-auto-scaling-hadoop-clusters/ - Paid service

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
