package com.pjff.musicplayerdiplo.ui

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.pjff.musicplayerdiplo.R

class MainActivity : AppCompatActivity() {

    //Paso 1.12,Lo ponemos para tener el alcance global
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Paso 1.13
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)

        //Paso 1.14,Si se pudo obtener el NavHOst sin problema
        if(navHostFragment!=null){
            navController = navHostFragment.findNavController()
            //Paso 1.15 ,Si me meto en los fragments ,pondre una flecha para que me dirija atras
            NavigationUI.setupActionBarWithNavController(this, navController)
            //Paso 1.17,Le ponemos el color que defininos en el los themes
            supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.actionBarColor)))
        }
    }
    //Pasi 1.16,Navegacion hacia arriba
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }
}