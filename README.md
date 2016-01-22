# mongoinserts
The Java MongoDB driver (here at ver: 3.2.0) exposes several flavours by which documents can be inserted. `mongoinserts.Inserts` exercises some of them while recording TPS information. 

## The data
The data used here is the [`primer-dataset`](https://raw.githubusercontent.com/mongodb/docs-assets/primer-dataset/dataset.json) (copy checked in) . It contains 25359 restaurant listings
in JSON format  

## The flavours

* `insertOne` translates to `MongoCollection.insertOne`
* `insertOnePar` a concurrent version of `insertOne` using Java 8 parallel stream
* `insertMany` translates to `MongoCollection.insertMany`
* `insertManyPar` a concurrent version of `insertMany` using Java 8 parallel stream with custom batching of the input documents
* `bulkWriteOrdered` translates to `MongoCollection.bulkWrite` with `BulkWriteOptions().ordered(true)`
* `bulkWriteOrderedPar` a concurrent version of `bulkWriteOrdered`using Java 8 parallel stream with custom batching of the input documents
* `bulkWriteUnOrdered` translates to `MongoCollection.bulkWrite` with `BulkWriteOptions().ordered(false)`
* `bulkWriteUnOrderedPar` a concurrent version of `bulkWriteUnOrdered`using Java 8 parallel stream with custom batching of the input documents

## Methodology
Each flavour is run multiple times, before each run the collection is dropped. The duration of each run is recorded and the last N runs are averaged to produce a TPS number.    

## Numbers

The absolute values are not very useful as they depend on hardware to a very large degree (if you are **really** curious I get between 10k and 70k doc/sec depending on the flavour).
Consequently all results here are normalized by the slowest result which is `insertOne`:

```
insertOne              1.0
insertOnePar           3.4
insertMany             2.6
insertManyPar          6.2
bulkWriteUnordered     2.5
bulkWriteUnorderedPar  5.7
bulkWriteOrdered       2.7
bulkWriteOrderedPar    6.0
```

## Observations
1. In every case the concurrent version produces better throughput then the serial.
2. `bulkWriteOrdered` performs (slightly) better then `bulkWriteUnOrdered` this remains the case for the parallel versions as well. 
3. `insertMany` is similar to the bulk operations and is better when done in parallel. 
 