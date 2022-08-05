package com.rhezarijaya.stories.util

class SingleEvent<out T>(private val data: T) {
    var hasBeenDone = false
        private set

    fun getData(): T? {
        return if (hasBeenDone) {
            null
        } else {
            hasBeenDone = true
            return data
        }
    }

    fun peekData(): T = data
}