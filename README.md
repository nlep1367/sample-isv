sample-isv
==========

Sample application that integrates with AppDirect for distribution.

# Run Locally

Create MySQL database schema:

	create database isv CHARACTER SET utf8 COLLATE utf8_unicode_ci;

Launch the application:

	mvn clean install
	cd sample-isv-web/
	mvn -Dspring.profiles.active=local spring-boot:run

# Deploy to Cloud Foundry

	mvn clean install
	cd sample-isv-web/
	mvn -o cf:push -Dcf.username={username} -Dcf.password={password}
