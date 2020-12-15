package com.kostas.kafkaStreams.metadata

data class HostStoreInfo(
    val host: String,
    val port: Int,
    val isThisHost: Boolean
)
