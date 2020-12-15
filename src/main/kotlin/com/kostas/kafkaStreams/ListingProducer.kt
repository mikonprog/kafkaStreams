package com.kostas.kafkaStreams

import com.fasterxml.jackson.databind.ObjectMapper
import com.kostas.kafkaStreams.config.KafkaTopicsConfig
import com.kostas.kafkaStreams.model.ListingSummary
import com.kostas.kafkaStreams.util.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListingProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    kafkaTopicsConfig: KafkaTopicsConfig
) : Producer(topic = kafkaTopicsConfig.listingEvents, prefix = "kafkaStreams.listings") {

    fun publishListing(listing: ListingSummary) = publish(UUID.randomUUID().toString(), objectMapper.writeValueAsString(listing))

    private fun publish(key: String, value: String) {
        val record = ProducerRecord(topic, key, value)
        val result = kafkaTemplate.send(record).also { kafkaTemplate.flush() }.get()

        logMessageSent(key, result.recordMetadata.partition(), result.recordMetadata.offset())

    }
}
