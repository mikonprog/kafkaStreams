import * as cdk from "@aws-cdk/core"
import * as ec2 from "@aws-cdk/aws-ec2"

export class VPCStack extends cdk.Stack {
    public readonly vpc: ec2.Vpc;

    public getVpc() {
        return this.vpc;
    }

    public get privateSubnetIds() {
        return this.vpc.privateSubnets.map((sn) => sn.subnetId);
    }

    constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.vpc = new ec2.Vpc(this, "KMKafkaVPC", {
            cidr: "10.0.0.0/16",
            natGateways: 1,
            maxAzs: 2,
            subnetConfiguration: [
                {
                    cidrMask: 24,
                    name: "public-subnet",
                    subnetType: ec2.SubnetType.PUBLIC,
                },
                {
                    cidrMask: 24,
                    name: "private-subnet",
                    subnetType: ec2.SubnetType.PRIVATE,
                },
            ],
        });
    }
}
