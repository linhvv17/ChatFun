package com.example.chatfun.model

class Chat {
     var sender: String = ""
     var receiver: String = ""
     var message: String = ""
     var isSeen: Boolean = false
     var url: String = ""
     var messageId: String = ""

    constructor()
    constructor(
        sender: String,
        receiver: String,
        message: String,
        isSeen: Boolean,
        url: String,
        messageId: String
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.isSeen = isSeen
        this.url = url
        this.messageId = messageId
    }


}