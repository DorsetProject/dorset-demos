DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'email-client*.jar')
java -cp .:$JAR edu.jhuapl.dorset.demos.EmailClient