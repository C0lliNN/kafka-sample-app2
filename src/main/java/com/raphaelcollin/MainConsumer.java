package com.raphaelcollin;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import com.raphaelcollin.kafka.Order;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Properties;

public class MainConsumer {
    private static Logger logger = LoggerFactory.getLogger(MainConsumer.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fraud-detection-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Order> stream = builder.stream("payments");
        stream
                .peek((transactionId, order) -> System.out.println("Entering: " + transactionId + ": " + order))
                .filter((transactionId, order) -> !order.getUserId().toString().isBlank())
                .filter((transactionId, order) -> order.getNbOfItems() < 1000)
                .filter((transactionId, order) -> order.getTotalAmount() < 10000)
                .mapValues(order -> {
                    order.setUserId(order.getUserId().toString().toUpperCase(Locale.ROOT));
                    return order;
                })
                .peek((transactionId, order) -> System.out.println("Exiting: " + transactionId + ": " + order))
                .to("validated-payments");


        Topology topology = builder.build();

        KafkaStreams streams = new KafkaStreams(topology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}