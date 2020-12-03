package com.kostas.kafkaStreams.config

import mu.KotlinLogging
import org.apache.kafka.streams.StreamsBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/topology")
class TopologyController(
    private val builder: StreamsBuilder
) {
    @GetMapping(produces = ["text/plain"])
    fun getTopology(): String {
        logger.info { "Topologies: ${builder.build().describe()}" }
        return builder.build().describe().toString()
    }
}
