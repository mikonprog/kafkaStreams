package com.kostas.kafkaStreams.metadata

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ApiClientFactory(
    private val restTemplate: RestTemplate
) {
    fun forInstance(address: RemoteAddress) = ApiClient(restTemplate, address.host, address.port)

}
