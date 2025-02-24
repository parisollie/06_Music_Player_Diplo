package com.pjff.musicplayerdiplo.ui.fragments

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.pjff.musicplayerdiplo.R
import com.pjff.musicplayerdiplo.data.local.model.MusicFileDto
import com.pjff.musicplayerdiplo.databinding.FragmentMusicPlayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/*class MusicPlayer : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_play, container, false)
    }

}*/


class MusicPlayer : Fragment(), MediaPlayer.OnCompletionListener, View.OnClickListener {

    private var _binding: FragmentMusicPlayBinding? = null
    private val binding get() = _binding

    //21-oct-2020
    //Instancia hacia nuestro view model
    private val musicListViewModel: MusicListViewModel by viewModels()

    private lateinit var mediaPlayer: MediaPlayer
    private var position = -1
    private var song: MusicFileDto? = null
    private var playing = false
    private var isShuffleOn = false
    private var isRepeatOn = false
    private var audioList = ArrayList<MusicFileDto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMusicPlayBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args : MusicPlayerArgs by navArgs()
        //Igualamos elpostion anuestro argumento
        position = args.position

        //Ya me trae todos los archivos de musica
        musicListViewModel.getAllAudio(requireContext())

        //nuestros botones
        binding?.fabPlayPause?.setOnClickListener(this)
        binding?.ivNext?.setOnClickListener(this)
        binding?.ivPrev?.setOnClickListener(this)
        binding?.ivShuffle?.setOnClickListener(this)
        binding?.ivRepeat?.setOnClickListener(this)
        //Y ya los observo
        musicListViewModel.musicFiles.observe(viewLifecycleOwner){ songs ->
            //Obtenemos el listado de canciones disponibles
            audioList = songs as ArrayList<MusicFileDto>
            //Ya tenemos la cancion a reproducir
            song = songs[position]

            //Ponemos de inicio el botón de reproducción en pausa
            binding?.fabPlayPause?.setImageResource(R.drawable.ic_pause)

            prepareSong(song, true)

            //Para mover la seekbar por parte del usuario

            binding?.sbSong?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        mediaPlayer.seekTo(progress*1000) //lo regresamos a milisegundos
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mediaPlayer.release()
    }

    //Para saber si se esta reproducineod
    override fun onStart() {
        super.onStart()
        if(playing)
            mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        playing = mediaPlayer.isPlaying
        mediaPlayer.pause()
    }

    //Funcion para una cancion ,para los minutos y segundoss que lleva la cancion
    fun formattedTime(currentPosition: Int): String{
        var totalOut = ""
        var totalNew = ""
        val seconds  = "${currentPosition%60}"
        val minutes  = "${currentPosition/60}"
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"

        //122 ->  2:02       135 seg ->  2:15 min

        return if(seconds.length == 1) totalNew
        else totalOut
    }

    //Traemos los metada de nuestra cancion
    private fun setMetaData(song: MusicFileDto){
        //Por si no trae una portada
        var cover: ByteArray? = null

        try{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(song.path)
            cover = retriever.embeddedPicture
            retriever.release()
        }catch (e: Exception){
            e.printStackTrace()
        }

        //Si si obtuvo la imagen
        if(cover!=null){
            binding?.ivCover?.let { ivCover ->
                Glide.with(requireContext()).asBitmap()
                    .load(cover)
                    .into(ivCover)
            }
        }else{
            binding?.ivCover?.setImageResource(R.drawable.cover)
        }

        //Le ponemos el valor de la duracion de la cancion en la esquina
        val duration = song.duration.toInt()/1000
        binding?.tvDuration?.text = formattedTime(duration)

    }

    //Cuando la cancion termine por reproducirse
    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        //Se va a ejecutar cuando se termine de reproducir una canción
        mediaPlayer?.release()

        if(isShuffleOn && !isRepeatOn){
            position = (0 until audioList.size).random()
            //si ninguno de los dos esta encendido
        }else if(!isShuffleOn && !isRepeatOn){
            position = (position+1) % audioList.size
        }

        song = audioList[position]

        prepareSong(song, true)
        binding?.fabPlayPause?.setImageResource(R.drawable.ic_pause)

    }

    //el start nos dice , la reproduzco o me espero
    private fun prepareSong(song: MusicFileDto?, start: Boolean){
        song?.let{ song ->
            //establezco mis text view
            binding?.tvSongName?.text = song.title
            binding?.tvArtistName?.text = song.artist

            //Instanciamos el media player , le pasamos el path de nuestros archivos en el path
            mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(song.path))

            mediaPlayer.setOnCompletionListener(this)

            //Le ponemos la seek bar es de la duracion que dure la cancion
            binding?.sbSong?.max = mediaPlayer.duration/1000

            //Aqui ya le mandamos nuestra cancion
            setMetaData(song)

            //Actualizamos la seekbar cada segundo
            //le ponemos una ecorutina
            lifecycleScope.launch(Dispatchers.Main){
                //is active para saber si la ecorutina esta activa
                while(isActive){
                    //pasamos la posición actual a segundos
                    val currentPosition = mediaPlayer.currentPosition/1000
                    //en que segundo esta la progress bar
                    binding?.sbSong?.progress = currentPosition
                    //el tv time es el que va cambiando con el tiempo
                    binding?.tvTime?.text = formattedTime(currentPosition)
                    delay(1000)
                }
            }

            //si viene en true, empiezo a reproduccir la cancion
            if(start){
                mediaPlayer.start()
                playing = mediaPlayer.isPlaying
            }

        }
    }

    //Click a los botones
    override fun onClick(view: View?) {
        //Para manejar todos los clicks a los botones
        when(view){
            binding?.fabPlayPause -> {
                //si se esta reproducciendo le ponemos pausa al boton
                if(mediaPlayer.isPlaying){
                    mediaPlayer.pause()
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_play)
                }else{
                    mediaPlayer.start()
                    //me cambia el boton a pausa
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_pause)
                }
            }
            binding?.ivNext -> {
                //Indices
                //    1  2
                //[1, 2, 3]    size = 3
                //position = (position+1) % audioList.size

                //Implementando el shuffle y repeat
                //!isRepeatOn , el otro esta apagado
                if(isShuffleOn && !isRepeatOn){
                    position = (0 until audioList.size).random()
                }else if(!isShuffleOn && !isRepeatOn){
                    position = (position+1) % audioList.size
                }

                song = audioList[position]

                if(mediaPlayer.isPlaying){
                    mediaPlayer.release()
                    prepareSong(song, true)
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_pause)
                }else{
                    //Preparo la cancion sig ,pero no la reproduzco
                    mediaPlayer.release()
                    prepareSong(song, false)
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_play)
                }
            }
            //cancion previa
            binding?.ivPrev -> {
                //position = (position-1+audioList.size) % audioList.size

                if(isShuffleOn && !isRepeatOn){
                    position = (0 until audioList.size).random()
                }else if(!isShuffleOn && !isRepeatOn){
                    position = (position-1+audioList.size) % audioList.size
                }

                song = audioList[position]

                if(mediaPlayer.isPlaying){
                    mediaPlayer.release()
                    prepareSong(song, true)
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_pause)
                }else{
                    mediaPlayer.release()
                    prepareSong(song, false)
                    binding?.fabPlayPause?.setImageResource(R.drawable.ic_play)
                }
            }
            binding?.ivShuffle -> {
                //Cambiamos el color del shuffle ,de las felchitas que se reproducen
                if(isShuffleOn){
                    isShuffleOn = false
                    binding?.ivShuffle?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }else{
                    isShuffleOn = true
                    isRepeatOn = false
                    binding?.ivShuffle?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.onColor))
                    binding?.ivRepeat?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            binding?.ivRepeat -> {
                if(isRepeatOn){
                    isRepeatOn = false
                    binding?.ivRepeat?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }else{
                    isRepeatOn = true
                    isShuffleOn = false
                    binding?.ivRepeat?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.onColor))
                    binding?.ivShuffle?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }
    }

}


