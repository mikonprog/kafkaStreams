package com.kostas.kafkaStreams

import com.kostas.kafkaStreams.config.KafkaTopicsConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream

class ListingStream(
    private val kafkaTopicsConfig: KafkaTopicsConfig,
    private val builder: StreamsBuilder
) {
    fun listingEventsStream(): KStream<String, String> {
        val stringSerde = Serdes.String()
        return builder
                .stream(kafkaTopicsConfig.listingEvents, Consumed.with(stringSerde, stringSerde))
    }
}
