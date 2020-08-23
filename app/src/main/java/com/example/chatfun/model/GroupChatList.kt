package com.example.chatfun.model

class GroupChatList {
    var groupId: String= ""
    var groupTitle: String= ""
    var groupDescription: String= ""
    var groupIcon: String= ""
    var timestamp: String= ""
    var createdBy: String= ""


    constructor(
        groupId: String,
        groupTitle: String,
        groupDescription: String,
        groupIcon: String,
        timestamp: String,
        createdBy: String

    ) {
        this.groupId=groupId
        this.groupTitle=groupTitle
        this.groupDescription=groupDescription
        this.groupIcon=groupIcon
        this.timestamp=timestamp
        this.createdBy=createdBy
    }

    // make sure to have an empty constructor inside ur model class
    constructor() {}

}