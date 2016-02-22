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
$(document).ready(function() {
    $("#text-input-type-rb").prop("checked", true);

    $("input[name='text']").change(function() {
        $("#speech-input-type-rb").prop("checked", false);
    });

    $("input[name='speech']").change(function() {
        $("#text-input-type-rb").prop("checked", false);
    });

    $("#start-button").click(function() {
        if ($("#text-input-type-rb").is(':checked')) {
            sendPost($("#question-input-id").val());
        } else if ($("#speech-input-type-rb").is(':checked')) {
            runSpeechToText();
        }
    });
});

function sendPost(question) {
    $.ajax({
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        type: "POST",
        url: "api/request",
        data: JSON.stringify({
            "text": question
        }),
        dataType: 'json',
        success: function(response) {
            $('#answer-output-id').val(response.text);
            $('#question-input-id').val(question);

            if ($("#speech-input-type-rb").is(':checked')) {
                var msg = new SpeechSynthesisUtterance(response.text);
                window.speechSynthesis.speak(msg);
            }

        },
        error: function(e) {}
    });
}