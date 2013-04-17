echo off
cls
echo Starting PSCafePOS
set CLASSPATH=.;lib/APGJposService171.jar;lib/jcl_editor.jar;lib/mysql-connector-java-5.1.8-bin.jar;lib/xerces.jar;lib/Jpos17.jar;
set CLASSPATH=%CLASSPATH%;lib/derby.jar;lib/jconn3.jar;lib/postgresql-8.4-701.jdbc3.jar;lib/commons-dbcp-1.2.2.jar;lib/commons-pool-1.5.3.jar;lib/USB554JNI.dll  
java -cp "%CLASSPATH%" -Djava.util.logging.config.file=etc/logging.properties -Duser.home=. org.pscafepos.PSCafePos etc/settings.dbp

echo Done

