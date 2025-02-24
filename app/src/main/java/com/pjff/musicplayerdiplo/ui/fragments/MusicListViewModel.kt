package com.pjff.musicplayerdiplo.ui.fragments

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pjff.musicplayerdiplo.data.local.model.MusicFileDto

class MusicListViewModel: ViewModel() {
    //Cola para los strings de los permisos a solicitar
    private val permissionsToRequestQueue = mutableListOf<String>()

    //Livedatas
    private val _permissionsToRequest = MutableLiveData<List<String>>()
    //privado guian abajo
    val permissionsToRequest = _permissionsToRequest

    private val _musicFiles = MutableLiveData<List<MusicFileDto>>()
    //Version publica
    val musicFiles = _musicFiles

    //Función para quitar los permisos ya concedidos de la cola
    fun dismissDialog(){
        if(permissionsToRequestQueue.isNotEmpty())
            permissionsToRequestQueue.removeFirst()
    }

    //Función para manejar el resultado del permiso, si el usuario lo acepta o no
    fun onPermissionResult(
        permission: String,
        //booleano para saber si lo acepto o no
        isGranted: Boolean
    ){
        if(!isGranted && !permissionsToRequestQueue.contains(permission)) {
            permissionsToRequestQueue.add(permission)
            //
            _permissionsToRequest.postValue(permissionsToRequestQueue)
        }
    }

    //Funcion para tener todo el audio
    fun getAllAudio(context: Context){
        val tempAudioList = ArrayList<MusicFileDto>()

        //Checar todos los archivos de audio que tenga
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        //Puedo encontrar alguna cancion
        if(cursor!=null){
            while(cursor.moveToNext()){
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                val path = cursor.getString(3)
                val artist = cursor.getString(4)

                val musicFile = MusicFileDto(path, title, artist, album, duration?:"0")
                Log.d("MUSICA", "Path: $path - Album: $album")
                tempAudioList.add(musicFile)
            }
            cursor.close()
        }
        _musicFiles.postValue(tempAudioList)
    }
}