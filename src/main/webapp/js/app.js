/* SpendWise: tiny helpers. The app works without JavaScript except for these conveniences. */
(function () {
  'use strict';

  // Ask before any form marked data-confirm="..." is submitted (used for deletes).
  document.addEventListener('submit', function (event) {
    var message = event.target.getAttribute('data-confirm');
    if (message && !window.confirm(message)) {
      event.preventDefault();
    }
  });

  // Mobile menu toggle.
  var button = document.getElementById('menuBtn');
  var sidebar = document.getElementById('sidebar');
  if (button && sidebar) {
    button.addEventListener('click', function () {
      var open = sidebar.classList.toggle('open');
      button.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
  }

  // Stop double submits: disable the submit button once the form is sent.
  document.addEventListener('submit', function (event) {
    if (event.defaultPrevented) { return; }
    var submit = event.target.querySelector('button[type=submit].btn-primary');
    if (submit) {
      window.setTimeout(function () { submit.disabled = true; }, 0);
    }
  });
})();
