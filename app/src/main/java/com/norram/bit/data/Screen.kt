package com.norram.bit

class Screen {
    var width: Int = 1
    var isMeasured: Boolean = false // measure the screen width only once

    companion object {
        private var instance : Screen? = null

        fun getInstance(): Screen {
            if(instance == null) instance = Screen()
            return instance!!
        }
    }
}