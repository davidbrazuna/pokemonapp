package com.example.pokemonapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemonapp.databinding.AbilityItemBinding
import com.example.pokemonapp.pojo.Ability
import com.example.pokemonapp.pojo.AbilityItem

class AbilitiesAdapter : RecyclerView.Adapter<AbilitiesAdapter.AbilitiesViewHolder> (){

    inner class AbilitiesViewHolder(val binding: AbilityItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var abilitiesList = ArrayList<Ability>()

    fun setAbilitiesList(abilitiesList: List<Ability>){
        this.abilitiesList = abilitiesList as ArrayList<Ability>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbilitiesViewHolder {
       return AbilitiesViewHolder(
           AbilityItemBinding.inflate(LayoutInflater.from(parent.context))
       )
    }

    override fun getItemCount() = abilitiesList.size

    override fun onBindViewHolder(holder: AbilitiesViewHolder, position: Int) {
        holder.binding.abilityName.text = abilitiesList[position].ability.name
    }

}