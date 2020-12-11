# Build project source into WAR
FROM maven:3.6-jdk-11-slim AS build
WORKDIR /home/app/src
# Copy project source to docker container
COPY . .
# Build WAR
RUN mvn clean install

# Use tomcat base image
FROM tomcat:9-jre11

# Configure resource in tomcat to communicate with postgres
COPY ./build-web/context.xml /usr/local/tomcat/conf/context.xml
# Copy postgres driver to tomcat
COPY ./build-web/postgresql-9.4.1208.jar /usr/local/tomcat/lib/

# Creates user with access to manager webapp
COPY ./build-web/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
# Disables network filtering to allow access to manager webapp
COPY ./build-web/manager-context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml

# Remove ROOT folder and set DataSpread as ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT
# Copies built WAR from previous container
COPY --from=build /home/app/src/webapp/target/DataSpread.war /usr/local/tomcat/webapps/ROOT.war
# Uncomment line below to use working DataSpread WAR
# COPY ./build-web/DataSpread.war /usr/local/tomcat/webapps/ROOT.war

# Start up tomcat server
CMD ["catalina.sh", "run"]