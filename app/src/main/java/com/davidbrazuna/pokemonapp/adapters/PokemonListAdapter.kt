package com.davidbrazuna.pokemonapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davidbrazuna.pokemonapp.databinding.PokemonListItemBinding
import com.davidbrazuna.pokemonapp.model.PokemonWithImage

class PokemonListAdapter : RecyclerView.Adapter<PokemonListAdapter.PokemonViewHolder>() {

    private var pokemonList = listOf<PokemonWithImage>()
    var onItemClick: ((PokemonWithImage) -> Unit)? = null

    fun setPokemonList(pokemonList: List<PokemonWithImage>) {
        this.pokemonList = pokemonList
        notifyDataSetChanged()
    }

    inner class PokemonViewHolder(val binding: PokemonListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        return PokemonViewHolder(
            PokemonListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = pokemonList.size

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val item = pokemonList[position]
        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .into(holder.binding.imgPokemon)

        holder.binding.pokemonName.text = item.name

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
}
