package com.kostas.kafkaStreams.config

import com.kostas.kafkaStreams.ListingProducer
import com.kostas.kafkaStreams.model.ListingSummary
import com.kostas.kafkaStreams.util.ResourceFileReader
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class Initializer(
    private val listingProducer: ListingProducer,
    private val resources: ResourceFileReader
) {

    @PostConstruct
    fun onStartUp() {
        val listings = resources.read<List<ListingSummary>>("listings.json")
        listings.map { listing -> listingProducer.publishListing(listing) }
    }
}
