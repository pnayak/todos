Knowledge Hub
=============

The Pragya Knowledge Hub.

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

* *Clone*: git clone git@github.com:PragyaSystems/knowledgeHub.git
* *Build*: mvn clean compile package
* *Run*:   
    * java -jar knowledgeHub-service/target/knowledgeHub-service.jar server knowledgeHub-service/knowledgeHub-service.yaml
* *Verify*: 
    * *Basic/admin services*
        * curl http://localhost:8080/about
        * curl http://localhost:8081/healthcheck
        * curl http://localhost:8081/metrics
    * *LearningContent*
        * The URI is of the form /{spaceId}/learningcontent
        * *Add*: curl -H "Content-Type: application/json" -XPUT http://localhost:8080/1234/learningcontent -i -d '{"title" : "quiz", "uri":"http://www.yahoo.com/math.html"}' // Creates with auto-generated UUID = 50b7b169da064d15987eab4c 
        * *Fetch one*: curl -XGET
          http://localhost:8080/1234/learningcontent/50b7b169da064d15987eab4c
        * *Fetch all*: curl -XGET http://localhost:8080/1234/learningcontent
        * *Search*: curl -XGET localhost:9200/learningcontent/_search?pretty=true -d '{"query" : {"term" : {"title" : "quiz"}}}'
        * *Delete*: curl -i -XDELETE http://localhost:8080/1234/learningcontent/50b7b169da064d15987eab4c
    * *LearningTopic*
        * The URI is of the form /{spaceId}/learningtopic
        * *Add*: curl -H "Content-Type: application/json" -XPUT http://localhost:8080/1234/learningtopic -i -d '{"title":"Calculus","description":"test description","children":[{"uuid":"50b7b169da064d15987eab4c"}]}'  // Creates with auto-generated UUID = 50b7c1afda065424ee85d30a.  Note we pass a ref. to a previously created LearningContent with uuid = 50b7b169da064d15987eab4c
        * *Fetch one*: curl -XGET http://localhost:8080/1234/learningtopic/50b7c1afda065424ee85d30a
        * *Fetch all*: curl -XGET http://localhost:8080/1234/learningtopic
        * *Search*: curl -XGET localhost:9200/learningtopic/_search?pretty
        * *Delete*: curl -i -XDELETE http://localhost:8080/1234/learningtopic/50b7c1afda065424ee85d30a
* Rock On!

Development
-----------
* The *CI server* is located @ http://50.57.221.20:8111/overview.html
* More details coming soon...
