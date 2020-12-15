package com.kostas.kafkaStreams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.kafka.annotation.EnableKafkaStreams
import java.io.ObjectInputFilter

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableKafkaStreams
class KafkaStreamsApplication

fun main(args: Array<String>) {
	runApplication<KafkaStreamsApplication>(*args)
}
