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
./run.bat
```

If you're using Mac/Linux, run the Email Client with:  
```
./run.sh
```

## Available Agents:
 - Date/Time agent: ask what the time is or what is today's date  
 
## How To Use:
- Send an email to DorsetTest123@gmail.com
- The email should ask a question in the subject, unless it is responding to an email from DorsetTest123@gmail.com, then the subject should be left as is
- See Available Agents section for example questions