package com.kostas.kafkaStreams.rest

import com.kostas.kafkaStreams.model.ListingSummary

interface ListingApi {
    fun getListingsByPostcode(postcode: String): List<ListingSummary>
    fun getListingsByType(propertyType: String): List<ListingSummary>
}
