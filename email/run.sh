DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'email-client*.jar')
java -cp .:target/$JAR edu.jhuapl.dorset.demos.EmailClient