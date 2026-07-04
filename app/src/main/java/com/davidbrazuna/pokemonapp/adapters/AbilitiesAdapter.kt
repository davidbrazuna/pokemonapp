package com.davidbrazuna.pokemonapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davidbrazuna.pokemonapp.databinding.AbilityItemBinding
import com.davidbrazuna.pokemonapp.model.Ability

class AbilitiesAdapter : RecyclerView.Adapter<AbilitiesAdapter.AbilitiesViewHolder>() {

    inner class AbilitiesViewHolder(val binding: AbilityItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var abilitiesList = listOf<Ability>()

    fun setAbilitiesList(abilitiesList: List<Ability>) {
        this.abilitiesList = abilitiesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbilitiesViewHolder {
        return AbilitiesViewHolder(
            AbilityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = abilitiesList.size

    override fun onBindViewHolder(holder: AbilitiesViewHolder, position: Int) {
        holder.binding.abilityName.text = abilitiesList[position].ability.name
    }
}
