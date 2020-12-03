package com.kostas.kafkaStreams.metadata

import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.state.HostInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.config.StreamsBuilderFactoryBean

internal class MetadataServiceTest {
    private val streamsBuilderFactoryBean = mockk<StreamsBuilderFactoryBean>()

    private val kafkaStreams = mockk<KafkaStreams>()

    private val kafkaProperties = mockk<KafkaProperties>()

    private val properties = mapOf("application.server" to "localhost:9999")

    private val stringSerdesSerializer = Serdes.String().serializer()

    private lateinit var underTest: MetadataService

    @BeforeEach
    fun setup() {
        every { streamsBuilderFactoryBean.kafkaStreams } returns kafkaStreams
        every { kafkaProperties.streams.properties } returns properties
        underTest = MetadataService(streamsBuilderFactoryBean, kafkaProperties)
    }

    @Nested
    inner class MetadataForStoreAndKey {

        @Test
        fun `should return metadata for a store with key`() {
            val streamsMetadata = KeyQueryMetadata(HostInfo("localhost", 9999), emptySet(), 0)
            every { kafkaStreams.queryMetadataForKey(any(), eq("key"), eq(stringSerdesSerializer)) } returns streamsMetadata

            val actual = underTest.streamsMetadataForStoreAndKey("node-by-id-store", "key", stringSerdesSerializer)

            assertThat(actual.host).isEqualTo("localhost")
            assertThat(actual.port).isEqualTo(9999)
            assertThat(actual.isThisHost).isEqualTo(true)
        }

        @Test
        fun `should throw kafka metadata exception when it can not find host for store and key`() {
            val streamsMetadata = KeyQueryMetadata(HostInfo("unavailable", -1), emptySet(), 0)
            every { kafkaStreams.queryMetadataForKey(any(), eq("key"), eq(stringSerdesSerializer)) } returns streamsMetadata

            assertThatThrownBy { underTest.streamsMetadataForStoreAndKey("node-by-type-store", "key", stringSerdesSerializer) }
                    .isInstanceOf(Exception::class.java)
                    .hasMessageContaining("Could not find metadata for store: node-by-type-store with key key")
        }
    }
}
