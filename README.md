# DataSpread: A Spreadsheet-Database Hybrid System

![dataspread-fiverr2-cropped](https://cloud.githubusercontent.com/assets/1056605/21773459/cec3c198-d654-11e6-8d0e-5c7a867ed77b.png)


[DataSpread][dataspread-github] is a _spreadsheet-database hybrid system_, with a spreadsheet frontend, and a database backend. Thus, DataSpread inherits the flexibility and ease-of-use of spreadsheets, as well as the scalability and power of databases. A paper describing DataSpread's architecture, design decisions, and optimization can be found [here][dataspread-site]. DataSpread is a multi-year project, supported by the National Science Foundation via award number 1633755.

### Version
The current version is 0.5.1.

### Features
DataSpread is built using [PostgreSQL][postgressite] and [ZKSpreadsheet][zksite], an open-source web-based spreadsheet tool.

DataSpread's version 0.1 enables users to scale to **billions of cells and return results for common spreadsheet operations within seconds**. It does so via on-demand loading of spreadsheet data.


Like traditional spreadsheet software, DataSpread supports standard spreadsheet book and sheet operations like Load, Rename, Delete, and Import (via XLS and XLSX, and CSV). Any updates to the spreadsheets are automatically saved.

Like traditional spreadsheet software, DataSpread supports the use of 225+ spreadsheet functions, along with formatting and styling operations. It also supports row and column operations like insert, delete, cut, copy, and paste; during insertion and deletion, formulae are updated as is the case in traditional spreadsheet software. 

It supports all these operations while scaling to *arbitrarily large* spreadsheets.

In future releases, DataSpread will support SQL on the spreadsheet frontend, along with other relational algebra-based interactions. It will also support joint formula evaluation and optimization. 

### Key Design Innovations

* DataSpread employs a _flexible hybrid data model_ to represent spreadsheet data within a database. 
* DataSpread uses _positional indexing techniques_ to both locate data by position, and keep it up-to-date as the data is updated. 
* DataSpread also employs a _LRU caching mechanism_ to retrieve and keep in memory data from the database on demand. 
* DataSpread also employs _speculative fetching_ to fetch additional data beyond the user's current spreadsheet window. 



## Setup Instructions:

You can directly use DataSpread via our cloud-hosted [site][siteinfo] (Temporarily offline).

DataSpread can be deployed locally through Docker (recommended) or through Apache Tomcat.

## Docker Method

### Required Software

* [Docker][docker] >= 1.13.0

### Deploying DataSpread locally.

1. Clone the DataSpread repository and go the directory in your terminal. Alternatively, you can download the source as a zip or tar.gz. 

2. Install Docker. [Docker][docker] makes it easy to separate applications from underlying infrastructure so setting up and running applications is quick and easy.

3. Start Docker and start the application. It should be accessible at [http://localhost:8080/][install_loc]. Stop the application with `CTRL+C`.
	```
	docker-compose up
	```


### Rebuilding Changes

Any changes to the code can be rebuilt by adding the build tag when starting the application.
```
docker-compose up --build
```

If there are any errors or the docker image needs to be built from scratch, run the following.
```
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### Data Persistance

Data is automatically persisted in a Docker volume across shutdowns. Erase the persisted data by running the following.
```
docker-compose down -v
```

### Additional Information

Docker uses the `/docker-compose.yml` to startup the application. For more information about how the application is deployed, look at `/docker-compose.yml`, `/Dockerfile`, and the files in the `/build-db` and `/build-web` folders.

## Tomcat Method

To host DataSpread locally on Tomcat, you can either use one of the pre-build WAR files, available [here][warlink], or build the WAR file yourself from the source.

### Required Software

* [Java Platform (JDK)][java] >= 8
* [PostgreSQL][postgres] >= 10.5
* [PostgreSQL JDBC driver][jdbc] = 42.1.4
* [Apache Tomcat][tomcat] >= 8.5.4
* [Apache Maven][maven] >= 3.5.0
* [NodeJS][node] >= 10.9

### Building Instructions (To generate a WAR file)

1. Clone the DataSpread repository. Alternatively, you can download the source as a zip or tar.gz. 

2. Use maven to build the `war` file using the following command.  After the build completes, the WAR is available at `webapp/target/DataSpread.war`. 

	```
	mvn clean install
	```

### Deploying DataSpread locally. 

1. Install PostgreSQL database. [Postgres.app][Postgres.app] is a quick way to get PostgreSQL working on Mac. For other operating systems check out the guides [here][postgre_install].  

2. Create a database and an user who has access to the database.  Note the database name, username and password. Typically when you have PostgreSQL installed locally the password is blank.  

3. Install Apache Tomcat. You can use the guide [here][tomcat_install]. Make a note of the directory where tomcat is installed. This is known as `TOMCAT_HOME` in all documentation. 

4. Update the Tomcat configuration. You need to update the following file, which is present in `conf` folder under `TOMCAT_HOME` folder.  

    1. `context.xml` by adding the following text at the end of the file before the closing XML tag.   

	```
	<Resource name="jdbc/ibd" auth="Container"
	          type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
	          url="jdbc:postgresql://127.0.0.1:5432/<database_name>"
	          username="<username>" password="<password>"
                  maxTotal="20" maxIdle="10" maxWaitMillis="-1" defaultAutoCommit="false" accessToUnderlyingConnectionAllowed="true"/>
	```

	Replace `<database_name>`, `<username>` and `<password>` with your PostgreSQL's database name, user name and password respectively.

5. Copy `postgresql-42.1.4.jar` (Download from [here][jdbc]) to `lib` folder under `TOMCAT_HOME`.  It is crucial to have the exact version of this file. 
 
6. Deploy the WAR file within Tomcat as the root application. This can be done via Tomcat's web interface by undeploying any application located at `/` and deploying the WAR file with the context path `/`. To do this manually, delete the `webapps/ROOT` folder under `TOMCAT_HOME` while the application is not running, copy the WAR file to the `webapps` folder, and rename it to `ROOT.war`. 

7. Now you are ready to run the program. Visit the url where Tomcat is installed. It will be typically [http://localhost:8080/][install_loc] for a local install.


License
----
MIT

[install_loc]: http://localhost:8080/
[tomcat_install]: https://www.ntu.edu.sg/home/ehchua/programming/howto/Tomcat_HowTo.html
[postgre_install]: https://wiki.postgresql.org/wiki/Detailed_installation_guides
[Postgres.app]: http://postgresapp.com
[jdbc]: https://repo1.maven.org/maven2/org/postgresql/postgresql/42.1.4/postgresql-42.1.4.jar
[ant]: https://ant.apache.org/bindownload.cgi
[tomcat]: http://tomcat.apache.org/download-80.cgi
[java]: http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html
[postgres]:https://www.postgresql.org/download/
[siteinfo]: http://kite.cs.illinois.edu:8080
[zksite]: https://www.zkoss.org/product/zkspreadsheet
[postgressite]: https://www.postgresql.org/
[warlink]: https://github.com/dataspread/releases/releases
[dataspread-github]: http://dataspread.github.io
[dataspread-site]: http://data-people.cs.illinois.edu/dataspread.pdf
[maven]: https://maven.apache.org/install.html
[node]: https://nodejs.org/en/download/current/
[docker]: https://www.docker.com/products/docker-desktop
