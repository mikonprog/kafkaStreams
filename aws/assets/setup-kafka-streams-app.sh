#!/bin/bash
CLUSTER_ARN=$(aws kafka list-clusters --region eu-west-1 | jq ".ClusterInfoList[0] .ClusterArn" -r)

BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers --cluster-arn $CLUSTER_ARN --region eu-west-1 | jq ".BootstrapBrokerString" -r | awk -F ',' '{print $1","$2}')
export KAFKA_BOOTSTRAP_SERVERS=$BOOTSTRAP_SERVERS

SR_IP_ADDRESS=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=KafkaStack/SchemaRegistry" --region eu-west-1 | jq ".Reservations[0] .Instances[0] .PrivateIpAddress" -r)
export SCHEMA_REGISTRY_URL="http://$SR_IP_ADDRESS:8081"

cat > application.yaml << EOL
spring:
  application:
    name: "kafka-streams-app"
  main:
    banner-mode: "off"
    allow-bean-definition-overriding: true
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: "all"
    properties:
      key.subject.name.strategy: io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
      value.subject.name.strategy: io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
      schema.registry.url: ${SCHEMA_REGISTRY_URL}
    streams:
      application-id: "pdp-kostas-kafka-streams"
      replication-factor: 1
      state-dir: "/tmp/kafka-streams/pdp-kafkaStreams-8080"
      properties:
        "default.key.serde": org.apache.kafka.common.serialization.Serdes$StringSerde
        "default.value.serde": org.apache.kafka.common.serialization.Serdes$StringSerde
        "num.stream.threads": 1
        "num.standby.replicas": 0
        "commit.interval.ms": 100
        "application.server": "http://localhost:8080"
        "default.deserialization.exception.handler": org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
        "topology.optimization": "all"
        "compression.type": "lz4"
        "max.request.size": "2196608"

server:
  port: 8080

kafka:
  topics:
    listing-events: "com.kostas.pdp.listings.events.avro"
    price-events: "com.kostas.pdp.prices.events.avro"

EOL

cat > /etc/systemd/system/cms-service.service << EOL
[Unit]
Description=Manage Java service

[Service]
WorkingDirectory=/
ExecStart=/usr/bin/java -jar app.jar
User=ubuntu
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOL

sudo systemctl enable kafka-streams-app
sudo systemctl start kafka-streams-app.service