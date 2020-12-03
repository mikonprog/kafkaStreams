package com.kostas.kafkaStreams

import arrow.core.Either
import com.kostas.kafkaStreams.metadata.RemoteAddress
import com.kostas.kafkaStreams.model.ListingSummary
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ListingServiceTest {
    private val propertyRepository = mockk<PropertyRepository>()

    private val listing = ListingSummary("SW10", 51.48655, -0.189434, "flat", 2, 1, false, 600000)

    private val eitherResponseOrRemote: Either<RemoteAddress, List<ListingSummary>> = Either.right(listOf(listing))

    private val underTest = ListingService(propertyRepository)

    @Nested
    inner class GetListingsByPostcode {

        @Nested
        inner class `Given a postcode` {

            @Test
            fun `It should return a list of properties that exist on that postcode`() {
                every { propertyRepository.getListingByPostcode(any()) } returns eitherResponseOrRemote

                val response = underTest.getListingsByPostcode("SW10")
                Assertions.assertThat(response).isEqualTo(eitherResponseOrRemote)
            }
        }
    }

    @Nested
    inner class GetListingsByType {

        @Nested
        inner class `Given a property type` {

            @Test
            fun `It should return a list of properties of the given property type`() {
                every { propertyRepository.getListingByType(any()) } returns eitherResponseOrRemote

                val response = underTest.getListingsByType("flat")
                Assertions.assertThat(response).isEqualTo(eitherResponseOrRemote)
            }
        }
    }
}
