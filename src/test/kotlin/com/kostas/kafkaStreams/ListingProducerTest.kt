package com.kostas.kafkaStreams

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kostas.kafkaStreams.config.KafkaTopicsConfig
import com.kostas.kafkaStreams.model.ListingSummary
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["listings.t", "prices.t"], bootstrapServersProperty = "spring.kafka.bootstrap-servers", brokerProperties = ["listeners=PLAINTEXT://localhost:9093", "port=9093"])
@ExtendWith(SpringExtension::class)
internal class ListingProducerTest {
    private val objectMapper = jacksonObjectMapper()

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var consumerProps: Map<String, Any>
    private lateinit var consumerFactory: ConsumerFactory<String, String>
    private lateinit var consumer: Consumer<String, String>

    private val listing = ListingSummary("SW10", 51.48655, -0.189434, "flat", 2, 1, false, 600000)

    private lateinit var underTest: ListingProducer

    @BeforeEach
    fun setup() {
        consumerProps = KafkaTestUtils.consumerProps("listings", "true", embeddedKafkaBroker)
        consumerFactory = DefaultKafkaConsumerFactory(consumerProps, StringDeserializer(), StringDeserializer())
        consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "listings.t")

        underTest = ListingProducer(kafkaTemplate, objectMapper, KafkaTopicsConfig("listings.t", "prices.t"))

    }

    @Test
    fun shouldPublishToTopic() {
        //When
        underTest.publishListing(listing)

        val messages = KafkaTestUtils.getRecords(consumer, 100 , 1)

        //Then
        assertThat(messages).hasSize(1)
    }
}
