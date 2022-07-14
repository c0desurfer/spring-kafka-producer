package ch.raciborski.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Component
public class ScheduledProducer {

    @Value("${targetTopic}")
    private String targetTopic;

    private final KafkaTemplate<Object, String> kafkaTemplate;

    public ScheduledProducer(KafkaTemplate<Object, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void tick() {
        log.debug("tick");

        ListenableFuture<SendResult<Object, String>> future = kafkaTemplate.send(targetTopic, "Hello World!");

        future.addCallback(new KafkaSendCallback<>() {
            @Override
            public void onSuccess(SendResult<Object, String> result) {
                log.debug("Sending successful. Offset is {}.", result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(KafkaProducerException e) {
                log.debug("Sending failed.", e);
                ProducerRecord<Object, String> failed = e.getFailedProducerRecord();
                log.debug("Failed record is {}", failed);
            }
        });

    }
}
