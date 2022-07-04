package com.norram.bit

data class InstaMedia(
    val url: String,
    val type: String,
    val childrenUrls: ArrayList<String>,
    var flag: Boolean
)