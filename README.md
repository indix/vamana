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
  # Configuration for each cluster goes in here
  clusters = [{
    # Name of the cluster (Optional)
    name = "Hadoop1 Staging Cluster"
    # Autoscaling group backing the Cluster
    asg = "as-hadoop-staging-spot"
    metrics {
      # Demand related metrics
      demand = ["map_count_demand", "reduce_count_demand"]
      # Supply related metrics
      supply = ["map_count_supply", "reduce_count_supply"]
      # Metric Source
      source = "cloudwatch"
      # Scalar implementation for this cluster
      scalar = "in.ashwanthkumar.vamana2.examples.HadoopScalar"
    }
  }]
}
```

## Features
- [x] Pluggable Metric Collector
- [x] Pluggable Scalar
- [ ] Pluggable AutoScalar
  - [x] AutoScaling on AWS
  - [ ] AutoScaler on GCE
  - [ ] [Vamana1](http://github.com/ashwanthkumar/vamana) for others?

## License
Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
