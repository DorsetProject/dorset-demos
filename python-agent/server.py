# Copyright 2016-2017 The Johns Hopkins University Applied Physics Laboratory LLC
# All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from flask import Flask, request
import json
import logging
import ephem
import time
from logging.handlers import RotatingFileHandler
from dorset import Dorset

app = Flask("astronomy")
file_handler = RotatingFileHandler('error.log', 'a', 1 * 1024 * 1024, 5)
file_handler.setFormatter(logging.Formatter('%(asctime)s %(levelname)s: %(message)s'))
app.logger.addHandler(file_handler)
app.logger.setLevel(logging.getLevelName('INFO'))


@app.route('/request', methods=['POST'])
def process():
    agent_request = Dorset.decode_request(request.data)
    app.logger.info("Handling request with text: " + agent_request.text)

    if "new moon" in agent_request.text:
        d = ephem.next_new_moon(time.strftime("%Y/%m/%d"))
        print(time.strftime("%Y/%m/%d"))
        response = 'The next new moon is ' + str(d)

    elif "full moon" in agent_request.text:
        d = ephem.next_full_moon(time.strftime("%Y/%m/%d"))
        response = 'The next full moon is ' + str(d)

    else:
        response = 'I am sorry, I don\'t understand your question' \
                      ' regarding moons.'

    return Dorset.encode_response(text=response)


@app.route('/ping', methods=['GET'])
def ping():
    return json.dumps("pong")


if __name__ == '__main__':
    app.run(debug=True)
