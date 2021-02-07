import * as cdk from "@aws-cdk/core"
import * as msk from "@aws-cdk/aws-msk"
import * as ec2 from "@aws-cdk/aws-ec2"
import * as iam from "@aws-cdk/aws-iam"
import * as custom from "@aws-cdk/custom-resources"
import { KafkaStackProps } from "./types";

const cmd = ec2.InitCommand.shellCommand;

export class KafkaStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: KafkaStackProps) {
        super(scope, id, props);

        const vpc = props.vpcStack.vpc;

        const kafkaSecurityGroup = new ec2.SecurityGroup(
            this,
            "KMKafkaSecurityGroup",
            {
                vpc,
                securityGroupName: "km-kafka-sg",
                description: "Kafka security group",
                allowAllOutbound: true,
            }
        );

        const schemaRegistrySecurityGroup = new ec2.SecurityGroup(
            this,
            "KMSchemaRegistrySecurityGroup",
            {
                vpc,
                securityGroupName: "km-schema-registry-sg",
                description: "Schema Registry security group",
                allowAllOutbound: true,
            }
        );

        // kafka ingress
        kafkaSecurityGroup.addIngressRule(
            ec2.Peer.anyIpv4(),
            ec2.Port.allTraffic(),
            "within kafka"
        );

        kafkaSecurityGroup.addIngressRule(
            schemaRegistrySecurityGroup,
            ec2.Port.allTraffic(),
            "allow traffic from schema registry"
        );

        schemaRegistrySecurityGroup.addIngressRule(
            kafkaSecurityGroup,
            ec2.Port.allTraffic(),
            "allow traffic from kafka"
        );

        const mskCluster = new msk.CfnCluster(this, "KMKafkaCluster", {
            brokerNodeGroupInfo: {
                clientSubnets: props.vpcStack.privateSubnetIds,
                instanceType: "kafka.t3.small",
                securityGroups: [kafkaSecurityGroup.securityGroupId],
                storageInfo: {
                    ebsStorageInfo: {
                        volumeSize: 1,
                    },
                },
            },
            clusterName: "KMKafkaCluster",
            kafkaVersion: "2.7.0",
            numberOfBrokerNodes: 2,
            enhancedMonitoring: "DEFAULT",
            encryptionInfo: {
                encryptionInTransit: {
                    inCluster: false,
                    clientBroker: "PLAINTEXT",
                },
            },
        });

        // Ubuntu 20
        const ubuntu = ec2.MachineImage.genericLinux({
            "eu-west-1": "ami-0aef57767f5404a3c",
        });

        const role = new iam.Role(this, "KMSchemaRegistryRole", {
            assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
        });
        role.addManagedPolicy(
            iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")
        );
        role.addManagedPolicy(
            iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
        );

        const kafkaPolicy = new iam.PolicyStatement();
        kafkaPolicy.addActions(
            ...[
                "kafka:ListClusters",
                "kafka:GetBootstrapBrokers",
                "kafka:DescribeCluster",
            ]
        );
        kafkaPolicy.addResources("*");
        role.addToPolicy(kafkaPolicy);

        const schemaRegistryUserData = ec2.UserData.forLinux();
        schemaRegistryUserData.addCommands(
            "sudo apt-get update",
            "sudo apt-get install awscli -y",
            "sudo apt-get install openjdk-13-jre-headless -y",
            "sudo apt-get install jq -y",
            "wget http://packages.confluent.io/archive/6.0/confluent-community-6.0.0.tar.gz",
            "tar -xvf confluent-community-6.0.0.tar.gz",
            "aws s3 cp s3://km-kafka-app-config-code-bucket/setup-schema-registry.sh setup-schema-registry.sh",
            "sudo chmod +x setup-schema-registry.sh",
            "sudo ./setup-schema-registry.sh"
        );

        const schemaRegistry = new ec2.Instance(this, "KMSchemaRegistry", {
            vpc,
            instanceType: ec2.InstanceType.of(
                ec2.InstanceClass.T2,
                ec2.InstanceSize.NANO
            ),
            machineImage: ubuntu,
            securityGroup: schemaRegistrySecurityGroup,
            role,
            vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE },
            userData: schemaRegistryUserData,
        });
        // Do not start the schema registry until Kafka is ready
        schemaRegistry.node.addDependency(mskCluster);

        const getClusterArn = new custom.AwsCustomResource(this, "GetClusterArn", {
            onUpdate: {
                service: "Kafka",
                action: "listClusters",
                parameters: {
                    ClusterNameFilter: "KMKafkaCluster",
                },
                physicalResourceId: custom.PhysicalResourceId.of(Date.now().toString()),
            },
            policy: custom.AwsCustomResourcePolicy.fromSdkCalls({
                resources: custom.AwsCustomResourcePolicy.ANY_RESOURCE,
            }),
        });

        const getBootstrapBrokers = new custom.AwsCustomResource(
            this,
            "KGetBootstrapBrokers",
            {
                onUpdate: {
                    service: "Kafka",
                    action: "getBootstrapBrokers",
                    parameters: {
                        ClusterArn: `${getClusterArn.getResponseField(
                            "ClusterInfoList.0.ClusterArn"
                        )}`,
                    },
                    physicalResourceId: custom.PhysicalResourceId.of(
                        Date.now().toString()
                    ),
                },
                policy: custom.AwsCustomResourcePolicy.fromSdkCalls({
                    resources: custom.AwsCustomResourcePolicy.ANY_RESOURCE,
                }),
            }
        );

        getBootstrapBrokers.node.addDependency(mskCluster)

        const bootstrapBrokers = getBootstrapBrokers.getResponseField(
            "BootstrapBrokerString"
        );
        new cdk.CfnOutput(this, "KMBootstrapBrokers", {
            value: bootstrapBrokers,
            exportName: "BootstrapBrokers",
        });

        new cdk.CfnOutput(this, "KMKafkaSecurityGroupId", {
            value: kafkaSecurityGroup.securityGroupId,
            exportName: "KafkaSecurityGroupId",
        });

        new cdk.CfnOutput(this, "KMSchemaRegistrySecurityGroupId", {
            value: schemaRegistrySecurityGroup.securityGroupId,
            exportName: "SchemaRegistrySecurityGroupId",
        });
    }
}
