# mongoinserts

The Java MongoDB driver (here at ver: 3.2.0) exposes several flavours by which documents can be inserted. 
`mongoinserts.Inserts` exercises some of them while recording TPS information. 

# The data
The data used here is the [`primer-dataset`](https://raw.githubusercontent.com/mongodb/docs-assets/primer-dataset/dataset.json) (copy checked in) . It contains 25359 restaurant listings
in JSON format  

# The flavours

* `insertOne` translates to `MongoCollection.insertOne`
* `insertOnePar` a concurrent version of `insertOne` using Java 8 parallel stream
* `insertMany` translates to `MongoCollection.insertMany`
* `insertManyPar` a concurrent version of `insertMany` using Java 8 parallel stream with custom batching of the input documents