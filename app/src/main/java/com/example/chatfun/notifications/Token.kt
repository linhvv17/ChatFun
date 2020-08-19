package com.example.chatfun.notifications

class Token {
    private var token: String = ""
    constructor()
    constructor(token: String){
        this.token = token
    }
    //token
    fun getToken(): String?{
        return token
    }
    fun setToken(token: String){
        this.token = token!!
    }
}