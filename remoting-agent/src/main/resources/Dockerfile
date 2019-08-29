# This Dockerfile creates the main Image that is present in the Logfile Extractor sidecar in TestGrid K8S Deployments.

# Built on openjdk 9 base image for the running the remote-agent.jar service
FROM openjdk:8

# Install python 3 and pip to install aws cli for s3 management
RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip \
        python3-setuptools \
        groff \
        less \
    && pip3 install --upgrade pip \
    && apt-get clean
RUN pip3 --no-cache-dir install --upgrade awscli

# install zip for file extraction and archiving
RUN apt-get install zip

# Copy the remoting agent to the image
COPY target/agent.zip /opt/testgrid/agent.zip
WORKDIR /opt/testgrid
RUN unzip agent.zip
WORKDIR /opt/testgrid/agent

# grant execution access for main scripts
RUN chmod +x kubernetes_startup.sh
RUN chmod +x testgrid-agent
RUN chmod +x log_archiver.sh
