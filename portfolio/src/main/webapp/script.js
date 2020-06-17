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
  // Extract the number of comments selected from the dropdown.
  let numCommentsSelect = document.getElementById('num-comments');
  let numCommentsString = numCommentsSelect.options[numCommentsSelect.selectedIndex].value;

  // Create fetch string and perform GET request.
  let searchParams = new URLSearchParams();
  searchParams.append('num-comments', encodeURIComponent(numCommentsString));
  fetch('/data?' + searchParams).then(function(response) {
    let commentsContainer = document.getElementById('comments-container');
    if (response.ok) {
      response.json().then((commentBundles) => {
        // Reset comments continer.
        commentsContainer.innerHTML = '';

        commentBundles.forEach((commentBundle) => {
          let message = commentBundle.message;
          let userEmail = commentBundle.userEmail;

          // Create HTML for comment.
          let commentP = document.createElement("p");
          commentP.innerHTML = message;

          let commentPosterDiv = document.createElement("div");
          commentPosterDiv.classList.add('comment-poster-name');
          commentPosterDiv.innerText = 'Posted by ' + userEmail;

          // Add comment paragraph to comments-box div
          let commentDiv = document.createElement('div');
          commentDiv.classList.add('comments-box');

          commentDiv.appendChild(commentPosterDiv);
          commentDiv.appendChild(commentP);

          // Add comment div to container.
          commentsContainer.appendChild(commentDiv);
        });
      });
    } else {
      commentsContainer.innerHTML = '<p>You must be logged in to see comments.</p>'
    }
  });
}

/**
 * Sends request to delete all comments if user confirms that that is the action
 * they want to take.
 */
function deleteComments() {
  if (getUserLogin()) {
    let doDeleteComments = confirm('Are you sure you want to delete all of the comments?');

    if (doDeleteComments === true) {
      fetch('/delete-data', {
        method:'POST'
      }).then(response => response.json()).then(_ => getComments());
    }
  } else {
    alert("You must be logged in to delete comments.")
  }
}

// Code for adding chart using Google Charts API
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawVisitDayOfWeekChart);
google.charts.setOnLoadCallback(drawVisitDateChart);

function drawVisitDateChart() {
  let searchParams = new URLSearchParams();
  searchParams.append('chart-choice', encodeURIComponent('0'));
  fetch('/visit?' + searchParams).then(response => response.json())
    .then((dailyVisits) => {
      const data = new google.visualization.DataTable();
      data.addColumn('string', 'date');
      data.addColumn('number', 'visits');
      Object.keys(dailyVisits).forEach((date) => {
        data.addRow([date, dailyVisits[date]]);
      });

      const options = {
        'title': 'Page Visits per Day',
        'width':600,
        'height':500
      };

      const chart = new google.visualization.LineChart(
          document.getElementById('visit-date-container'));
      chart.draw(data, options);
    });
}

function drawVisitDayOfWeekChart() {
  let searchParams = new URLSearchParams();
  searchParams.append('chart-choice', encodeURIComponent('1'));
  fetch('/visit?' + searchParams).then(response => response.json())
    .then((dailyVisits) => {
      const data = new google.visualization.DataTable();
      data.addColumn('string', 'Day of Week');
      data.addColumn('number', 'Page Visits');
      data.addRows(
        [['Sunday', dailyVisits['1']],
        ['Monday', dailyVisits['2']],
        ['Tuesday', dailyVisits['3']],
        ['Wednesday', dailyVisits['4']],
        ['Thursday', dailyVisits['5']],
        ['Friday', dailyVisits['6']],
        ['Saturday', dailyVisits['7']]]
      );
      
      const options = {
        'title': 'Number of Page Visits by Day of Week',
        'width': 600,
        'height': 500
      };
    
      const chart = new google.visualization.BarChart(
        document.getElementById('visit-day-week-container'));
      chart.draw(data, options);
    });
}

function getUserLogin() {
  return fetch('login-check').then(response => response.json()).then((userData) => {
    let loginElement = document.getElementById("user-name");
    userLoggedIn = userData.isLoggedIn;
    if (userLoggedIn === false) {
      loginElement.innerHTML = '<p>You are not logged in. Login <a href="/login">here</a>.</p>';
    } else {
      loginElement.innerHTML = '<p>Hello ' + userData.email + '!' +
          ' Logout <a href="/login">here</a>.</p>';
    }
    return userLoggedIn;
  });
}

function startUpWebpage() {
  // Count visit.
  fetch('/visit', {
    method:'POST'
  });

  // Check if logged in and get comments.
  getUserLogin().then((loggedIn) => {
    if (!loggedIn) {
      let element = document.getElementById('comment-input-form-container');
      element.hidden = true;
    }
  });
  getComments();
}