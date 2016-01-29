mvn install:install-file -DgroupId=apache.any23 -DartifactId=any23.modified -Dversion=0.7 -Dpackaging=jar -Dfile=libs/*.jar
mvn install:install-file -DgroupId=nlp.dragontools -DartifactId=dragontools -Dversion=1.0 -Dpackaging=jar -Dfile=libs/dragontool.jar
mvn install:install-file -DgroupId=nlp.simmetrics -DartifactId=simmetrics -Dversion=1.0 -Dpackaging=jar -Dfile=libs/simmetrics_jar_v1_6_2_d07_02_07.jar
