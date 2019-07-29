FROM openjdk:8
COPY target/agent.zip /opt/testgrid/agent.zip
RUN apt-get update
RUN apt-get install nano
WORKDIR /opt/testgrid
RUN unzip agent.zip
WORKDIR /opt/testgrid/agent
ENTRYPOINT chmod +x kubernetes_startup.sh
ENTRYPOINT chmod +x testgrid-agent
CMD ["/bin/bash","-c","./testgrid-agent restart"]
