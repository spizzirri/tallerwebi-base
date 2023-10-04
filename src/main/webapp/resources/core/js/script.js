const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/spring/wschat'
});

stompClient.debug = function(str) {
    console.log(str)
 };

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/messages', (m) => {
        console.log(JSON.parse(m.body).content);
        const messagesContainer = document.getElementById("chat-messages");
        const newMessage = document.createElement("p")
        newMessage.textContent = JSON.parse(m.body).content;
        messagesContainer.appendChild(newMessage);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

stompClient.activate();

// Take the value in the ‘message-input’ text field and send it to the server with empty headers.
function sendMessage(){

    let input = document.getElementById("message");
    let message = input.value;

    stompClient.publish({
        destination: "/app/chat",
        body: JSON.stringify({message: message})
    });
}

