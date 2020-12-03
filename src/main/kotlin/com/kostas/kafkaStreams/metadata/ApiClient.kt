package com.kostas.kafkaStreams.metadata

import com.kostas.kafkaStreams.model.ListingSummary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class ApiClient(
    private val restTemplate: RestTemplate,
    host: String,
    port: Int
) {
    private val api = "http://$host:$port"

    fun getByPostcode(postcode: String): List<ListingSummary> =
            restTemplate.safelyGET<List<ListingSummary>>("$api/listings?postcode=$postcode")

    fun getByType(propertyType: String): List<ListingSummary> =
            restTemplate.safelyGET<List<ListingSummary>>("$api/listings/property_type/$propertyType")

    private inline fun <reified T> RestTemplate.safelyGET(url: String): T =
        try {
            this.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, object : ParameterizedTypeReference<T>() {}).body!!
        } catch (e: java.lang.Exception) {
            throw Exception("Remote Instance [$api] returned an unexpected status code: ${e.message}")
        }
}
