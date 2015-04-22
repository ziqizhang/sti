@echo off
set submvn=start /WAIT cmd /C mvn
@echo installing twitter-corpus-tools
%submvn% install:install-file -DgeneratePom=true -Dfile=lib/twitter-corpus-tools-0.0.1.jar -DgroupId=com.twitter.corpus -DartifactId=twitter-corpus-tools -Dversion=0.0.1 -Dpackaging=jar

@echo installing langdetect
%submvn% install:install-file -DgeneratePom=true -Dfile=lib/langdetect.jar -DgroupId=com.cybozu.labs -DartifactId=langdetect -Dversion=0.0.1 -Dpackaging=jar

@echo installing jsonic
%submvn% install:install-file -DgeneratePom=true -Dfile=lib/jsonic-1.1.3.jar -DgroupId=jsonic -DartifactId=jsonic -Dversion=1.1.3 -Dpackaging=jar

@echo installing mallet
%submvn% install:install-file -DgeneratePom=true -Dfile=lib/mallet.jar -DgroupId=mallet -DartifactId=mallet -Dversion=2.0.7 -Dpackaging=jar

@echo installing mallet-deps
%submvn% install:install-file -DgeneratePom=true -Dfile=lib/mallet-deps.jar -DgroupId=malletdeps -DartifactId=malletdeps -Dversion=2.0.7 -Dpackaging=jar
