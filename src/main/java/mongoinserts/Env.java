package mongoinserts;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


/**
 * Created by David Soroko on 21/01/2016.
 */
public class Env {
    private static final String DB_NAME = "test";
    private static final String COLLECTION_NAME = "restaurants";
    private static final String DATA_FILE = "primer-dataset.json";

    private MongoDatabase db;
    private MongoCollection<Document> collection;

    private final Data data;


    public Env() throws Exception {
        db = new MongoClient().getDatabase(DB_NAME);
        data = new Data(db.getCollection(COLLECTION_NAME), DATA_FILE);
    }


    public Data getData() {
        return data;
    }

    public void dropCollection() {
        db.getCollection(COLLECTION_NAME).drop();
    }


}
