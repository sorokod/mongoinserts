package mongoinserts;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by David Soroko on 22/01/2016.
 */
public class Data {

    private List<Document> documents;
    private List<WriteModel<Document>> documentModels;
    private MongoCollection<Document> collection;

    public Data(MongoCollection<Document> collection, String filePath) throws Exception {
        this.documents = streamLines(filePath).map(Document::parse).collect(Collectors.toList());
        this.documentModels = documents.stream().map(InsertOneModel::new).collect(Collectors.toList());
        this.collection = collection;
    }

    public int size() {
        return documents.size();
    }

    public List<Document> documents() {
        return documents;
    }

    public List<WriteModel<Document>> documentModels() {
        return documentModels;
    }

    public void bulkWrite(BulkWriteOptions options) {
        bulkWrite(documentModels, options);
    }

    public void bulkWrite(List<WriteModel<Document>> docs, BulkWriteOptions options) {
        collection.bulkWrite(docs, options);
    }

    public void insertMany(List<Document> docs) {
        collection.insertMany(docs);
    }

    public void insertOne(Document doc) {
        collection.insertOne(doc);
    }

    public <T> Stream<List<T>> batchedStream(List<T> list) {
        return new BatchingSpliterator.Builder().wrap(list).batchSize(5000).stream();
    }

    private Stream<String> streamLines(String dataFile) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Path dataPath = new File(classLoader.getResource(dataFile).getFile()).toPath();
        return Files.lines(dataPath);
    }

}
