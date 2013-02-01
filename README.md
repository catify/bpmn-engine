Catify BPMN Engine
====================

The catify bpmn engine is based on Akka (http://akka.io/) and Neo4j (http://neo4j.com/) and is fully compatible to BPMN 2.0. 
It can handle millions of long running processes out of the box.
Though not yet feature complete, it is easy to extend via different service provider interfaces. 

You can easily integrate the engine into your infrastructure. Via the Integration SPI you can access it by the integration framework of your choice. Out of the box it comes with Apache Camel (http://camel.apache.org/) and therefore all of its strenghts.

Build:

To build the project please run the usual:

"mvn clean package"

To add the Apache Camel fun please build the according project you can find in the catify repo (bpmn-engine-integration-spi-camel) and place the jar of that build process in the classpath of the bpmn engine. Do this for all other services needed as well (like Timer and DataObject).

We also provide a distribution package that has camel and jpa service providers built in and is ready to use (https://github.com/catify/bpmn-engine-dist-jpa-camel).

Test-run:

You are now ready to make a first test run. You can find ready to use processes in "/src/test/resources/data".
Let's start with the test-process called

"testprocess_throw_integrationStart.bpmn"

As its name suggests, it will use the Integration SPI to fire up two Camel routes.
The first one is located at the Start Event (you can tell from the xml). When the process is started the folder "target/data/testprocess_throw_integrationStart/startInstance" will be created.

Place a file in that folder. The engine will grab the file and create a process instance. You will then see the debugging logs of the running process in your console.
The second route is located at the Throwing Intermediate Event and will create the folder "data/testprocess_throw_integrationStart/integrationOutput" on process start. When a proces instances gets to the Intermediate Event a file will be created here.

While the process engine is running, you can access the Neo4j web console at http://localhost:7474/webadmin/. To get an even closer look at the process data you can use Neoclipse (https://github.com/neo4j/neoclipse/downloads).
