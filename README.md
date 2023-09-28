# evosuite-cit
Evosuite extension integrating pairwise coverage criteria for method parameters values. 

PLEASE   refer to Evosuite.org for the original software from which this work has been derived.

From the base installation dir of this code, to create an executable jar, write on terminal: 

 Prompt>   mvn clean; mvn compile;  mvn package -DskipTests=true
 
To execute the jar on the class of an hypotetic java project named Tutorial, write : 

  Prompt>  java -jar master/target/evosuite-master-1.0.4-SNAPSHOT.jar -class tutorial.Example -projectCP path/to/Tutorial_project/target/classes  -criterion=CIT

