$(function() {
  var wsUri = "ws://cabin.nyampass.com/ws";
  var $promoted = $('#promoted');

  var websocket;
  var peerId;

  function startWebsocket() {
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) { onOpen(evt) };
    websocket.onclose = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror = function(evt) { onError(evt) };
  }

  function onOpen(evt) {
    appendMessage("CONNECTED");
  }

  function onClose(evt) {
    appendMessage("DISCONNECTED");
  }

  function onMessage(evt) {
    var rawMessage = evt.data;
    var message;

    appendMessage('RESPONSE: ' + rawMessage);

    message = JSON.parse(rawMessage);
    if (message.type == 'connected') {
      peerId = message["peer-id"];
      app.setPeerId(peerId);
    } else if (message.type == 'promote' && message.status == 'ok') {
      $promoted.prop('checked', true);
    } else if (message.type == 'demote' && message.status == 'ok') {
      $promoted.prop('checked', false);
    }
  }

  function onError(evt) {
    appendMessage('ERROR: ' + evt.data);
  }

  function doSend(message) {
    appendMessage("SENT: " + message);
    websocket.send(message);
  }

  function appendMessage(message) {
    app.log(message)
  }

  startWebsocket();

  window.onPromotedChanged = function(checked, password) {
    if (checked) {
      var request = {type: 'promote', from: peerId, password: password};
      doSend(JSON.stringify(request));
    } else {
      var request = {type: 'demote', from: peerId};
      doSend(JSON.stringify(request));
    }
  }

  window.firmata = function() {
    console.log("called firmata");
    this.send = function() {
        console.log("called firmata.send");
    }
  }

  window.delay = function(second) {
    app.delay(second);
  }

  window.run = function(code) {
    eval("function evalInContext() {" + code + ";}");
    evalInContext.call();
  }
});