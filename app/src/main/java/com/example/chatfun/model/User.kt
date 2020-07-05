package com.example.chatfun.model

class User {
    var uid: String = ""
    var username: String = ""
    var search: String = ""
    var status: String = ""
    var profile: String = ""
    var cover: String = ""
    var facebook: String = ""
    var instagram: String = ""
    var website: String = ""
    constructor()
    constructor(
        uid: String,
        username: String,
        search: String,
        status: String,
        profile: String,
        cover: String,
        facebook: String,
        instagram: String,
        website: String
    ) {
        this.uid = uid
        this.username = username
        this.search = search
        this.status = status
        this.profile = profile
        this.cover = cover
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
    }
    
}
