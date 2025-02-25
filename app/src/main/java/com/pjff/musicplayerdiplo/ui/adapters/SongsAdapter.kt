package com.pjff.musicplayerdiplo.ui.adapters

import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pjff.musicplayerdiplo.R
import com.pjff.musicplayerdiplo.data.local.model.MusicFileDto
import com.pjff.musicplayerdiplo.databinding.MusicItemBinding


//Paso 2.2, pongo mi adaptador
class SongsAdapter(
    //Le pongo mis listado de DTO
    private val songs: List<MusicFileDto>,
    private val onSongClicked: (position: Int) -> Unit
): RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    //Paso 2.3
    class ViewHolder(private val binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root){
        //Paso 2.5, pintamos el album art
        val ivSongImage = binding.ivSongImage

        //Paso 2.6
        fun bind(song: MusicFileDto){
            binding.tvSongName.text = song.title
        }
    }

    //Paso 2.7
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // le pasamos el bidnign instanciado
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = songs.size


    //Paso 2.8
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Me llega el binding  generado
        val song = songs[position]
        holder.bind(song)
        var image: ByteArray? = null

        try{
            // le paso mi imagen
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
            //Mando a llamar mi lambda
            onSongClicked(position)
        }
    }

    //Paso 2.4, para obtener la imagen
    fun getAlbumArt(uri: String): ByteArray?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art: ByteArray? = retriever.embeddedPicture
        //Lo cierro para no tener memory licks
        retriever.close()
        return art
    }

}