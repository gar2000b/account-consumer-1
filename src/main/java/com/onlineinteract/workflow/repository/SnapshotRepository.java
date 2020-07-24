package com.onlineinteract.workflow.repository;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.onlineinteract.workflow.dbclient.DbSnapshotClient;
import com.onlineinteract.workflow.model.SnapshotInfo;
import com.onlineinteract.workflow.utility.JsonParser;

@Repository
public class SnapshotRepository {

	@Autowired
	DbSnapshotClient dbSnapshotClient;

	public SnapshotRepository() {
	}

	public void loadInSnapshotInfo(SnapshotInfo snapshotInfo) {
		MongoDatabase database = dbSnapshotClient.getMongoClient().getDatabase(DbSnapshotClient.DATABASE);
		Document snapshotInfoDocument = Document.parse(JsonParser.toJson(snapshotInfo));
		MongoCollection<Document> snapshotInfoCollection = database.getCollection("snapshot-info");
		snapshotInfoCollection.insertOne(snapshotInfoDocument);
		System.out.println("Snapshot Info Persisted to snapshot-info collection");
	}

	public void updateSnapshotInfo(SnapshotInfo snapshotInfo) {
		MongoDatabase database = dbSnapshotClient.getMongoClient().getDatabase(DbSnapshotClient.DATABASE);
		Document snapshotInfoDocument = Document.parse(JsonParser.toJson(snapshotInfo));
		MongoCollection<Document> snapshotInfoCollection = database.getCollection("snapshot-info");
		snapshotInfoCollection.replaceOne(new Document("_id", snapshotInfo.get_id()), snapshotInfoDocument);
		System.out.println("Account Updated in accounts collection");
	}

	public SnapshotInfo getSnapshotInfo() {
		MongoDatabase database = dbSnapshotClient.getMongoClient().getDatabase(DbSnapshotClient.DATABASE);
		MongoCollection<Document> accountsCollection = database.getCollection("snapshot-info");
		BasicDBObject query = new BasicDBObject();
		query.put("_id", "1");
		FindIterable<Document> snapshotInfoDocuments = accountsCollection.find(query);
		for (Document accountDocument : snapshotInfoDocuments) {
			System.out.println("Found: " + accountDocument.toJson());
			return JsonParser.fromJson(accountDocument.toJson(), SnapshotInfo.class);
		}

		return null;
	}
}
