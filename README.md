# OLA3_DB

## Part 1 Optimistic & Pessimistic Concurrency Control

#### 1.0 Implement Optimistic Concurrency Control for Tournament Updates

##### 10 threads

| Metric                   | Optimistic  | Pessimistic |
|--------------------------|-------------|-------------|
| Execution Time (ms)      | 1509 - 1686 | 1755 - 2897 |
| Transaction Success Rate | 1 of 10     | 10 of 10    |
| Lock Contention          | NONE        | HIGH        |
| Best use case            | READ-HEAVY  | WRITE-HEAVY       |    

##### 50 threads

| Metric                   | Optimistic  | Pessimistic |
|--------------------------|-------------|-------------|
| Execution Time (ms)      | 2047 - 2884 | 1874 - 7053 |
| Transaction Success Rate | 1 of 50     | 50 of 50    |
| Lock Contention          | NONE        | HIGH        |
| Best use case            | READ-HEAVY  | WRITE-HEAVY |



## Part 2a Denormalization & Partitions

## Part 2b Query Optimization
