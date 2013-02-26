ToDo's Sample Dropwizard Project
=============

A sample Dropwizard Project.  Setup to use DropWizard with Spring Data for MongoDB and ElasticSearch.
Allows the following to be CRUD'd:  Users and Tasks.  Also supports basic healthchecks and metrics.

Pre-Requisites
--------------
* JDK 1.6.0_37 (minimum)
* Eclipse (preferred) - use Java EE edition
    * m2e Maven plugin
    * TestNG plugin
* Install MongoDB on your machine (http://www.mongodb.org/display/DOCS/Quickstart)
* Install ElasticSearch on your machine (http://www.elasticsearch.org/guide/reference/setup/installation.html)

Basic Build and Usage
---------------------

* *Clone*: git clone git@github.com:pnayak/tasks.git
* *Build*: mvn clean compile package
* *Run*:   
    * java -jar todos-service/target/todos-service.jar server todos-service/todos-service.yaml
* *Verify*: 
    * *Basic/admin services*
        * curl http://localhost:8080/about
        * curl http://localhost:8081/healthcheck
        * curl http://localhost:8081/metrics
    * *task*
        * The URI is of the form /task
        * *Add*: curl -H "Content-Type: application/json" -XPUT http://localhost:8080/task -i -d '{"title" : "Get Milk" }' // Creates with auto-generated UUID = 50b7b169da064d15987eab4c 
        * *Fetch one*: curl -XGET
          http://localhost:8080/task/50b7b169da064d15987eab4c
        * *Fetch all*: curl -XGET http://localhost:8080/task
        * *Search*: curl -XGET localhost:9200/task/_search?pretty=true -d '{"query" : {"term" : {"title" : "Milk"}}}'
        * *Delete*: curl -i -XDELETE http://localhost:8080/task/50b7b169da064d15987eab4c
* Rock On!

Development
-----------
* More details coming soon...
