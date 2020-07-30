package com.onlineinteract.workflow.dbclient;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Component
public class DbSnapshotClient {
	public static final String DATABASE = "snapshots";

	private MongoClient mongoClient;
	private ServerAddress address;
	private MongoCredential credential;

	public DbSnapshotClient() {
		System.out.println("Initializing MongoDB Client");
		String host = "localhost";
		int port = 27017;
		String user = "snapshots";
		String pwd = "password";

		address = new ServerAddress(host, port);
		credential = MongoCredential.createCredential(user, DATABASE, pwd.toCharArray());

		MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder().heartbeatConnectTimeout(20000)
				.heartbeatFrequency(10000).heartbeatSocketTimeout(20000).connectTimeout(20000).socketTimeout(20000)
				.maxConnectionIdleTime(10000).build();
		mongoClient = new MongoClient(address, credential, mongoClientOptions);
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	@PreDestroy
	private void onDestroy() {
		System.out.println("Closing MongoClient");
		mongoClient.close();
		System.out.println("MongoClient closed");
	}
}
