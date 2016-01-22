package mongoinserts;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static mongoinserts.TimeIt.timeIt;
import static mongoinserts.TimeIt.tps;

/**
 * Created by David Soroko on 14/01/2016.
 */
public class Inserts {

    private Env env;
    private Data data;

    private final Map<String, List<Long>> stats = new ConcurrentHashMap<>();

    public Inserts() throws Exception {
        env = new Env();
        data = env.getData();
    }

    public void run(int numberOfRounds, TimeIt.CodeBlock block) throws Exception {
        for (int i = 0; i < numberOfRounds; i++) {
            env.dropCollection();
            System.gc();
            block.invoke();
        }
    }

    public void bulkWriteOrdered() throws Exception {
        updateStats("bulkWriteOrdered", timeIt(() ->
                data.bulkWrite(new BulkWriteOptions().ordered(true))));
    }

    public void bulkWriteOrderedPar() throws Exception {
        Stream<List<WriteModel<Document>>> docs = data.batchedStream(data.documentModels());
        updateStats("bulkWriteOrderedPar", timeIt(() ->
                docs.parallel().forEach(list -> data.bulkWrite(list, new BulkWriteOptions().ordered(true)))));
    }


    public void bulkWriteUnordered() throws Exception {
        updateStats("bulkWriteUnordered", timeIt(() ->
                data.bulkWrite(new BulkWriteOptions().ordered(false))));
    }

    public void bulkWriteUnorderedPar() throws Exception {
        Stream<List<WriteModel<Document>>> docs = data.batchedStream(data.documentModels());
        updateStats("bulkWriteUnorderedPar", timeIt(() ->
                docs.parallel().forEach(list -> data.bulkWrite(list, new BulkWriteOptions().ordered(false)))));
    }


    public void insertMany() throws Exception {
        updateStats("insertMany", timeIt(() ->
                data.insertMany(data.documents())));
    }


    public void insertManyPar() throws Exception {
        Stream<List<Document>> docs = data.batchedStream(data.documents());
        updateStats("insertManyPar", timeIt(() ->
                docs.parallel().forEach(data::insertMany)));
    }

    public void insertOne() throws Exception {
        updateStats("insertOne", timeIt(() ->
                data.documents().stream().forEach(data::insertOne)));
    }

    public void insertOnePar() throws Exception {
        updateStats("insertOnePar", timeIt(() ->
                data.documents().parallelStream().forEach(data::insertOne)));

    }

    public void printStats(int skip) {
        stats.keySet().stream().sorted().forEach(testName -> {
                    int average = (int) stats.get(testName).stream().skip(skip).mapToLong(Long::longValue).average().getAsDouble();
                    System.out.printf(
                            "%s %d documents in %d msec. [%.0f doc/sec.]\n",
                            testName, data.size(), average, tps(average, data.size()));
                }
        );
    }

    private void updateStats(String testName, long duration) {
        stats.computeIfAbsent(testName, v -> new ArrayList<>()).add(duration);
    }

    public static void main(String[] args) throws Exception {
        Inserts inserts = new Inserts();

        inserts.run(30, inserts::bulkWriteOrdered);
        inserts.run(30, inserts::bulkWriteOrderedPar);

        inserts.run(30, inserts::bulkWriteUnordered);
        inserts.run(30, inserts::bulkWriteUnorderedPar);

        inserts.run(30, inserts::insertMany);
        inserts.run(30, inserts::insertManyPar);

        inserts.run(30, inserts::insertOne);
        inserts.run(30, inserts::insertOnePar);

        inserts.printStats(20);
    }
}
