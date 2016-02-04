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
 
 function submitQuestion() {
     var input = document.getElementById('questionInputId').value;
     console.log("The question asked was: " + input);
     sendGet(input);
 }

 function sendGet(question) {
     $.ajax({
         headers: {
             'Accept': 'application/json',
             'Content-Type': 'application/json'
         },
         type: "GET",
         url: "api/process/" + question,
         dataType: 'json',
         success: function(response) {
             console.log("Success!");
             console.log("The answer to the question is: " + response.text);
             document.getElementById('answerOutputId').value = response.text;

         },
         error: function(e) {
             console.log("AJAX error");
             console.log(e);
         }
     });
 }