/**
 * Copyright 2016 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var finalTranscript = '';
var recognizing = false;
var recognition;

$(document).ready(function() {
    if (!('webkitSpeechRecognition' in window)) {
        alert("Sorry, your Browser does not support the Speech API. ");
        $('#speech-input-type-rb').prop("disabled", true);

    } else {

        recognition = new webkitSpeechRecognition();
        recognition.continuous = true; 
        recognition.interimResults = true; 
        recognition.lang = 'en-US'; 
        
        recognition.onstart = function() {
            $('#question-input-id').val('');
            $('#answer-output-id').val('');

            recognizing = true;
            $('#instructions').html('Speak slowly and clearly');
        };

        recognition.onerror = function(event) {
            if(event.error == "network"){
                alert("There was a recognition error due to"
                        + " no internet connection.\n\n Please check "
                        + " your network connectivity.");

            }else if(event.error == "no-speech"){
                alert("There was a recognition error. No speech"
                        + " was detected. \n\n Please make sure your microphone"
                        + " is setup properly.");

            }else{
                alert("There was a recognition error.\n\n");
            }
        };

        recognition.onend = function() {
            recognizing = false;
            $('#instructions').html('&nbsp;');
            $('question-input-id').val(finalTranscript);

        };

        recognition.onresult = function(event) {
            var interimTranscript = '';
            for (var i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    finalTranscript += event.results[i][0].transcript;
                } else {
                    interimTranscript += event.results[i][0].transcript;
                }
            }

            if (finalTranscript.length > 0) {
                sendPost(finalTranscript);
                recognition.stop();
                $('#start-button').html('Ask Question');
                recognizing = false;
            }
        }
    }
});

function runSpeechToText() {

    $('#start-button').html('Submit');

    if (recognizing) {
        recognition.stop();
        $('#start-button').html('Ask Question');
        recognizing = false;
    } else {
        finalTranscript = '';
        recognition.start();
        $('#instructions').html('Allow the browser to use your Microphone');
    }

}

