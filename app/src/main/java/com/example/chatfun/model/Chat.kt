package com.example.chatfun.model

class Chat {
     private var sender: String = ""
     private var receiver: String = ""
     private var message: String = ""
     private var isSeen: Boolean = false
     private var url: String = ""
     private var messageId: String = ""

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
    //sender
    fun getSender(): String?{
        return sender
    }
    fun setSender(sender: String){
        this.sender = sender
    }
    //receiver
    fun getReceiver(): String?{
        return receiver
    }
    fun setReceiver(receiver: String){
        this.receiver = receiver
    }
    //message
    fun getMessage(): String?{
        return message
    }
    fun setMessage(message: String){
        this.message = message
    }
    //isSeen
    fun isIsSeen(): Boolean?{
        return isSeen
    }
    fun setIsSeen(isSeen: Boolean){
        this.isSeen = isSeen
    }
    //url
    fun getUrl(): String?{
        return url
    }
    fun setUrl(url: String){
        this.url = url
    }
    //messageId
    fun getMessageId(): String?{
        return messageId
    }
    fun setMessageId(messageId: String){
        this.messageId = messageId
    }


}