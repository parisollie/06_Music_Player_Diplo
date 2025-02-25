package com.pjff.musicplayerdiplo.ui.providers

//Paso 1.34, funcion generica para los permisos
interface PermissionExplanationProvider {
    fun getPermissionText(): String
    //Si el usuario prohibio el permiso
    fun getExplanation(isPermanentlyDeclined: Boolean): String
}