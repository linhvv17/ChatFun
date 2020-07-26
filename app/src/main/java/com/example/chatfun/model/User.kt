package com.example.chatfun.model

class User {
    private var uid: String = ""
    private var username: String = ""
    private var search: String = ""
    private var status: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var facebook: String = ""
    private var instagram: String = ""
    private var website: String = ""
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
    //sender
    fun getUid(): String?{
        return uid
    }
    fun setUid(uid: String){
        this.uid = uid
    }
    //receiver
    fun getUsername(): String?{
        return username
    }
    fun setUsername(username: String){
        this.username = username
    }
    //message
    fun getSearch(): String?{
        return search
    }
    fun setSearch(search: String){
        this.search = search
    }
    //isSeen
    fun getStatus(): String?{
        return status
    }
    fun setStatus(status: String){
        this.status = status
    }
    //url
    fun getProfile(): String?{
        return profile
    }
    fun setProfile(profile: String){
        this.profile = profile
    }
    //messageId
    fun getCover(): String?{
        return cover
    }
    fun setCover(cover: String){
        this.cover = cover
    }
    //
    fun getFacebook(): String?{
        return facebook
    }
    fun setFacebook(facebook: String){
        this.facebook = facebook
    }
    //
    fun getInstagram(): String?{
        return instagram
    }
    fun setInstagram(instagram: String){
        this.instagram = instagram
    }
    //
    fun getWebsite(): String?{
        return website
    }
    fun setWebsite(website: String){
        this.website = website
    }
    
}
