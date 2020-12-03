package com.kostas.kafkaStreams.rest

import arrow.core.getOrHandle
import com.kostas.kafkaStreams.ListingService
import com.kostas.kafkaStreams.metadata.ApiClientFactory
import com.kostas.kafkaStreams.model.ListingSummary
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.QueryParam

@RestController
@RequestMapping("/listings")
class Controller(
    private val listingService: ListingService,
    private val apiClient: ApiClientFactory
): ListingApi {

    @GetMapping("/postcode/{postcode}")
    override fun getListingsByPostcode(@PathVariable("postcode") postcode: String): List<ListingSummary> =
            listingService.getListingsByPostcode(postcode)
                .getOrHandle { apiClient.forInstance(it).getByPostcode(postcode) }

    @GetMapping("/property_type/{property_type}")
    override fun getListingsByType(@PathVariable("property_type") propertyType: String): List<ListingSummary> =
            listingService.getListingsByType(propertyType)
                .getOrHandle { apiClient.forInstance(it).getByType(propertyType) }

}
