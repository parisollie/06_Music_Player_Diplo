package com.pjff.musicplayerdiplo.ui.providers

//Paso 1.36
class ReadPermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de lectura"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la aplicación"
        else
            "Se requiere el permiso de lectura para acceder a los archivos de audio del dispositivo"
    }
}