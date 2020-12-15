package com.kostas.kafkaStreams.rest

import arrow.core.Either
import com.kostas.kafkaStreams.ListingService
import com.kostas.kafkaStreams.metadata.ApiClient
import com.kostas.kafkaStreams.metadata.ApiClientFactory
import com.kostas.kafkaStreams.metadata.RemoteAddress
import com.kostas.kafkaStreams.model.ListingSummary
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ControllerTest {
    private val listingService = mockk<ListingService>()
    private val apiClientFactory = mockk<ApiClientFactory>()
    private val apiClient = mockk<ApiClient>()

    private val listing = ListingSummary("SW10", 51.48655, -0.189434, "flat", 2, 1, false, 600000)

    private val remote = Either.left(RemoteAddress("host", 8280))
    private val localNode = Either.right(listOf(listing))

    private val underTest = Controller(listingService, apiClientFactory)

    @BeforeEach
    fun setup() {
        every { apiClientFactory.forInstance(any()) } returns apiClient
    }

    @Nested
    inner class getListingsByPostcode {

        @Test
        fun `should return local node`() {
            every { listingService.getListingsByPostcode("SW10") } returns localNode

            val response = underTest.getListings("SW10", null)
            assertThat(response).isEqualTo(listOf(listing))
        }

        @Test
        fun `should delegate to client for remote node`() {
            every { listingService.getListingsByPostcode("SW10") } returns remote
            every { apiClient.getByPostcode("SW10") } returns listOf(listing)

            val response = underTest.getListings("SW10", null)
            assertThat(response).isEqualTo(listOf(listing))
        }
    }

    @Nested
    inner class getListingsByType {

        @Test
        fun `should return local node`() {
            every { listingService.getListingsByType("flat") } returns localNode

            val response = underTest.getListings(null, "flat")
            assertThat(response).isEqualTo(listOf(listing))
        }

        @Test
        fun `should delegate to client for remote node`() {
            every { listingService.getListingsByType("flat") } returns remote
            every { apiClient.getByType("flat") } returns listOf(listing)

            val response = underTest.getListings(null, "flat")
            assertThat(response).isEqualTo(listOf(listing))
        }
    }
}
