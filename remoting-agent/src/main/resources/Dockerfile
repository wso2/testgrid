FROM openjdk:8
COPY target/agent.zip /opt/testgrid/agent.zip
WORKDIR /opt/testgrid
RUN unzip agent.zip
WORKDIR /opt/testgrid/agent
RUN chmod +x kubernetes_startup.sh
RUN chmod +x testgrid-agent
RUN chmod +x logarchiver.sh
