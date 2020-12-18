import * as cdk from "@aws-cdk/core";
import * as msk from "@aws-cdk/aws-msk";
import * as ec2 from "@aws-cdk/aws-ec2";
import * as iam from "@aws-cdk/aws-iam";
import * as s3 from "@aws-cdk/aws-s3";
import * as s3deploy from "@aws-cdk/aws-s3-deployment";

import { ApplicationStackProps } from "./types";

export class ApplicationStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: ApplicationStackProps) {
        super(scope, id, props);
        const vpc = props.vpcStack.vpc;
        const ksgid = cdk.Fn.importValue("KafkaSecurityGroupId");
        const srgid = cdk.Fn.importValue("SchemaRegistrySecurityGroupId");
        const kafkaSecurityGroup = ec2.SecurityGroup.fromSecurityGroupId(
            this,
            "KafkaSecurityGroup",
            ksgid,
            {
                mutable: true,
            }
        );
        const schemaRegistrySecurityGroup = ec2.SecurityGroup.fromSecurityGroupId(
            this,
            "SchemaRegistrySecurityGroup",
            srgid,
            {
                mutable: true,
            }
        );

        const kafkaStreamsSecurityGroup = new ec2.SecurityGroup(
            this,
            "KafkaStreamsSecurityGroup",
            {
                vpc,
                securityGroupName: "kafka-streams-sg",
                description: "KafkaStreams security group",
                allowAllOutbound: true,
            }
        );

        kafkaSecurityGroup.addIngressRule(
            kafkaStreamsSecurityGroup,
            ec2.Port.allTraffic(),
            "allow traffic from kafka clients"
        );

        kafkaStreamsSecurityGroup.addIngressRule(
            kafkaSecurityGroup,
            ec2.Port.allTraffic(),
            "allow traffic from kafka"
        );

        schemaRegistrySecurityGroup.addIngressRule(
            kafkaStreamsSecurityGroup,
            ec2.Port.allTraffic(),
            "allow all traffic from app"
        );

        kafkaStreamsSecurityGroup.addIngressRule(
            schemaRegistrySecurityGroup,
            ec2.Port.allTraffic(),
            "allow traffic from schema registry"
        );

        // Ubuntu 20
        const ubuntu = ec2.MachineImage.genericLinux({
            "eu-west-1": "ami-0aef57767f5404a3c",
        });

        const role = new iam.Role(this, "KafkaStreamsAppServiceRole", {
            assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com"),
        });
        role.addManagedPolicy(
            iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")
        );
        role.addManagedPolicy(
            iam.ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
        );

        const kafkaStreamsPolicies = new iam.PolicyStatement();
        kafkaStreamsPolicies.addActions(
            ...[
                "kafka:ListClusters",
                "kafka:GetBootstrapBrokers",
                "kafka:DescribeCluster",
                "ec2:DescribeInstances",
            ]
        );
        kafkaStreamsPolicies.addResources("*");
        role.addToPolicy(kafkaStreamsPolicies);

        const kafkaStreamsUserData = ec2.UserData.forLinux();
        kafkaStreamsUserData.addCommands(
            "sudo apt-get update",
            "sudo apt-get install awscli -y",
            "sudo apt-get install openjdk-13-jre-headless -y",
            "sudo apt-get install jq -y",
            "cd /home/ubuntu",
            "wget https://apache.mirrors.nublue.co.uk/kafka/2.6.0/kafka_2.13-2.6.0.tgz",
            "tar -xvf kafka_2.13-2.6.0.tgz",
            "aws s3 cp s3://km-kafka-app-config-code-bucket/create-topics.sh create-topics.sh",
            "sudo chmod +x create-topics.sh",
            "sudo ./create-topics.sh",
            "aws s3 cp s3://km-kafka-app-config-code-bucket/app.jar app.jar",
            "aws s3 cp s3://km-kafka-app-config-code-bucket/setup-kafka-streams-app.sh setup-kafka-streams-app.sh",
            "sudo chmod +x setup-kafka-streams-app.sh",
            "sudo ./setup-kafka-streams-app.sh"
        );

        const kafkaStreams = new ec2.Instance(this, "KMKafkaStreams", {
            vpc,
            instanceType: ec2.InstanceType.of(
                ec2.InstanceClass.T2,
                ec2.InstanceSize.MICRO
            ),
            machineImage: ubuntu,
            securityGroup: kafkaStreamsSecurityGroup,
            role,
            vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
            userData: kafkaStreamsUserData
        });
    }
}
