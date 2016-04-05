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

    $("#scatterplot-canvas-id").hide();

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

    $("#scatterplot-canvas-id").hide();

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

            if (response.type == "json") {
                plotScatterplot(response.payload);
            }
            
            $('#answer-output-id').val(response.text);
            $('#question-input-id').val(question);

            if ($("#speech-input-type-rb").is(':checked')) {
                var msg = new SpeechSynthesisUtterance(response.text);
                msg.lang = 'en-US';
                window.speechSynthesis.speak(msg);
            }

        },
        error: function(e) {}
    });
}

function plotScatterplot(payload) {
    payloadObj = (JSON.parse(payload));

    var labels = [];
    if (payloadObj.labels == null) {
        for (var i = 0; i < JSON.parse(payloadObj.data).length; i++) {
            labels.push("");
        }
    } else {
        labels = JSON.parse(payloadObj.labels);
    }

    $("#scatterplot-canvas-title-id").html(payloadObj.title);
    $("#xaxis-scatterplot-canvas-id").html(payloadObj.xaxis);
    $("#yaxis-scatterplot-canvas-id").html(payloadObj.yaxis);

    var lineChartData = {
        labels: labels,
        datasets: [{
            label: payloadObj.title,
            fillColor: "rgba(151,187,205,0.2)",
            strokeColor: "rgba(151,187,205,1)",
            pointColor: "rgba(151,187,205,1)",
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: "rgba(151,187,205,1)",
            data: JSON.parse(payloadObj.data)
        }]
    }

    $("#scatterplot-canvas-id").show();

    var ctx = document.getElementById("scatterplot-canvas-graph-id").getContext("2d");
    window.myLine = new Chart(ctx).Line(lineChartData, {
        responsive: true,
        onAnimationComplete: scatterplotDoneDrawing
    });

    function scatterplotDoneDrawing() {
        $("#export-canvas-button").html("<button type=\"button\" class=\"pull-right btn btn-default\" id=\"export-canvas-button\"><a download=\"" + payloadObj.title + ".png\" href=" + ctx.canvas.toDataURL() + ">Export</a></button>");
    }
}