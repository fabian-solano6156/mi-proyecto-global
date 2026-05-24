package com.example.cordiprueba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.cordiprueba.ui.theme.CordipruebaTheme
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.*
import com.example.cordiprueba.Inicio.composables.HOME_SCREEN
import com.example.cordiprueba.Inicio.composables.PRACTICA_ELECTRONICAS
import com.example.cordiprueba.Inicio.composables.PRACTICA_COMPUTO
import com.example.cordiprueba.Inicio.composables.CREAR_NUEVO_SCREEN
import com.example.cordiprueba.Inicio.composables.CREANDO_ELECTRONICA_SCREEN
import com.example.cordiprueba.Inicio.composables.CREANDO_COMPUTO_SCREEN
import com.example.cordiprueba.Inicio.composables.PRESENTAR_ELECTRONICA_SCREEN
import com.example.cordiprueba.Inicio.composables.PRESENTAR_COMPUTO_SCREEN
import com.example.cordiprueba.Inicio.composables.GESTION_PRACTICAS_SCREEN
import com.example.cordiprueba.Inicio.composables.EDITAR_ELECTRONICA_SCREEN
import com.example.cordiprueba.Inicio.composables.EDITAR_COMPUTO_SCREEN
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CordipruebaTheme {

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("home") {
                            HOME_SCREEN(navController)
                        }

                        composable("electronica") {
                            PRACTICA_ELECTRONICAS(navController)
                        }

                        composable("computo") {
                            PRACTICA_COMPUTO(navController)
                        }

                        composable("crear") {
                            CREAR_NUEVO_SCREEN(navController)
                        }
                        composable("creando_electronica") {
                            CREANDO_ELECTRONICA_SCREEN(navController)
                        }
                        composable("creando_computo") {
                            CREANDO_COMPUTO_SCREEN(navController)
                        }
                        composable("presentar_electronica/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                            PRESENTAR_ELECTRONICA_SCREEN(navController = navController, practicaId = id)
                        }
                        composable("presentar_computo/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                            PRESENTAR_COMPUTO_SCREEN(navController = navController, practicaId = id)
                        }
                        composable("gestion") {
                            GESTION_PRACTICAS_SCREEN(navController)
                        }
                        composable("editar_electronica/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                            EDITAR_ELECTRONICA_SCREEN(navController = navController, practicaId = id)
                        }
                        composable("editar_computo/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                            EDITAR_COMPUTO_SCREEN(navController = navController, practicaId = id)
                        }
                    }
                }
            }
        }
    }
}