import * as cdk from "@aws-cdk/core"
import { VPCStack } from "./vpc-stack";

export interface KafkaStackProps extends cdk.StackProps {
    readonly vpcStack: VPCStack;
}

export interface ApplicationStackProps extends cdk.StackProps {
    readonly vpcStack: VPCStack;
}
