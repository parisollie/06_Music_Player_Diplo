package com.pjff.musicplayerdiplo.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pjff.musicplayerdiplo.R
import com.pjff.musicplayerdiplo.databinding.FragmentMusicListBinding
import com.pjff.musicplayerdiplo.ui.adapters.SongsAdapter
import com.pjff.musicplayerdiplo.ui.providers.PermissionExplanationProvider
import com.pjff.musicplayerdiplo.ui.providers.ReadAudioPermissionExplanationProvider
import com.pjff.musicplayerdiplo.ui.providers.ReadPermissionExplanationProvider
import com.pjff.musicplayerdiplo.ui.providers.WritePermissionExplanationProvider

class MusicList : Fragment() {

    //Paso 1.1,Hacemos binding
    private var _binding: FragmentMusicListBinding? = null
    //Paso 1.2,_binding, lo hacemos mutable
    private val binding get() = _binding

    //Paso 1.27,Instanciamos nuestro view model
    private val musicListViewModel: MusicListViewModel by viewModels()

    //Paso 1.28,Nuestras variables mutables que se inicializan en falso.
    private var readMediaAudioGranted = false
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private var permissionsToRequest = ArrayList<String>()

    //Paso 1.30,Enfoque con google
    private val permissionLauncher = registerForActivityResult(
        //Le pasamos el contrato que pide google y le mando el que quiero de múltiples permisions
        ActivityResultContracts.RequestMultiplePermissions()
        //le mandamos una lambda , para ver si tiene el permiso o no.
    ){ permissions: Map<String, Boolean> ->
        //all es una función de extensión sobre el mapa que me regresa true si todos están en true
        val allGranted = permissions.all{
            //revisa si todos los elementos cumplen , me regresa un true
            it.value
        }
        if(allGranted){
            //Paso 1.32,Todos los permisos están concedidos
            actionPermissionsGranted()
        }else{
            //Acá no los tengo, se almacena en el permission 
            permissionsToRequest.forEach{ permission ->
                musicListViewModel.onPermissionResult(
                    //le pasamos el permisison y si esta garantizado
                    permission = permission,
                    isGranted = permissions[permission] == true
                )
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //Paso 1.4 ,Lo instanciamos
        _binding = FragmentMusicListBinding.inflate(inflater, container, false)
        //Regrese la version no mutable.
        return binding?.root
    }

    //Cuando el usuario ya ve la vista.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Paso 1.43
        musicListViewModel.permissionsToRequest.observe(viewLifecycleOwner){ queue ->
            queue.reversed().forEach(){ permission ->
                //Paso 1.44,Reviso la cola de los permisos
                showPermissionExplanationDialog(
                    when(permission){
                        Manifest.permission.READ_EXTERNAL_STORAGE -> {
                            ReadPermissionExplanationProvider()
                        }
                        Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            WritePermissionExplanationProvider()
                        }
                        Manifest.permission.READ_MEDIA_AUDIO -> {
                            ReadAudioPermissionExplanationProvider()
                        }
                        else -> return@forEach
                    },
                    //Que permiso quieres
                    !shouldShowRequestPermissionRationale(permission),
                    {
                        musicListViewModel.dismissDialog()
                    },
                    {
                        musicListViewModel.dismissDialog()
                        updateOrRequestPermissions()
                    },
                    {
                        startActivity(
                            Intent(
                                //Le paso el paquete de mi aplicacion
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", requireContext().packageName, null)
                            )
                        )
                    }
                )
            }
        }
    }

    //Paso 1.3
    override fun onDestroy() {
        super.onDestroy()
        //para evitar memory liks
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        //Paso 1.42
        updateOrRequestPermissions()
    }

    //Paso 1.29,Funcion para saber si los permisos se revocaron o no.
    private fun updateOrRequestPermissions(){
        /*Revisamos los permisos, de las sdk si son correctas
        Revisamos si el permiso es menor a 33*/
        val hasReadPermission = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
        //para el almacenamiento externo
        val hasWritePermission = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
        //android trece hacia arriba.
        val hasMediaAudioPermission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }

        //Variable para nuestro minsdk , que se coloca en true
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        readMediaAudioGranted = hasMediaAudioPermission

        /*
        Para api level del 23 al 28
        READ_EXTERNAL_STORAGE y WRITE_EXTERNAL_STORAGE

        Para api level del 29 al 32
        READ_EXTERNAL_STORAGE

        Para el api level 33 en adelante
        READ_MEDIA_AUDIO
        */
        if(!readPermissionGranted)
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if(!writePermissionGranted)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(!readMediaAudioGranted)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)

        if(permissionsToRequest.isNotEmpty()){
            /*Paso 1.33,Hay permisos que pedir
            por lo tanto, pedimos los permisos, lo convertimos para que funcione*/
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }else{
            //Todos los permisos se han concedido
            actionPermissionsGranted()
            Toast.makeText(requireContext(), "Todos los permisos se han concedido",Toast.LENGTH_SHORT).show()
        }
    }

    //Paso 1.31,Función a ejecutar si tengo los permisos
    private fun actionPermissionsGranted(){
        //Toast.makeText(requireContext(), "Todos los permisos se han concedido", Toast.LENGTH_SHORT).show()
        //Paso 2.1,le mandamos la funcion del audio.
        musicListViewModel.getAllAudio(requireContext())

        musicListViewModel.musicFiles.observe(viewLifecycleOwner){ songs ->
            if(songs.isNotEmpty()){
                val songsAdapter = SongsAdapter(songs){ position ->
                    //Procesamos el click a la canción
                    //el findNavcontroller, viene desde nuestro activity
                    findNavController().navigate(MusicListDirections.actionMusicListToMusicPlayer(
                        //Le paso la posicon hacia mi otro fragment
                        position
                    ))
                }
                binding?.rvSongs?.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                //Paso 2.9
                binding?.rvSongs?.adapter = songsAdapter
            }
        }
    }

    //Paso 1.33,función para mostrar un dialógo explicando por qué necesito el permiso
    private fun showPermissionExplanationDialog(
        //Paso 1.38,Cada vez que lo mande a llamar mandare los permisos
        permissionExplanationProvider: PermissionExplanationProvider,
        isPermanentlyDeclined: Boolean,
        //Paso 1.39,le pasamos nuestras lambdas
        onDismiss: () -> Unit,
        onOkClick: () -> Unit,
        onGoToAppSettingsClick: () -> Unit
    ){
        AlertDialog.Builder(requireContext())
            /*Paso 1.40, le ponemos la abstraccion, permissionExplanationProvider.getPermissionText()
            para saber que permiso tendrá*/
            .setTitle(permissionExplanationProvider.getPermissionText())
            .setMessage(permissionExplanationProvider.getExplanation(isPermanentlyDeclined))
            .setPositiveButton(if(isPermanentlyDeclined) "Configuración" else "Aceptar"){ dialog, _ ->
                //Paso 1.41,Si ya me lo declino le mando configuración
                if(isPermanentlyDeclined) onGoToAppSettingsClick()
                else onOkClick()
            }
            .setOnDismissListener{ _ ->
                //para que se deje de mostrar
                onDismiss()
            }
            //Para que se muestre.
            .show()
    }
}