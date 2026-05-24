package com.example.cordiprueba.Inicio.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Search

@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME1() {
    val navController = rememberNavController()
    PRACTICA_ELECTRONICAS(navController = navController)
}

@Composable
fun PRACTICA_ELECTRONICAS(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var listaPracticas by remember { mutableStateOf<List<PracticaResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.obtenerPracticas()
            }
            if (response.isSuccessful) {
                val todasLasPracticas = response.body() ?: emptyList()
                listaPracticas = todasLasPracticas.filter { it.tipo == "electronica" }
            } else {
                println("Error al obtener datos: ${response.code()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error de red: No se pudo conectar al servidor")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
    ) {
        Column {
            TOP_BAR()

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp).weight(1f)
            ) {
                item { FILTER_SECTION() }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("Cargando prácticas de la base de datos...", color = Color.Gray)
                        }
                    }
                }
                else if (listaPracticas.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No hay prácticas de electrónica guardadas aún.", color = Color.Gray)
                        }
                    }
                }
                else {
                    items(listaPracticas) { practica ->
                        PRACTICE_ITEM(
                            practica = practica,
                            onClick = {
                                navController.navigate("presentar_electronica/${practica.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                            .clickable { navController.navigate("crear") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 24.sp)
                    }
                }
                BOTTOM_BAR_ELECTRONICA(navController)
            }
        }
    }
}

@Composable
fun PRACTICE_ITEM(
    practica: PracticaResponse,
    onClick: () -> Unit
) {
    val tieneImagen = !practica.archivos.isNullOrEmpty()
    val rutaImagen = if (tieneImagen) practica.archivos!!.first().ruta else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF132030), RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = practica.nombre.uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = practica.descripcion,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 2
            )

            if (tieneImagen) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("🖼️ Imagen detectada", color = Color(0xFF2EC4B6), fontSize = 10.sp)
            }

        }
    }
}

@Composable
fun TOP_BAR() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Prácticas de", color = Color.Gray, fontSize = 14.sp)
            Text("Electrónica", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FILTER_SECTION() {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Text("Todo", color = Color.White, modifier = Modifier.background(Color.Gray.copy(0.3f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun BOTTOM_BAR_ELECTRONICA(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f)).padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BOTTOM_ITEM_E("inicio", false) { navController.navigate("home") }
        BOTTOM_ITEM_E("electronica", true) { navController.navigate("electronica") }
        BOTTOM_ITEM_E("computo", false) { navController.navigate("computo") }
        BOTTOM_ITEM_E("editar", false) { navController.navigate("gestion") }
    }
}

@Composable
fun BOTTOM_ITEM_E(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(24.dp).background(if (selected) Color.White else Color.Gray, CircleShape))
        Text(text, color = if (selected) Color.White else Color.Gray, fontSize = 12.sp)
    }
}