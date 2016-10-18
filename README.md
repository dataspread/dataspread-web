## Setup Instructions:

### Required Software

* Tomcat 8.5.4
* Java JDK 8
* PostgreSQL 9.5
* IntelliJ IDEA (Recommended)

### Procedure

1. Download DBSpread-Web from git.

2. Create a user in Postgres with your username and password. Also create a database named "ibd".

3. Update web.xml in Tomcat by adding following:

	```
	<listener>
	    <listener-class>org.zkoss.zss.model.impl.DBHandler</listener-class>
	</listener>
	```

4. Update context.xml in Tomcat by adding following:
	```
	<Resource name="jdbc/ibd" auth="Container"
	          type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
	          url="jdbc:postgresql://127.0.0.1:5432/ibd"
	          username="<your_username>" password="<your_password>"
                  maxTotal="20" maxIdle="10" maxWaitMillis="-1" defaultAutoCommit="false" accessToUnderlyingConnectionAllowed="true"/>
	```

	Replace `<your_username>` and `<your_password>` with your user name and password created in Postgres.

5. Copy postgresql-9.4.1208 from project local_lib to Tomcat lib.

6. Configure the Application server to Tomcat.

7. Now you are ready to run the program!