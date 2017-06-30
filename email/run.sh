DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'email-client*.jar')
java -jar $JAR