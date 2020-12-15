package com.kostas.kafkaStreams.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ListingSummary(
    val postcode: String,
    val latitude: Double,
    val longitude: Double,
    val property_type: String,
    val num_bedrooms: Int,
    val num_bathrooms: Int,
    val new_home : Boolean?,
    val price: Long
)
