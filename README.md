# Lucy's Love Bus Backend

This project is the Java backend api scaffold for lucys-love-bus.

[![Build Status](https://travis-ci.org/Code-4-Community/lucys-love-bus-backend.svg?branch=master)](https://travis-ci.org/Code-4-Community/lucys-love-bus-backend)

[![Coverage Status](https://coveralls.io/repos/github/Code-4-Community/lucys-love-bus-backend/badge.svg?branch=master)](https://coveralls.io/github/Code-4-Community/lucys-love-bus-backend?branch=master)


#### Start a local postgres database
1. Download postgres with pgAdmin4. https://www.postgresql.org/download/
2. Start pgAdmin4, the database should start on localhost, port 5432 by default.
3. You must create a login/ group role that you can connect to the database with.
On the left side bar on pgAdmin open Login/Group Roles, right click and select create.
Set the name to a username of your choosing.
Under the Definition tab, set a password for the user.
Under the Privileges tab, select yes for every option.
4. Create a Database schema named 'lucys-love-bus' with your created user as the owner.
Right click Databases and select create.
Set the name to 'checkin' and then select your created user.

#### Update Secret Files
1. Create a copy of the file `/common/src/main/resources/db.properties.example` at `/common/src/main/resources/db.properties`.
2. Update the file at `/common/src/main/resources/db.properties` to contain your database connection information.
You should only have to change the username and password.
3. Copy the other `.example` files and remove the `.example` extension when you do so.

#### Compile the code base
1. Run `mvn clean install` from the root directory
  - Run `mvn spotless:apply` to apply code formatting corrections to your code if 
  your build is failing because of `spotless:check`

#### Run the API
1. The `ServiceMain.java` class has the main method for running the code, this can be run directly in IntelliJ.
Alternatively: `mvn install` creates a jar file at:
`service/target/service-1.0-SNAPSHOT-jar-with-dependencies.jar`.
This can be run from the command line with the command `java -jar service-1.0-SNA....`

#### Hitting the API
By default the API is accessible at `http://localhost:8081`. All routes
have the `/api/v1` prefix to them.

For example, the HTTP request to login would be:
`GET http://localhost:8081/api/v1/user/login`
