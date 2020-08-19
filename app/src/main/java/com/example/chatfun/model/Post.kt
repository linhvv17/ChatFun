package com.example.chatfun.model

import com.google.firebase.database.ServerValue

class Post {
    var postId: String? = null
    var postDes: String? = null
    var postTitle: String? = null
    var postImage: String? = null
    var postTime: String? = null
    var postLikes: String? = null
    var uid: String? = null
    var uName: String? = null
    var userProfile: String? = null

    constructor(
         postId: String?,
         postDes: String?,
         postTitle: String?,
         postImage: String?,
         postTime: String?,
         postLikes: String?,
         uid: String?,
         uName: String?,
         userProfile: String?
    ) {
        this.postId=postId
        this.postDes=postDes
        this.postTitle=postTitle
        this.postImage=postImage
        this.postTime=postTime
        this.postLikes=postLikes
        this.uid=uid
        this.uName=uName
        this.userProfile=userProfile
    }

    // make sure to have an empty constructor inside ur model class
    constructor() {}

}