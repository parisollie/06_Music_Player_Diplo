package com.pjff.musicplayerdiplo.ui.providers


interface PermissionExplanationProvider {
    fun getPermissionText(): String
    //Si el usuario prohibio el permiso
    fun getExplanation(isPermanentlyDeclined: Boolean): String
}