package com.onlineinteract.workflow.utility;

import org.bson.Document;

public class MongoUtility {

	public static void removeEventMembers(Document document) {
		System.out.println("Removing event members");
		document.remove("eventId");
		document.remove("created");
		document.remove("eventType");
	}

	public static void removeMongoId(Document document) {
		System.out.println("Removing _id");
		document.remove("_id");
	}
}
