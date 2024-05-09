package com.example.pokemonapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokemonapp.databinding.PokemonListItemBinding
import com.example.pokemonapp.viewmodel.PokemonWithImage


class PokemonListAdapter : RecyclerView.Adapter<PokemonListAdapter.PokemonViewHolder> (){

    private var pokemonList = ArrayList<PokemonWithImage>()
    var onItemClick : ((PokemonWithImage) -> Unit)? = null

    fun setPokemonList(pokemonList: List<PokemonWithImage>) {
        this.pokemonList = pokemonList as ArrayList<PokemonWithImage>
        notifyDataSetChanged()
    }

    inner class PokemonViewHolder(val binding: PokemonListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        return PokemonViewHolder(
            PokemonListItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount() = pokemonList.size

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(pokemonList[position].imageUrl)
            .into(holder.binding.imgPokemon)

        holder.binding.pokemonName.text = pokemonList[position].name

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(pokemonList[position])
        }

    }
}