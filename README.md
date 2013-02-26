Todos Sample Dropwizard Project
=============

A sample Dropwizard Project.

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

* *Clone*: git clone git@github.com:pnayak/todos.git
* *Build*: mvn clean compile package
* *Run*:   
    * java -jar todos-service/target/todos-service.jar server todos-service/todos-service.yaml
* *Verify*: 
    * *Basic/admin services*
        * curl http://localhost:8080/about
        * curl http://localhost:8081/healthcheck
        * curl http://localhost:8081/metrics
    * *ToDo*
        * The URI is of the form /todo
        * *Add*: curl -H "Content-Type: application/json" -XPUT http://localhost:8080/todo -i -d '{"title" : "Get Milk" }' // Creates with auto-generated UUID = 50b7b169da064d15987eab4c 
        * *Fetch one*: curl -XGET
          http://localhost:8080/todo/50b7b169da064d15987eab4c
        * *Fetch all*: curl -XGET http://localhost:8080/todo
        * *Search*: curl -XGET localhost:9200/learningcontent/_search?pretty=true -d '{"query" : {"term" : {"title" : "Milk"}}}'
        * *Delete*: curl -i -XDELETE http://localhost:8080/todo/50b7b169da064d15987eab4c
* Rock On!

Development
-----------
* More details coming soon...
