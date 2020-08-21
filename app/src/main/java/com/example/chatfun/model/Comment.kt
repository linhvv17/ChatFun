package com.example.chatfun.model

class Comment {
    var commentId: String? = null
    var commentContent: String? = null
    var commentTime: String? = null
    var userCommentId: String? = null
    var userCommentName: String? = null
    var userCommentProfile: String? = null
    constructor(
        commentId: String?,
        commentContent: String?,
        commentTime: String?,
        userCommentId: String?,
        userCommentName: String?,
        userCommentProfile: String?

    ) {
        this.commentId=commentId
        this.commentContent=commentContent
        this.commentTime=commentTime
        this.userCommentId=userCommentId
        this.userCommentName=userCommentName
        this.userCommentProfile=userCommentProfile
    }

    // make sure to have an empty constructor inside ur model class
    constructor() {}

}