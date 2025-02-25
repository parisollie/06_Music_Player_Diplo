package com.pjff.musicplayerdiplo.ui.providers

//Paso 1.37
class WritePermissionExplanationProvider: PermissionExplanationProvider {
    override fun getPermissionText(): String = "Permiso de escritura"

    override fun getExplanation(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined)
            "El permiso se ha negado o sigue negado permanentemente. Para usar esta función, habilite el permiso en la configuración de la aplicación"
        else
            "Se requiere el permiso de escritura para acceder a los archivos de audio del dispositivo"
    }
}