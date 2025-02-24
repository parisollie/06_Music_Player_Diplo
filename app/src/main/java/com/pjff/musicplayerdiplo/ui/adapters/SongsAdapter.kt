package com.pjff.musicplayerdiplo.ui.adapters

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pjff.musicplayerdiplo.R
import com.pjff.musicplayerdiplo.data.local.model.MusicFileDto
import com.pjff.musicplayerdiplo.databinding.MusicItemBinding


class SongsAdapter(
    private val songs: List<MusicFileDto>,
    private val onSongClicked: (position: Int) -> Unit
): RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    class ViewHolder(private val binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root){
        val ivSongImage = binding.ivSongImage

        fun bind(song: MusicFileDto){
            binding.tvSongName.text = song.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = songs.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        holder.bind(song)

        var image: ByteArray? = null

        try{
            image = getAlbumArt(song.path)
        }catch (e: Exception){
            e.printStackTrace()
        }

        //Sino tengo imagen la importo
        if(image != null){
            Glide.with(holder.itemView.context).asBitmap()
                .load(image)
                .into(holder.ivSongImage)
        }else{
            holder.ivSongImage.setImageResource(R.drawable.ic_song)
        }

        holder.itemView.setOnClickListener {
            onSongClicked(position)
        }

    }

    fun getAlbumArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        retriever.close()
        return art
    }

}