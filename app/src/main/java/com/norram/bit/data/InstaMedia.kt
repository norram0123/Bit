package com.norram.bit

data class InstaMedia(
    val url: String,
    var permalink: String,
    val type: String,
    val childrenUrls: ArrayList<String>,
    var flag: Boolean
)