import "source-map-support/register"
import * as cdk from "@aws-cdk/core"
import { VPCStack } from "../lib/vpc-stack";
import { S3Stack } from "../lib/s3-stack";
import { ApplicationStack } from "../lib/application-stack";
import { KafkaStack } from "../lib/kafka-stack";

const app = new cdk.App();

const envProps: cdk.StackProps = {
    env: {
        region: 'eu-west-1',
        account: '603508486078'
    }
};

const s3Stack = new S3Stack(app, 'S3Stack', { ...envProps });

const vpcStack = new VPCStack(app, 'VPCStack', { ...envProps });

const kafkaStack = new KafkaStack(app, 'KafkaStack', {
    ...envProps,
    vpcStack
});

const applicationStack = new ApplicationStack(app, 'ApplicationStack', {
    ...envProps,
    vpcStack
});
applicationStack.addDependency(kafkaStack);
