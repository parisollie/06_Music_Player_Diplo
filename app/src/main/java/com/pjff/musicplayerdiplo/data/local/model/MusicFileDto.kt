package com.pjff.musicplayerdiplo.data.local.model

//Paso 1.19, ponemos nuestra dataclass
data class MusicFileDto(
    //Propiedades de inicio
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String
)
