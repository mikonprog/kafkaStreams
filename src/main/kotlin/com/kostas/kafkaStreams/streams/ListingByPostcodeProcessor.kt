package com.kostas.kafkaStreams.streams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kostas.kafkaStreams.config.StateStoreNames
import com.kostas.kafkaStreams.model.ListingSummary
import mu.KotlinLogging
import net.logstash.logback.marker.Markers.appendEntries
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.state.Stores

private val logger = KotlinLogging.logger {}

class ListingByPostcodeProcessor(
    private val mapper: ObjectMapper,
    private val listingStream: KStream<String, String>
) {

    fun listingByPostcodeProcessor(): KTable<String, Set<ListingSummary>> {
        val stringSerde = Serdes.String()

        val listingSummarySerde = Serdes.serdeFrom(
                { _, stringSet -> mapper.writeValueAsBytes(stringSet) },
                { _, bytes -> mapper.readValue<ListingSummary>(bytes)}
        )
        val listingSetSummarySerde = Serdes.serdeFrom(
                { _, stringSet -> mapper.writeValueAsBytes(stringSet) },
                { _, bytes -> mapper.readValue<Set<ListingSummary>>(bytes)}
        )
        val store = Stores.inMemoryKeyValueStore(StateStoreNames.listingByPostcode)

       return listingStream
            .peek(
                { key, value ->
                    logger.info(appendEntries(mapOf("key" to key, "type" to value::class.simpleName))) { "Processing message=[$value]" }
                }, Named.`as`("listings-by-postcode.inbound.logger")
            )
            .mapValues { value ->
                val listing = mapper.readValue(value, ListingSummary::class.java)
                listing
            }
           .selectKey { _, value -> value.postcode }
           .groupByKey(
               Grouped.with(stringSerde, listingSummarySerde)
           )
           .aggregate(
               { emptySet() },
               { _, newListingByPostcode, aggregate ->
                   val set = aggregate + newListingByPostcode
                   // logger.info { "Publishing new message onto aggregate: [$set]" }
                   set
               },
               Materialized.`as`<String, Set<ListingSummary>>(store)
                       .withKeySerde(stringSerde)
                       .withValueSerde(listingSetSummarySerde)
           )
    }
}
