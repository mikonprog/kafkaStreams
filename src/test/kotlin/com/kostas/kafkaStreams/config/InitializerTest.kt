package com.kostas.kafkaStreams.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kostas.kafkaStreams.ListingProducer
import com.kostas.kafkaStreams.util.ResourceFileReader
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

internal class InitializerTest {

    private val contextRunner: ApplicationContextRunner = ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java)
            .withBean(ResourceFileReader::class.java)
            .withUserConfiguration(Initializer::class.java)

    @Test
    fun cacheEnabled() {
        contextRunner
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(Initializer::class.java)
            }
    }

    class TestConfig {
        @Bean
        fun listingProducer(): ListingProducer? {
            return Mockito.mock(ListingProducer::class.java)
        }

        @Bean
        fun objectMapper(): ObjectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    }
}
