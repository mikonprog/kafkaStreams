package com.kostas.kafkaStreams.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "kafka.topics")
data class KafkaTopicsConfig(
    val listingEvents: String,
    val priceEvents: String
)
