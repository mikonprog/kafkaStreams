package com.kostas.kafkaStreams.streams

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kostas.kafkaStreams.model.ListingSummary
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties

internal class ListingByTypeProcessorTest {
    private val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val schemaRegistry = "schema.registry.url" to "mock"

    private val stringSerde = Serdes.String()
    private val storeKey = "flat"
    private val listing = ListingSummary("SW10", 51.48655, -0.189434, "flat", 2, 1, false, 600000)

    private lateinit var testDriver: TopologyTestDriver
    private lateinit var listingsInputTopic: TestInputTopic<String, String>

    @BeforeEach
    fun setup() {
        val builder = StreamsBuilder()

        val config: Properties = mapOf(
                StreamsConfig.APPLICATION_ID_CONFIG to "kafka-streams",
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "mock:8888",
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.StringSerde::class.java.name,
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.StringSerde::class.java.name,
                schemaRegistry
        ).toProperties()

        val stream =
                builder.stream<String, String>("listings.events", Consumed.with(stringSerde, stringSerde))

        val processor = ListingByTypeProcessor(mapper, stream)

        processor.listingByTypeProcesor()

        val topology = builder.build()

        testDriver = TopologyTestDriver(topology, config)
        listingsInputTopic = testDriver.createInputTopic("listings.events", stringSerde.serializer(), stringSerde.serializer())

    }

    @AfterEach
    fun afterEach() {
        testDriver.close()
    }

    @Test
    fun `Listing is added to the store`() {
        val record = mapper.writeValueAsString(listing)

        listingsInputTopic.pipeInput(record)

        val listingsByTypeStore =
                testDriver.getKeyValueStore<String, Set<ListingSummary>>("listing-by-type")

        val listings = listingsByTypeStore.get(storeKey)

        Assertions.assertThat(listings).hasSize(1)
        Assertions.assertThat(listings.first().latitude).isEqualTo(51.48655)
        Assertions.assertThat(listings.first().longitude).isEqualTo(-0.189434)
    }
}
