# Email Client
This demonstration of Dorset polls an email server for incoming messages. It sends the message text to the appropriate Dorset agent, which returns a response. A reply is then sent back to the original sender.  

## Build
mvn clean package  

## Configurations
See sample.conf for an example of configurations.   
Sample ports and hosts are for a gmail server.  

## Run
The Email Client is run with:  
```
java -jar target/[email client jar name]  
```

## Available Agents:
 - Date/Time agent: ask what the time is or what is today's date