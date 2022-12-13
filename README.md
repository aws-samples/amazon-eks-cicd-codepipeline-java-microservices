# Java SpringBoot based microservices with deployment on Amazon EKS with AWS CodePipeline

## Objective

Objective of this project is to demonstrate Containerized and Kubernetized microservices deployment on Amazon EKS through CI/CD Pipeline using AWS DevOps Tools.

The project demonstrates some of the Kubernetes related nuances such as externally exposing a microservices over the internet and at the same time only exposing a microservice internally to other microservices.

For authenticating the clients for microservice-based API invocation, the project makes use of Amazon Cognito User Pool.

To demonstrate Agile development and deployment, project uses of AWS DevOps Tools based deployment pipeline using AWS CodePipeline, AWS CodeBuild. The CI/CD build pipeline can make use of GitHub or AWS CodeCommit or other Soruce Code control systems.

## Project Description
### Code structure
This is a multi-module maven based Spring-Boot project with two microservices. Both microservices are packaged as separate modules and get built as Docker image as part of the AWS CodeBuild build stage execution.

### Springboot Microservices
Microservices are implemented as Springboot project along with Spring Security. 

"Product" microservice is external facing which calls the other internal microservice "Review". 

Both microservices are authenticated with Amazon Cognito User pool based authentication using OIDC - OAuth2 (JWT) mechanism.

### Kubernetes deployment
The project also consists of Kubernetes deployment artifact that defines how the Kubernetes deployment

Product microservice is internet facing and is exposed using Ingress controller. Product microservice invokes Review microservice which is exposed only within Kubernetes cluster using ClusterIP.

## Set-up steps

### EKS Cluster
Create Amazon EKS Cluster with 3 Compute nodes with instance type t3.small or larger

### Amazon ECR
Create private Amazon ECR Repositories for Docker images for two microservices
- Product
- Review

### Amazon Cognito
- Create Amazon Cognito User Pool with at-least one validated user.
- Make sure you update the application.yml (<project_module>/src/main/resources) files under both Java project modules (Product and Review) with the Cognito User Pool Id that you created.
    - You can get the User Pool Id from Amazon Cognito Console

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.{AWS-Region}.amazonaws.com/{Cognito-user-pool-Id}
```

### AWS CodeBuild
Create AWS CodeBuild project named as - "aws-samples-k8s-microservices"
- In the Environment section select following settings
    - Managed image
    - Operating System - Ubuntu
    - Runtime - Standard
    - Image - Select latest image available e.g. aws/codebuild/standard:6.0
    - Image version - Select - Always use the latest image for this runtme version
    - Tick the "Privileged" checkbox. This flag is needed to build the Docker image inside the CodeBuild stage.
- Create following environment variables as part of configuration
    1. ECR_PRODUCT_REPOSITORY_URI - ECR Repository for Product Container Image
    2. ECR_REVIEW_REPOSITORY_URI - ECR Repository for Review Container Image
    3. EKS_CLUSTER_NAME - Name of the EKS Cluster
    4. AWS_REGION - AWS Region e.g. ap-south-1
    5. ECR_REGISTRY - Elastic Container Registry URL for your Private Repository e.g. <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com

#### IAM Role for AWS CodeBuild
By default AWS CodeBuild will create a Service Role - "codebuild-aws-samples-k8s-microservices-service-role" if AWS CodeBuild Project name is "aws-samples-k8s-microservices".

For this Role, add following permission policy through AWS command line or AWS IAM Console.
Below permissions are needed for AWS CodeBuild to work with Amazon ECR Service and Amazon EKS cluster

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
              "eks:DescribeNodegroup",
              "eks:DescribeUpdate",
              "eks:DescribeCluster",
              "ecr:CompleteLayerUpload",
              "ecr:GetAuthorizationToken",
              "ecr:UploadLayerPart",
              "ecr:InitiateLayerUpload",
              "ecr:BatchCheckLayerAvailability",
              "ecr:PutImage"
            ],
            "Resource": "*"
        }
    ]
}
```       

### Updating Kubernetes `aws-auth` ConfigMap
1. Edit the aws-auth ConfigMap of your cluster.
```shell
$ kubectl -n kube-system edit configmap/aws-auth
```  
2. Add the AWS CodeBuild Project specific execution service role as shown below,
```yaml
apiVersion: v1
data:
  mapRoles: |
    - groups:
      - system:masters
      rolearn: arn:aws:iam::{AWS_Account_ID}:role/codebuild-aws-samples-k8s-microservices-service-role
      username: codebuild-aws-samples-k8s-microservices-service-role
```

### AWS CodePipeline
- Create AWS CodePipeline Project with two stages
    - Configrue Source stage with Source Provider as this Github project forked in your own account
    - Configure Build stage with the CodeBuild project "aws-samples-k8s-microservices" created above
- AWS CodePipeline will get triggered on your Github commit.

## Troubleshooting AWS CodePipeline and AWS CodeBuild Errors
Q1. How do I resolve "error: You must be logged in to the server (Unauthorized)" errors when connecting to an Amazon EKS cluster from CodeBuild?
- https://aws.amazon.com/premiumsupport/knowledge-center/codebuild-eks-unauthorized-errors/

## How to invoke the Product microservice API?
### Using Postman
TO BE ADDED

## Next Steps
    - Connect to RDS with Security Groups mechanism
    - Use of AWS X-Ray
    - USe of Istio/App Mesh

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

