package com.kostas.kafkaStreams.util

import mu.KotlinLogging
import net.logstash.logback.marker.Markers

private val logger = KotlinLogging.logger {}

open class Producer(val topic: String, val prefix: String) {

    fun logMessageSent(id: String, partition: Int, offset: Long) {
        logger.info(
            Markers.appendEntries(
                mapOf(
                    "$prefix.id" to id,
                    "$prefix.kafka.partition" to partition,
                    "$prefix.kafka.offset" to offset
                )
            )
        ) { "Published id $id to kafka topic $topic" }
    }
}