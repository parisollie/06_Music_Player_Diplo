package com.pjff.musicplayerdiplo.ui.providers




class ReadAudioPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso para leer los archivos de audio"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la aplicación"
        else
            "Se requiere el permiso para acceder a los archivos de audio del dispositivo"
    }
}