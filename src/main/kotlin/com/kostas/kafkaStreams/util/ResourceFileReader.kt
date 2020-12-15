package com.kostas.kafkaStreams.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class ResourceFileReader(val objectMapper: ObjectMapper) {
    final inline fun <reified T> read(fileName: String) =
            objectMapper.readValue<T>(ClassPathResource(fileName).inputStream.readAllBytes())
}
