#!/bin/bash
CLUSTER_ARN=$(aws kafka list-clusters --region eu-west-1 | jq ".ClusterInfoList[0] .ClusterArn" -r)

BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers --cluster-arn $CLUSTER_ARN --region eu-west-1 | jq ".BootstrapBrokerString" -r | awk -F ',' '{print "PLAINTEXT://"$1",""PLAINTEXT://"$2}')

rm /confluent-6.0.0/etc/schema-registry/schema-registry.properties

cat > /confluent-6.0.0/etc/schema-registry/schema-registry.properties << EOL
listeners=http://0.0.0.0:8081
kafkastore.bootstrap.servers=${BOOTSTRAP_SERVERS}
kafkastore.topic=_schemas
debug=false
EOL

cat > /etc/systemd/system/confluent-schema-registry.service << EOL
[Unit]
Description=RESTful schema registry for Apache Kafka
Documentation=http://docs.confluent.io/
After=network.target confluent-kafka.target

[Service]
Type=simple
User=ubuntu
Environment="LOG_DIR=/var/log/confluent/schema-registry"
ExecStart=/confluent-6.0.0/bin/schema-registry-start /confluent-6.0.0/etc/schema-registry/schema-registry.properties
TimeoutStopSec=180
Restart=no

[Install]
WantedBy=multi-user.target
EOL

sudo systemctl enable confluent-schema-registry
sudo systemctl start confluent-schema-registry.service