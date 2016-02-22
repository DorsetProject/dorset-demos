from flask import Flask, request
import json
import logging
import ephem
import time
from logging.handlers import RotatingFileHandler

app = Flask("astronomy")
file_handler = RotatingFileHandler('error.log', 'a', 1 * 1024 * 1024, 5)
file_handler.setFormatter(logging.Formatter('%(asctime)s %(levelname)s: %(message)s'))
app.logger.addHandler(file_handler)
app.logger.setLevel(logging.getLevelName('INFO'))


class AgentRequest(object):
    def __init__(self, text):
        self.text = text


class AgentMessages(object):
    success = 0
    no_response = 100
    bad_request = 101
    invalid_response = 102


class AgentResponse(object):
    def __init__(self, text='', code=AgentMessages.success):
        self.text = text
        self.code = code

    def export(self):
        return {"text": self.text, "code": self.code}


@app.route('/request', methods=['POST'])
def process():
    data = request.get_json(silent=False)
    app.logger.info("Handling request with text: " + data['text'])

    if "new moon" in data['text']:
        d = ephem.next_new_moon(time.strftime("%Y/%m/%d"))
        print(time.strftime("%Y/%m/%d"))
        strResponse = 'The next new moon is ' + str(d)

    elif "full moon" in data['text']:
        d = ephem.next_full_moon(time.strftime("%Y/%m/%d"))
        strResponse = 'The next full moon is ' + str(d)

    else:
        strResponse = 'I am sorry, I don\'t understand your question' \
                      ' regarding moons.'

    response = AgentResponse(text=strResponse)

    return json.dumps(response.export())


@app.route('/ping', methods=['GET'])
def ping():
    return json.dumps("pong")


if __name__ == '__main__':
    app.run(debug=True)