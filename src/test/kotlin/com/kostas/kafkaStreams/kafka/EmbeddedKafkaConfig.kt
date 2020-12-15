package com.kostas.kafkaStreams.kafka

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG as CONSUMER_BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG as PRODUCER_BOOTSTRAP_SERVERS_CONFIG
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.test.context.TestPropertySource

@Configuration
@EnableKafka
@TestPropertySource(properties = ["kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"])
class EmbeddedKafkaConfig(
    @Value("\${kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    private val consumerProperties = mapOf(
        CONSUMER_BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        GROUP_ID_CONFIG to "kafka-test-group",
        AUTO_OFFSET_RESET_CONFIG to "earliest",
        "schema.registry.url" to "n/a",
        "auto.register.schemas" to "true"
    )

    private val producerProperties = mapOf(
        PRODUCER_BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        "schema.registry.url" to "no-need"
    )

    @Bean
    fun kafkaListenerContainerFactory(registryClient: MockSchemaRegistryClient): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        val properties = consumerProperties

        factory.consumerFactory = DefaultKafkaConsumerFactory(properties, StringDeserializer(), StringDeserializer())

        return factory
    }

    @Bean
    fun kafkaTemplate(registryClient: MockSchemaRegistryClient): KafkaTemplate<String, String> {
        val properties = producerProperties

        return KafkaTemplate(DefaultKafkaProducerFactory(properties, StringSerializer(), StringSerializer()))
    }

    @Bean
    fun registryClient() = MockSchemaRegistryClient()

}
