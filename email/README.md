# Email Client
This demonstration of Dorset polls an email server for incoming messages. It sends the message text to the appropriate Dorset agent, which returns a response. A reply is then sent back to the original sender.  

## Build
mvn clean package  

## Configurations
See sample.conf for an example of configurations.   
Sample ports and hosts are for a gmail server.  
Configuration file must be named application.conf

## Run
If you're using Windows, run the Email Client with:  
```
./run.sh
```

If you're using Mac/Linux, run the Email Client with:  
```
./run.bat
```

## Available Agents:
 - Date/Time agent: ask what the time is or what is today's date  