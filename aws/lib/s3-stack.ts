import * as cdk from "@aws-cdk/core";
import * as s3 from "@aws-cdk/aws-s3";
import * as s3deploy from "@aws-cdk/aws-s3-deployment";

export class S3Stack extends cdk.Stack {
    public readonly kmS3Bucket: s3.Bucket;

    constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.kmS3Bucket = new s3.Bucket(this, "KafkaBucket", {
            bucketName: "km-kafka-app-config-code-bucket",
            blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
        });

        const deployment = new s3deploy.BucketDeployment(this, "Deployment", {
            sources: [s3deploy.Source.asset("../aws/assets")],
            destinationBucket: this.kmS3Bucket,
        });
    }
}
