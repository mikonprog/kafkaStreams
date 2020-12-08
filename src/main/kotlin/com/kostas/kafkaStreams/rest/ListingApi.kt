package com.kostas.kafkaStreams.rest

import com.kostas.kafkaStreams.model.ListingSummary

interface ListingApi {
    fun getListings(postcode: String?, propertyType: String?): List<ListingSummary>

}
