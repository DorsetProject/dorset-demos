Slackbot
===============
This demonstration of Dorset uses a single echo agent and a basic router to provide a bot for Slack. 
You'll need to get a bot key from the [new bot page](https://my.slack.com/services/new/bot) and paste it into the slackbot.properties file.

Build
-----------
A self-contained application jar with all its dependencies is built with:

```
mvn clean package
```

Run
----------
The slackbot demo is run with:

```
java -jar target/[slackbot jar name]
```

or

use run.bat on windows
use run.sh on linux / mac


