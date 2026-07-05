package com.davidbrazuna.pokemonapp.util

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData

// Shared across MainActivity and PokemonDetailsScreen to avoid duplicating the
// same observe-and-show-Toast block in both places.
fun AppCompatActivity.observeErrorToast(errorLiveData: LiveData<Event<String>>) {
    errorLiveData.observe(this) { event ->
        event.getContentIfNotHandled()?.let { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
