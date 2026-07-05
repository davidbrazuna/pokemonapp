package com.davidbrazuna.pokemonapp.util

// Wraps a value that should be handled at most once. LiveData re-delivers its
// last value to every new observer (including after a rotation), which makes
// one-off things like error messages reappear as if they had just happened.
// Wrapping the value in an Event and marking it handled on first read fixes that.
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    // Returns the content if it hasn't been handled yet, null otherwise.
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}
