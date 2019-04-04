# ------------------------------------------------------------------------
#
# Copyright 2017 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

# This file contains the dockerfile to create the image
FROM adoptopenjdk/openjdk8:jdk8u192-b12
MAINTAINER WSO2 Docker Maintainers "testgrid_team@wso2"
RUN apt-get update && \
    apt-get install -y netcat python3-pip && \
    rm -rf /var/lib/apt/lists/*

# activate user ubuntu
RUN useradd -rm -d /home/ubuntu -s /bin/bash -g root -G sudo -u 1000 ubuntu

# install awscli
RUN pip3 install awscli --upgrade --no-cache-dir

# install mvn,git,ssh
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get install -y git && \
    apt-get install -y ssh

# add github to known hosts
RUN mkdir -p /home/ubuntu/.ssh && \
    ssh-keyscan github.com >> /home/ubuntu/.ssh/known_hosts

USER ubuntu
WORKDIR /home/ubuntu
ENV JAVA_HOME=/opt/java/openjdk

ENTRYPOINT ["/bin/bash"]
