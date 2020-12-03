package com.kostas.kafkaStreams

import arrow.core.Either
import com.kostas.kafkaStreams.metadata.HostStoreInfo
import com.kostas.kafkaStreams.metadata.MetadataService
import com.kostas.kafkaStreams.metadata.RemoteAddress
import com.kostas.kafkaStreams.model.ListingSummary
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.kafka.config.StreamsBuilderFactoryBean

internal class PropertyRepositoryTest_ByType {
    private val streamsBuilderFactoryBean = mockk<StreamsBuilderFactoryBean>()
    private val kafkaStreams = mockk<KafkaStreams>()
    private val store = mockk<ReadOnlyKeyValueStore<String, Set<ListingSummary>?>>()
    private val metadataService = mockk<MetadataService>()

    private val listing = ListingSummary("SW10", 51.48655, -0.189434, "flat", 2, 1, false, 600000)

    private val localhost = HostStoreInfo("host", 8080, true)
    private val remotehost = HostStoreInfo("host", 8081, false)
    private val localResponse: Either<Nothing, List<ListingSummary>> = Either.right(listOf(listing))
    private val remoteResponse: Either<RemoteAddress, Nothing> = Either.left(RemoteAddress("host", 8081))

    private val underTest = PropertyRepository(streamsBuilderFactoryBean, metadataService)

    @BeforeEach
    fun setup() {
        every { streamsBuilderFactoryBean.kafkaStreams } returns kafkaStreams
        every { kafkaStreams.store(any<StoreQueryParameters<*>>()) } returns store
    }

    @Nested
    inner class `Given a property type` {

        @Test
        fun `should return a list of listing summaries from local store`() {
            every { store.get(eq("flat")) } returns setOf(listing)
            every { metadataService.streamsMetadataForStoreAndKey(any(), eq("flat"), any()) } returns localhost

            val actual = underTest.getListingByType("flat")

            assertThat(actual).isEqualTo(localResponse)

        }

        @Test
        fun `should return an empty list when key not found`() {
            every { store.get(eq("wrongType")) } returns emptySet()
            every { metadataService.streamsMetadataForStoreAndKey(any(), "wrongType", any()) } returns localhost

            val actual = underTest.getListingByType("wrongType")

            assertThat(actual).isEqualTo(Either.right(emptyList<ListingSummary>()))
        }

        @Test
        fun `should return a list of summaries from remote host store`() {
            every { metadataService.streamsMetadataForStoreAndKey(any(), eq("flat"), any()) } returns remotehost

            val actual = underTest.getListingByType("flat")

            assertThat(actual).isEqualTo(remoteResponse)
        }
    }
}
