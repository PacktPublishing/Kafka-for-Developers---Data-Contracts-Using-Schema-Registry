package com.learnavro.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Properties;

public class CoffeeOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(CoffeeOrderConsumer.class);
    private static final String COFFEE_ORDERS = "coffee-orders";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "coffee.consumer");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(properties);
        consumer.subscribe(Collections.singletonList(COFFEE_ORDERS));
        log.info("Consumer Started");
        while(true){
            ConsumerRecords<String, byte[]> records  = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, byte[]> record : records){

                try{
                    CoffeeOrder coffeeOrder = CoffeeOrder(record.value());
                    log.info("Consumed Message , key : {} , value : {}", record.key(), coffeeOrder.toString());

                    //ZoneId.SHORT_IDS
                    var utcDateTime = LocalDateTime.ofInstant(coffeeOrder.getOrderedTime(), ZoneOffset.UTC);
                    var cstDateTime = LocalDateTime.ofInstant(coffeeOrder.getOrderedTime(), ZoneId.of("America/Chicago"));
                    log.info("utcDateTime: {} , cstDateTime : {} ",utcDateTime,  cstDateTime);

                }catch (Exception e){
                    log.error("Exception is : {} ", e.getMessage(), e);
                }
            }
        }

    }

    private static CoffeeOrder CoffeeOrder(byte[] array) throws IOException {

        return CoffeeOrder.fromByteBuffer(ByteBuffer.wrap(array));

    }
}
