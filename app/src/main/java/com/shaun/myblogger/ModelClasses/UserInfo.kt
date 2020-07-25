package com.shaun.myblogger.ModelClasses

class UserInfo{
    private var id=""
    private var name=""
    private  var username=""
    private var postIDs:ArrayList<String>?=null
    private var Pphoto = ""
    private var description = ""
    constructor()
    constructor(
        id: String,
        name: String,
        username: String,
        postIDs: ArrayList<String>?,
        Pphoto: String,
        description: String
    ) {
        this.id = id
        this.name = name
        this.username = username
        if (postIDs != null) {
            this.postIDs = postIDs
        }
        this.Pphoto = Pphoto
        this.description = description
    }


    override fun toString(): String {
        return """
            uid = $id
            name =$name
            username = $username
            profile Photo = $Pphoto
            Bio = $description
        """.trimIndent()
    }

    fun getid() = id
    fun setid(uid: String) {
        this.id = uid
    }

    fun getname():String{
        return name
    }

    fun setname(name: String) {
        this.name = name
    }

    fun getusername() = username
    fun setusernam(username: String) {
        this.username = username
    }

    fun getPphoto() = Pphoto
    fun setPphoto(profile: String) {
        this.Pphoto = profile
    }

    fun getpostIDs() = postIDs
    fun setpostIDs(posts: ArrayList<String>?) {
        if (posts != null) {
            this.postIDs = posts
        }
    }


    fun removePost(postId: String) {
        this.postIDs!!.remove(postId)
    }

    fun getdescription() = description
    fun setdescription(Bio: String) {
        this
            .description = Bio
    }

}
