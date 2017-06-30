DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'email-client*.jar')
java -cp .:target/email-client-0.4.0-SNAPSHOT.jar edu.jhuapl.dorset.demos.EmailClient