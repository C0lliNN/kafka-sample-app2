package com.raphaelcollin;

import com.raphaelcollin.kafka.Order;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

public class MainProducer {
    private static Logger logger = LoggerFactory.getLogger(MainProducer.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fraud-detection-producer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        try (Producer<String, Order> producer = new KafkaProducer<>(props)) {
            for (Order order : createOrders()) {

                ProducerRecord<String, Order> record = new ProducerRecord<>("payments", UUID.randomUUID().toString(), order);

                producer.send(record).get();

                sleep(1000);
            }

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Order> createOrders() {
        Order order1 = new Order();
        order1.setUserId("user-id-1");
        order1.setNbOfItems(10);
        order1.setTotalAmount(100F);

        Order order2 = new Order();
        order2.setUserId("");
        order2.setNbOfItems(10);
        order2.setTotalAmount(100F);

        Order order3 = new Order();
        order3.setUserId("user-id-2");
        order3.setNbOfItems(100000000);
        order3.setTotalAmount(100F);

        Order order4 = new Order();
        order4.setUserId("user-id-2");
        order4.setNbOfItems(10);
        order4.setTotalAmount(1000000000F);

        return List.of(order1, order2, order3, order4);
    }
}
