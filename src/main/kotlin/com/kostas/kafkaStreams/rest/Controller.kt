package com.kostas.kafkaStreams.rest

import arrow.core.getOrHandle
import com.kostas.kafkaStreams.ListingService
import com.kostas.kafkaStreams.metadata.ApiClientFactory
import com.kostas.kafkaStreams.model.ListingSummary
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/listings")
class Controller(
    private val listingService: ListingService,
    private val apiClient: ApiClientFactory
): ListingApi {

    @GetMapping
    override fun getListings(
            @RequestParam(required = false, value = "postcode") postcode: String?,
            @RequestParam(required = false, value = "property_type") propertyType: String?,
    ) : List<ListingSummary> = when {
            !postcode.isNullOrEmpty() -> listingService.getListingsByPostcode(postcode)
                    .getOrHandle { apiClient.forInstance(it).getByPostcode(postcode) }
            !propertyType.isNullOrEmpty() -> listingService.getListingsByType(propertyType)
                    .getOrHandle { apiClient.forInstance(it).getByType(propertyType) }
            else -> emptyList()
    }

}
