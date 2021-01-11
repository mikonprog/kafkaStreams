import "source-map-support/register"
import * as cdk from "@aws-cdk/core"
import { VPCStack } from "../lib/vpc-stack";
import { S3Stack } from "../lib/s3-stack";
import { ApplicationStack } from "../lib/application-stack";
import { KafkaStack } from "../lib/kafka-stack";

const app = new cdk.App();

const envProps: cdk.StackProps = {
    env: {
        region: //e.g.,'eu-west-1',
        account: //your aws account
    }
};

const s3Stack = new S3Stack(app, 'KMS3Stack', { ...envProps });

const vpcStack = new VPCStack(app, 'KMVPCStack', { ...envProps });

const kafkaStack = new KafkaStack(app, 'KMKafkaStack', {
    ...envProps,
    vpcStack
});

const applicationStack = new ApplicationStack(app, 'KMKafkaApplicationStack', {
    ...envProps,
    vpcStack
});

applicationStack.addDependency(kafkaStack);
