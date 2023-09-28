# evosuite-cit
Evosuite extension integrating pairwise coverage criteria for method parameters values. 
PLEASE   refer to Evosuite.org for the original software from which this work has been derived.

from the base installation dir of this code, to create an executable jar, write on terminal: 
    mvn clean; mvn compile;  mvn package -DskipTests=true
to execute the jar on the class of an hypotetic java project named Tutorial, write : 
    java -jar master/target/evosuite-master-1.0.4-SNAPSHOT.jar -class tutorial.Example -projectCP path/to/Tutorial_project/target/classes  -criterion=CIT
