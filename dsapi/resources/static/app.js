var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/dsapi-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/callback/updateBooks', function (msg) {
            console.log(msg);
            showGreeting(msg.body);
        });

    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "/api/addBook", true);
    xhttp.setRequestHeader("auth-token", "guest");
    xhttp.setRequestHeader("Content-type", "application/json");
    var data = JSON.stringify({"name": $("#name").val()});
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState === 4 && xhttp.status === 200) {
            var json = JSON.parse(xhttp.responseText);
            console.log(json.status);
        }
    };
    xhttp.send(data);
}

function showGreeting(message) {
    $("#greetings").append("<tr><td> Book " + message + " Added </td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});

