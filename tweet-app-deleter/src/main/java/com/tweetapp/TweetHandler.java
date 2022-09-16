package com.tweetapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TweetHandler implements RequestHandler<SQSEvent, Void> {

	private Logger logger = LogManager.getLogger(TweetHandler.class);

	@Override
	public Void handleRequest(SQSEvent input, Context context) {
		try {
			long a = System.currentTimeMillis();
			logger.info("Lambda Invoked !");

			List<SQSMessage> records = input.getRecords();

			InputStream is = TweetHandler.class.getClassLoader().getResourceAsStream("application.properties");

			if (is == null) {
				throw new IOException("Unable to load application.properties");
			}
			
			Properties props = new Properties();
			props.load(is);
			logger.info("mongo.url = "+props.getProperty("mongo.url"));
			logger.info("mongo.database = "+props.getProperty("mongo.database"));
			logger.info("mongo.collection = "+props.getProperty("mongo.collection"));

			ConnectionString connectionString = new ConnectionString(props.getProperty("mongo.url"));
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.build();
			MongoClient mongoClient = MongoClients.create(settings);
			MongoDatabase database = mongoClient.getDatabase(props.getProperty("mongo.database"));

			for (SQSMessage msg : records) {

				logger.info("deletion id = " + msg.getBody());

				MongoCollection<Document> collection = database.getCollection(props.getProperty("mongo.collection"));
				collection.deleteOne(new Document("_id", new ObjectId(msg.getBody())));
				logger.info("Deleted Successfully !");

			}

			long b = System.currentTimeMillis();
			logger.info("Lambda Executed in :: " + (b - a) + " ms");
			return null;
		} catch (Exception e) {
			logger.error("Error:", e);
		}
		return null;
	}

}
