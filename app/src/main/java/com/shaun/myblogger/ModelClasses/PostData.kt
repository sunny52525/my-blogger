package com.shaun.myblogger.ModelClasses

import java.io.Serializable

class PostData : Serializable {
    private var id = ""
    private var content = ""
    private var like_count: Long = 0
    private var nameOP = ""
    private var time = ""
    private var title = ""
    private var username = ""
    private var userId = ""
    private var postCover = ""

    constructor()
    constructor(
        id: String,
        content: String,
        like_count: Long,
        nameOP: String,
        time: String,
        title: String,
        username: String,
        userId: String,
        photosInpost: ArrayList<String>
    ) {
        this.id = id
        this.content = content
        this.like_count = like_count
        this.nameOP = nameOP
        this.time = time
        this.title = title
        this.username = username
        this.userId = userId
        this.postCover = postCover
    }

    fun getid() = id
    fun getcontent() = content
    fun getlike_count() = like_count
    fun getnameOp() = nameOP
    fun gettime() = time
    fun gettitle() = title
    fun getusername() = username
    fun getuserId() = userId
    fun getpostCover() = postCover

    fun setid(id: String) {
        this.id = id
    }

    fun setcontent(content: String) {
        this.content = content
    }

    fun setlike_count(like_count: Long) {
        this.like_count = like_count
    }

    fun setnameOp(nameOP: String) {
        this.nameOP = nameOP
    }

    fun settime(time: String) {
        this.time = time
    }

    fun settitle(title: String) {
        this.title = title
    }

    fun setusername(username: String) {
        this.username = username
    }

    fun setuserId(userId: String) {
        this.userId = userId
    }

    fun setpostCover(postCover: String) {
        this.postCover = postCover
    }

    override fun toString(): String {
        return """
            id = $id
            content =$content
            like count =$like_count
            name =$nameOP
            username =$username
            time = $time
            title =$title
            postCover =$postCover
        """.trimIndent()
    }
}