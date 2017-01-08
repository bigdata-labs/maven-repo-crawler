Maven Repo Crawler
---
a crawler of maven's central repository, aim at getting all jar's pom file
 to analysing the whole jars dependences tree map.
 
##Get start
1. set up redis with default port `6372` if you haven't redis in your laptop. Or you can connect exists redis by changing env.properties
1. update mysql's username or password in ebean.properties 
1. create database named `crawler`: CREATE SCHEMA `crawler` DEFAULT CHARACTER SET utf8mb4 ; for now, default database is mysql
1. cd mavenrepocrawler
1. nohup mvn clean install exec:java &
1. tail -f nohup.out