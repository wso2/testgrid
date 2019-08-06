FROM openjdk:8
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
RUN apt-get install zip
COPY target/agent.zip /opt/testgrid/agent.zip
WORKDIR /opt/testgrid
RUN unzip agent.zip
WORKDIR /opt/testgrid/agent
RUN chmod +x kubernetes_startup.sh
RUN chmod +x testgrid-agent
RUN chmod +x logarchiver.sh
