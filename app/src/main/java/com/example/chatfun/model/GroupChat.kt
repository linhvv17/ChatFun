package com.example.chatfun.model

class GroupChat {
    var sender: String= ""
    var message: String= ""
    var timestamp: String= ""
    var type: String= ""

    constructor(
        sender: String,
        message: String,
        timestamp: String,
        type: String

    ) {
        this.sender=sender
        this.message=message
        this.timestamp=timestamp
        this.type=type
    }

    // make sure to have an empty constructor inside ur model class
    constructor() {}
}