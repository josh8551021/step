// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Fetches comments data from the server and uploads it to the portfolio main
 * page.
 */
function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
    let commentsContainer = document.getElementById('comments-container');
    comments.forEach((comment) => {
      // Create HTML for comment.
      let commentP = document.createElement("p");
      commentP.innerHTML = comment;

      // Add comment paragraph to comments-box div
      let commentDiv = document.createElement("div");
      commentDiv.classList.add("comments-box");
      commentDiv.appendChild(commentP);

      // Add comment div to container.
      commentsContainer.appendChild(commentDiv);
    });
  });
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawVisitDataChart);

function drawVisitDataChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Day of Week');
  data.addColumn('number', 'Page Visits');
  data.addRows([
    ['Sunday', 40],
    ['Monday', 55],
    ['Tuesday', 30],
    ['Wednesday', 20],
    ['Thursday', 25],
    ['Friday', 65],
    ['Saturday', 50]
  ]);

  const options = {
    'title': 'Number of Page Visits by Day of Week',
    'width': 600,
    'height': 500
  };

  const chart = new google.visualization.BarChart(
    document.getElementById('visit-data-container'));
  chart.draw(data, options);
}