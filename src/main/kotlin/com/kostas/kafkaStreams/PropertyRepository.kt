package com.kostas.kafkaStreams

import arrow.core.Either
import com.kostas.kafkaStreams.config.StateStoreNames
import com.kostas.kafkaStreams.metadata.MetadataService
import com.kostas.kafkaStreams.metadata.RemoteAddress
import com.kostas.kafkaStreams.model.ListingSummary
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.StoreQueryParameters.fromNameAndType
import org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Repository

@Repository
class PropertyRepository(
        private val streamsBuilder: StreamsBuilderFactoryBean,
        private val metadataService: MetadataService,
) {
    private val keySerializer = Serdes.String().serializer()
    private val byPostcode = ClusteredStateStore<String, Set<ListingSummary>?>(StateStoreNames.listingByPostcode, keySerializer)
    private val byType = ClusteredStateStore<String, Set<ListingSummary>?>(StateStoreNames.listingByType, keySerializer)

    fun getListingByPostcode(postcode: String): Either<RemoteAddress, List<ListingSummary>> =
            byPostcode.get(postcode).map { it?.toList() ?: emptyList() }

    fun getListingByType(propertyType: String): Either<RemoteAddress, List<ListingSummary>> =
            byType.get(propertyType).map { it?.toList() ?: emptyList() }

    private inner class ClusteredStateStore<K, V>(private val name: String, private val keySerializer: Serializer<K>) {
        val store: ReadOnlyKeyValueStore<K, V> by lazy {
            streamsBuilder.kafkaStreams.store(fromNameAndType(name, keyValueStore<K, V>()).enableStaleStores())
        }

        fun get(key: K): Either<RemoteAddress, V> {
            val (host, port, isLocal) = metadataService.streamsMetadataForStoreAndKey(name, key, keySerializer)
            return if (isLocal) {
                Either.Right(store.get(key))
            } else {
                Either.Left(RemoteAddress(host, port))
            }
        }
    }
}
