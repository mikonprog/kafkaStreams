#!/bin/bash
CLUSTER_ARN=$(aws kafka list-clusters --region eu-west-1 | jq ".ClusterInfoList[0] .ClusterArn" -r)

BOOTSTRAP_SERVER=$(aws kafka get-bootstrap-brokers --cluster-arn $CLUSTER_ARN --region eu-west-1 | jq ".BootstrapBrokerString" -r | awk -F ',' '{print $1}')

./kafka_2.13-2.7.0/bin/kafka-topics.sh --create --topic com.kostas.pdp.listings.events.avro --bootstrap-server $BOOTSTRAP_SERVER --partitions 5 --replication-factor 2
./kafka_2.13-2.7.0/bin/kafka-topics.sh --create --topic com.kostas.pdp.prices.events.avro --bootstrap-server $BOOTSTRAP_SERVER --partitions 5 --replication-factor 2
