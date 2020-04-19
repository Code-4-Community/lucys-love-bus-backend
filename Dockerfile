# Base the container image off of JDK 8
FROM openjdk:8

# Set the working dir env var
ENV WORK_DIR /lucys-love-bus-backend

# Expose the container ports to the host
EXPOSE 8081

# Set default working directory
WORKDIR ${WORK_DIR}

# Run necessary tasks
RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install maven -y

# Add some files
ADD api ${WORK_DIR}/api
ADD common ${WORK_DIR}/common
ADD persist ${WORK_DIR}/persist
ADD service ${WORK_DIR}/service
ADD pom.xml ${WORK_DIR}/pom.xml
ADD scripts ${WORK_DIR}

RUN rm -rf ${WORK_DIR}/service/test
RUN rm -rf ${WORK_DIR}/service/src/test
RUN rm -rf ${WORK_DIR}/common/src/test

# Set a default command to execute
CMD /lucys-love-bus-backend/deploy.sh
