package com.kostas.kafkaStreams

import org.springframework.stereotype.Service

@Service
class ListingService(
    private val propertyRepository: PropertyRepository
) {
    fun getListingsByPostcode(postcode: String) = propertyRepository.getListingByPostcode(postcode)

    fun getListingsByType(propertyType: String) = propertyRepository.getListingByType(propertyType)

}
