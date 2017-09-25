import argparse
import urllib.parse
import json
import time
import requests
from ws4py.client.threadedclient import WebSocketClient
import pyaudio


class KaldiClient(WebSocketClient):
    """
    Client that sends raw audio captured from a microphone to a kaldi gstreamer server via a websocket.
    Accepts a callback to call once a transcription is received.
    """
    FRAMES_PER_BUFFER = 1024

    def __init__(self, speech_url, rate, recognition_callback, partial_recognition_callback):
        """
        Initialize the client. This opens the audio device.
        :param speech_url: the websocket url to send the audio to
        :param rate: the rate of the audio
        :param recognition_callback: a function that accepts a string. called when the final transcription is received.
        :param partial_recognition_callback: a function that accepts a string. called when a partial transcription is received
        """
        super(KaldiClient, self).__init__(speech_url)

        self.py_audio = pyaudio.PyAudio()
        self.audio_stream = self.py_audio.open(channels=1, format=pyaudio.paInt16, rate=rate, input=True,
                                               frames_per_buffer=self.FRAMES_PER_BUFFER)

        self.connected = False
        self.recognition_callback = recognition_callback
        self.partial_recognition_callback = partial_recognition_callback

    def connect(self):
        """
        Simple override of the base class connect to make this call blocking.
        :return: None
        """
        super().connect()
        while not self.connected:
            time.sleep(0.5)

    def opened(self):
        """
        Called when the connection the the server is opened.
        :return: None
        """
        print('Connected to server... Speak into your microphone!')
        self.connected = True

    def stream_audio_to_server(self):
        """
        Sends audio from the microphone over the websocket connection.
        This blocks until the websocket connection ends.
        :return: None
        """
        while self.connected:
            data = self.audio_stream.read(num_frames=self.FRAMES_PER_BUFFER)
            self.send(data, binary=True)

    def received_message(self, message):
        """
        Called when a message is received over the websocket connection.
        This method will call the callbacks registered through the constructor.
        :param message: 
        :return: None
        """
        response = json.loads(str(message))
        if response['status'] == 0:
            text = response['result']['hypotheses'][0]['transcript']
            if 'result' in response:
                if response['result']['final']:
                    self.recognition_callback(text)
                else:
                    self.partial_recognition_callback(text)
        else:
            print('Received error from server (status {})'.format(response['status']))
            if 'message' in response:
                print('Error message: {}'.format(response['message']))

    def closed(self, code, reason=None):
        """
        Called when the connection is closed
        :param code:
        :param reason:
        :return:
        """
        print('Disconnected from server: {}'.format(reason))
        self.connected = False


def main():
    parser = argparse.ArgumentParser(
        description='command line client for sending audio to a kaldi-gstreamer-server')
    parser.add_argument('kaldi_server', default='localhost:8888',
                        help='address of server that does speech to text. Default=localhost:8888')
    parser.add_argument('dorset_server', default="localhost:8888",
                        help='address of server that answers requests. Default=localhost:8888')
    parser.add_argument('--rate', required=False, default=16000, help='rate of the audio. Default=16000')
    args = parser.parse_args()

    content_type = 'audio/x-raw,'
    content_type += 'layout=(string)interleaved,'
    content_type += 'rate=(int){},'.format(args.rate)
    content_type += 'format=(string)S16LE,'
    content_type += 'channels=(int)1'

    speech_recognition_url = 'ws://{}/client/ws/speech?{}'.format(args.kaldi_server, urllib.parse.urlencode(
        [("content-type", content_type)]))

    exit_phrases = ['quit', 'exit', 'stop listening']

    def request_answer(text):
        if text.replace('.', '') in exit_phrases:
            print('Quitting...')
            kaldi_client.connected = False
        else:
            print('You asked: {}'.format(text))
            response_str = requests.post(args.dorset_server + '/api/request', json={'text': text})
            response = json.loads(response_str.text)
            print('Answer: {}'.format(response['text'].lstrip()))

    def print_partial_transcription(text):
        print(text, end='\r', flush=True)

    kaldi_client = KaldiClient(speech_recognition_url, args.rate,
                               recognition_callback=request_answer,
                               partial_recognition_callback=print_partial_transcription)
    kaldi_client.connect()
    print('Say one of {} to quit.'.format(exit_phrases))
    kaldi_client.stream_audio_to_server()


if __name__ == "__main__":
    main()
