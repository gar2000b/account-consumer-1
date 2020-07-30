package com.onlineinteract.workflow.domain.account.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.onlineinteract.workflow.dbclient.DbClient;
import com.onlineinteract.workflow.domain.account.v1.AccountV1;
import com.onlineinteract.workflow.utility.JsonParser;
import com.onlineinteract.workflow.utility.MongoUtility;

@Repository
public class AccountRepository {

	@Autowired
	DbClient dbClient;

	public AccountRepository() {
	}

	public void createAccount(AccountV1 accountV1) {
		MongoDatabase database = dbClient.getMongoClient().getDatabase(DbClient.DATABASE);
		Document accountDocument = Document.parse(accountV1.toString());
		MongoCollection<Document> accountsCollection = database.getCollection("accounts");
		accountsCollection.insertOne(accountDocument);
//		System.out.println("Account Persisted to accounts collection");
	}

	public void updateAccount(AccountV1 accountV1) {
		MongoDatabase database = dbClient.getMongoClient().getDatabase(DbClient.DATABASE);
		Document accountDocument = Document.parse(accountV1.toString());
		MongoCollection<Document> accountsCollection = database.getCollection("accounts");
		accountsCollection.replaceOne(new Document("id", accountV1.getId().toString()), accountDocument);
//		System.out.println("Account Updated in accounts collection");
	}

	public AccountV1 getAccount(String accountId) {
		MongoDatabase database = dbClient.getMongoClient().getDatabase(DbClient.DATABASE);
		MongoCollection<Document> accountsCollection = database.getCollection("accounts");
		BasicDBObject query = new BasicDBObject();
		query.put("id", accountId);
		FindIterable<Document> accountDocuments = accountsCollection.find(query);
		for (Document accountDocument : accountDocuments) {
			System.out.println("Found: " + accountDocument.toJson());
			MongoUtility.removeMongoId(accountDocument);
			return JsonParser.fromJson(accountDocument.toJson(), AccountV1.class);
		}

		return null;
	}

	public String getAllAccounts() {
		MongoDatabase database = dbClient.getMongoClient().getDatabase(DbClient.DATABASE);
		MongoCollection<Document> accountsCollection = database.getCollection("accounts");
		FindIterable<Document> accountDocumentsIterable = accountsCollection.find();
		List<Document> accountDocuments = new ArrayList<>();
		for (Document accountDocument : accountDocumentsIterable) {
			System.out.println("Removing _id from account");
			MongoUtility.removeMongoId(accountDocument);
			accountDocuments.add(accountDocument);
		}
		String allAccounts = StreamSupport.stream(accountDocuments.spliterator(), false).map(Document::toJson)
				.collect(Collectors.joining(", ", "[", "]"));

		return allAccounts;
	}
	
	public void removeAllDocuments() {
		MongoDatabase database = dbClient.getMongoClient().getDatabase(DbClient.DATABASE);
		MongoCollection<Document> accountsCollection = database.getCollection("accounts");
		accountsCollection.deleteMany(new BasicDBObject());
	}
}
