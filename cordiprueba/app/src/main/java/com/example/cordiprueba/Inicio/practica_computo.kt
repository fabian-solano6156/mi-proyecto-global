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
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import com.example.cordiprueba.api.RetrofitInstance
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME11() {
    val navController = rememberNavController()
    PRACTICA_COMPUTO(navController = navController)
}
@Composable
fun PRACTICA_COMPUTO(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var listaPracticas by remember { mutableStateOf<List<PracticaResponse>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitInstance.api.obtenerPracticas()
                if (response.isSuccessful) {
                    listaPracticas = response.body()?.filter { it.tipo == "computo" } ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cargando = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
    ) {
        Column {
            TOP_BAR_COMPUTO()

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2EC4B6))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    item { FILTER_SECTION_COMPUTO() }

                    item { PRACTICE_LIST_COMPUTO(listaPracticas, navController) }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
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
                BOTTOM_BAR_COMPUTO(navController)
            }
        }
    }
}

@Composable
fun PRACTICE_LIST_COMPUTO(practicas: List<PracticaResponse>, navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (practicas.isEmpty()) {
            Text(
                "No hay prácticas guardadas",
                color = Color.Gray,
                modifier = Modifier.padding(top = 20.dp)
            )
        } else {
            practicas.forEach { practica ->
                PRACTICE_ITEM_REAL(practica, navController)
            }
        }
    }
}

@Composable
fun PRACTICE_ITEM_REAL(practica: PracticaResponse, navController: NavHostController) {
    val rutaImagen = practica.archivos?.firstOrNull()?.ruta

    val urlImagen = rutaImagen?.let { "http://192.168.5.236:8000/$it" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF132030))
            .clickable {
                navController.navigate("presentar_computo/${practica.id}")
            }
    ) {
        if (urlImagen != null) {
            AsyncImage(
                model = urlImagen,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = practica.nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = practica.descripcion,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2EC4B6).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("<>", color = Color(0xFF2EC4B6), fontWeight = FontWeight.Bold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${practica.detalles_computo?.pasos?.size ?: 0} pasos",
                    color = Color(0xFF2EC4B6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cómputo",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}


@Composable
fun TOP_BAR_COMPUTO() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Prácticas de", color = Color.Gray, fontSize = 14.sp)
            Text("Cómputo", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FILTER_SECTION_COMPUTO() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FILTER_CHIP_COMPUTO("Todas", true)
    }
}

@Composable
fun FILTER_CHIP_COMPUTO(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF2EC4B6) else Color(0xFF132030),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.White else Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun BOTTOM_BAR_COMPUTO(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BOTTOM_ITEM_C("inicio", false) { navController.navigate("home") }
        BOTTOM_ITEM_C("electronica", false) { navController.navigate("electronica") }
        BOTTOM_ITEM_C("computo", true) { navController.navigate("computo") }
        BOTTOM_ITEM_E("editar", false) { navController.navigate("gestion") }
    }
}

@Composable
fun BOTTOM_ITEM_C(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (selected) Color.White else Color.Gray, CircleShape)
        )
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            color = if (selected) Color.White else Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}