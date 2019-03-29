
How to update run-testplan docker image
===================== 
The image used to create the docker container for run-testplan block is stored as an ECR in TestGrid AWS account. That is being pulled into each jenkins-slave when executing builds. The following steps describe how to modif y the docker image.

1. Install AWS CLI and Docker in your machine.
2. Clone repository to your local machine.
3. Do required modifications in the Dockerfile.
4. Authenticate ECR registry to Docker CLI from the following command.

		$(aws ecr get-login --no-include-email)

5. Go to the Dockerfile directory and build Docker image using following command.

		docker build -t tg-run-testplan .

6. After build completes, tag the build

		docker tag tg-run-testplan:latest <TestGridDockerRegistryURI>/tg-run-testplan:latest
	
	Contact TestGrid team to find <TestGridDockerRegistryURI>

7. Push the image using following command

		docker push <TestGridDockerRegistryURI>/tg-run-testplan:latest

8. The latest image will be automatically picked up by all the slaves who will be spawned after updating the image in ECR.