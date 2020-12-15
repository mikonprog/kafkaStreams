package com.kostas.kafkaStreams.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kostas.kafkaStreams.ListingStream
import com.kostas.kafkaStreams.streams.ListingByPostcodeProcessor
import com.kostas.kafkaStreams.streams.ListingByTypeProcessor
import org.apache.kafka.streams.StreamsBuilder
import org.springframework.stereotype.Component

@Component
class Topology (
    mapper: ObjectMapper,
    builder: StreamsBuilder,
    kafkaTopicsConfig: KafkaTopicsConfig
) {

    init {
        val listingEventsStream = ListingStream(kafkaTopicsConfig, builder).listingEventsStream()

        ListingByPostcodeProcessor(mapper, listingEventsStream).listingByPostcodeProcessor()

        ListingByTypeProcessor(mapper, listingEventsStream).listingByTypeProcesor()

    }

}
