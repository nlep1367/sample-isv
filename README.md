sample-isv
==========

Sample application that integrates with AppDirect for distribution.

Sample ISV 1 (1.0 integration):

	mvn -P test-isv-1 clean package

Sample ISV 2 (1.1 integration):

	mvn -P test-isv-2 clean package

Artifacts can then be deployed to Cloud Foundry (for example).


To run the application locally:

Create database schema:

	create database isv CHARACTER SET utf8 COLLATE utf8_unicode_ci

Execute spring-boot locally:

	mvn spring-boot:run -Dspring.profiles.active=local
	