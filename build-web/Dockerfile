# Use tomcat base image
FROM tomcat:8-jre8 

# Configure resource in tomcat to communicate with postgres
COPY ./context.xml /usr/local/tomcat/conf/context.xml
# Copy postgres driver to tomcat
COPY ./postgresql-9.4.1208.jar /usr/local/tomcat/lib/

# Creates user with access to manager webapp
COPY ./tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
# Disables network filtering to allow access to manager webapp
COPY ./manager-context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml

# Remove ROOT folder and set DataSpread as ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY ./DataSpread.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]