package com.onlineinteract.workflow.domain.account.bus;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.onlineinteract.workflow.config.ApplicationProperties;
import com.onlineinteract.workflow.domain.account.AccountEvent;
import com.onlineinteract.workflow.domain.account.repository.AccountRepository;
import com.onlineinteract.workflow.model.SnapshotInfo;
import com.onlineinteract.workflow.model.SnapshotInfo.Domain;
import com.onlineinteract.workflow.model.SnapshotInfo.Version;
import com.onlineinteract.workflow.repository.SnapshotRepository;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

@Component
public class Consumer {

	private static final String ACCOUNT_EVENT_TOPIC = "account-event-topic";

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	SnapshotRepository snapshotRepository;

	private KafkaConsumer<String, AccountEvent> consumer;
	private boolean runningFlag = false;
	private long beginSnapshotOffset;

	private Set<TopicPartition> assignment;

	@PostConstruct
	public void startConsumer() {
		if (applicationProperties.isStandupNewService()) {
			System.out.println("**** Standing up new service and reconstituting state from last known snapshot ****");
			createConsumer();
			reconstituteState();
			processRecords();
		} else {
			System.out.println("**** Continuing to process from current position ****");
			createConsumer();
			processRecords();
		}
	}

	private void createConsumer() {
		Properties buildProperties = buildConsumerProperties();
		consumer = new KafkaConsumer<>(buildProperties);
		consumer.subscribe(Arrays.asList(ACCOUNT_EVENT_TOPIC));
	}

	private void reconstituteState() {
		accountRepository.removeAllDocuments();
		determineBeginSnapshotOffset();
		if (beginSnapshotOffset > 0)
			reconstitutePreviousSnapshot();

		consumer.poll(Duration.ofMillis(0));
		for (TopicPartition partition : consumer.assignment())
			consumer.seek(partition, beginSnapshotOffset);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		runningFlag = true;
		System.out.println("Spinning up kafka account consumer");
		while (runningFlag) {
			ConsumerRecords<String, AccountEvent> records = consumer.poll(Duration.ofMillis(1000));
//			System.out.println("*** records count 2: " + records.count());
			for (ConsumerRecord<String, AccountEvent> consumerRecord : records) {
//				System.out.println("Consuming event from account-event-topic with id/key of: " + consumerRecord.key());
				AccountEvent accountEvent = (AccountEvent) consumerRecord.value();
				if (accountEvent.getEventType().toString().contains("AccountCreatedEvent"))
					accountRepository.createAccount(accountEvent.getV1());
				if (accountEvent.getEventType().toString().contains("AccountUpdatedEvent"))
					accountRepository.updateAccount(accountEvent.getV1());
				if (!(accountEvent.getEventType().toString().contains("SnapshotBeginEvent")
						|| accountEvent.getEventType().toString().contains("SnapshotEvent")
						|| accountEvent.getEventType().toString().contains("SnapshotEndEvent"))) {
				}
			}
			if (records.count() == 0) {
				runningFlag = false;
				System.out.println("**** state fully re-constituted ****");
			}
		}
		System.out.println("Shutting down kafka account consumer");
	}

	private void reconstitutePreviousSnapshot() {
		consumer.poll(Duration.ofMillis(0));
		for (TopicPartition partition : consumer.assignment())
			consumer.seek(partition, beginSnapshotOffset);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		runningFlag = true;
		System.out.println("Spinning up kafka account consumer to reconstitute previous snapshot prior to "
				+ "replaying events on top to create new snapshot");
		while (runningFlag) {
			ConsumerRecords<String, AccountEvent> records = consumer.poll(Duration.ofMillis(1000));
			System.out.println("*** records count 1: " + records.count());
			for (ConsumerRecord<String, AccountEvent> consumerRecord : records) {
				System.out.println("Consuming event from account-event-topic with id/key of: " + consumerRecord.key());
				AccountEvent accountEvent = (AccountEvent) consumerRecord.value();
				if (accountEvent.getEventType().toString().contains("SnapshotBeginEvent")
						&& accountEvent.getVersion() == 1)
					System.out.println("Snapshot begin event detected");
				if (accountEvent.getEventType().toString().contains("SnapshotEvent") && accountEvent.getVersion() == 1)
					accountRepository.createAccount(accountEvent.getV1());
				if (accountEvent.getEventType().toString().contains("SnapshotEndEvent")
						&& accountEvent.getVersion() == 1) {
					System.out.println("Snapshot end event detected");
					return;
				}
			}
			if (records.count() == 0)
				runningFlag = false;
		}
	}

	private void processRecords() {
		consumer.poll(Duration.ofMillis(0));
		assignment = consumer.assignment();
		// consumer.seekToBeginning(consumer.assignment());
		runningFlag = true;
		System.out.println("Spinning up kafka account consumer");
		new Thread(() -> {
			while (runningFlag) {
				ConsumerRecords<String, AccountEvent> records;
				try {
					records = consumer.poll(Duration.ofMillis(1000));
				} catch (Error e) {
					System.out.println("There was a problem consuming the next record");
					continue;
				}
				for (ConsumerRecord<String, AccountEvent> consumerRecord : records) {
					try {
//						if (processPartitionOffset(consumerRecord))
//							continue;
						consumer.commitSync();
					} catch (Error e) {
						System.out.println("There was a problem committing the offset");
						continue;
					}
					AccountEvent accountEvent = (AccountEvent) consumerRecord.value();
					System.out.println("Consuming event from customer-event-topic with id/key of: "
							+ consumerRecord.key() + " - offset: " + consumerRecord.offset() + " - partition: "
							+ consumerRecord.partition() + " at " + new Date().getTime() + " - event type: "
							+ accountEvent.getEventType().toString() + " - opening balance: "
							+ accountEvent.getV1().getOpeningBalance() + " - name: " + accountEvent.getV1().getName());
					if (accountEvent.getEventType().toString().contains("AccountCreatedEvent"))
						accountRepository.createAccount(accountEvent.getV1());
					if (accountEvent.getEventType().toString().contains("AccountUpdatedEvent"))
						accountRepository.updateAccount(accountEvent.getV1());
				}
			}
			shutdownConsumerProducer();
			System.out.println("Shutting down kafka account consumer");
		}).start();
	}

	private boolean processPartitionOffset(ConsumerRecord<String, AccountEvent> consumerRecord) {
		if (!recordContainsPartition(consumerRecord)) {
			TopicPartition topicPartition = fetchTopicPartition(consumerRecord);
			if (topicPartition == null) {
				return false;
			}
			Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(assignment);
			OffsetAndMetadata offsetAndMetadata = committed.get(topicPartition);
			if (offsetAndMetadata == null) {
				System.out.println("offsetAndMetadata is null at partition: " + topicPartition.partition());
				consumer.seek(topicPartition, 0);
				return true;
			}
			long lastCommittedOffset = offsetAndMetadata.offset();
			consumer.seek(topicPartition, lastCommittedOffset);
			System.out.println("**** processed partition offset against partition: " + topicPartition.partition()
					+ " at offset position: " + lastCommittedOffset + " ****");
			return true;
		}
		return false;
	}

	private TopicPartition fetchTopicPartition(ConsumerRecord<String, AccountEvent> consumerRecord) {
		for (TopicPartition topicPartition : assignment) {
			if (consumerRecord.partition() == topicPartition.partition()) {
				return topicPartition;
			}
		}
		return null;
	}

	private boolean recordContainsPartition(ConsumerRecord<String, AccountEvent> consumerRecord) {
		for (TopicPartition topicPartition : assignment) {
			if (consumerRecord.partition() == topicPartition.partition()) {
				return true;
			}
		}
		assignment = consumer.assignment();
		return false;
	}

	private void determineBeginSnapshotOffset() {
		SnapshotInfo snapshotInfo = snapshotRepository.getSnapshotInfo();
		Domain accountsDomain = snapshotInfo.getDomains().get("accounts");
		if (accountsDomain == null) {
			beginSnapshotOffset = 0;
		} else {
			List<Version> versions = accountsDomain.getVersions();
			for (Version version : versions) {
				if (version.getVersion() == 1) {
					beginSnapshotOffset = version.getEndSnapshotOffset() + 1;
					return;
				}
			}
		}
	}

	@PreDestroy
	public void shutdownConsumerProducer() {
		System.out.println("*** consumer shutting down");
		consumer.close();
	}

	private Properties buildConsumerProperties() {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "account-event-topic-group-v1");
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
		properties.put("schema.registry.url", "http://localhost:8081");
		properties.put("specific.avro.reader", "true");
		return properties;
	}
}
