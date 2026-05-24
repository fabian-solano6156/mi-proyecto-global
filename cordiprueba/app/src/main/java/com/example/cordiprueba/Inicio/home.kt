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
import androidx.compose.ui.draw.clip
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.cordiprueba.api.RetrofitInstance
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.cordiprueba.R

@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME() {

    val navController = rememberNavController()

    HOME_SCREEN(
        navController = navController
    )
}
@Composable
fun HOME_SCREEN(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF061423))
            ) {

                HEADER()

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {

                    item {
                        HERO_SECTION(navController)
                    }

                    item {
                        FEED_SECTION(
                            navController = navController
                        )
                    }
                }
            }

            FLOATING_BUTTON(navController)

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                BOTTOM_BAR(navController)
            }
        }
    }

@Composable
fun HEADER() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(id = R.drawable.mi_foto_perfil),
                contentDescription = "Logo o Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF4F9DFF), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "bienvenido a ing. Electronica y Computación",
                color = Color(0xFF4F9DFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HERO_SECTION(navController: NavHostController) {

    Column(modifier = Modifier.padding(top = 16.dp)) {
        HERO_CARD(
            title = "Electrónica",
            category = "Hardware",
            description = "Diseño de circuitos y hardware",
            color = Color(0xFF2EC4B6),
            imageRes = R.drawable.img_electronica,
            onClick = {
                navController.navigate("electronica")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        HERO_CARD(
            title = "Computación",
            category = "SOFTWARE",
            description = "Desarrollo de software y sistemas embebidos",
            color = Color.DarkGray,
            imageRes = R.drawable.img_computo,
            onClick = {
                navController.navigate("computo")
            }
        )
    }
}

@Composable
fun HERO_CARD(
    title: String,
    category: String,
    description: String,
    color: Color,
    imageRes: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Fondo de $title",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = category,
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FEED_SECTION(navController: NavHostController) {
    var listaPracticas by remember { mutableStateOf<List<PracticaResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.obtenerPracticas()
            }
            if (response.isSuccessful) {
                val todas = response.body()?.reversed() ?: emptyList()

                val electronicas = todas.filter { it.tipo.equals("electronica", ignoreCase = true) }.take(3)
                val computos = todas.filter { it.tipo.equals("computo", ignoreCase = true) }.take(3)

                listaPracticas = (electronicas + computos).sortedByDescending { it.id }
            }
        } catch (e: Exception) {
            android.util.Log.e("FEED", "Error cargando feed: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("explora", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF2EC4B6),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)
            )
        } else if (listaPracticas.isEmpty()) {
            Text("No hay prácticas disponibles.", color = Color.Gray, fontSize = 14.sp)
        } else {
            listaPracticas.forEach { practica ->
                FEED_CARD(
                    practica = practica,
                    onClick = {
                        if (practica.tipo.lowercase() == "electronica") {
                            navController.navigate("presentar_electronica/${practica.id}")
                        } else {
                            navController.navigate("presentar_computo/${practica.id}")
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun FEED_CARD(practica: PracticaResponse, onClick: () -> Unit) {
    val archivoImagen = practica.archivos.firstOrNull { it.tipo_archivo == "imagen" }
    val rutaImagen = archivoImagen?.ruta

    val urlImagen = remember(rutaImagen) {
        if (!rutaImagen.isNullOrEmpty()) {
            "http://${RetrofitInstance.IP_SERVIDOR}:8000/uploads/$rutaImagen"
        } else null
    }

    val colorEtiqueta = if (practica.tipo.lowercase() == "electronica") Color(0xFF2EC4B6) else Color(0xFF4BDDB7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF132030))
            .clickable { onClick() } // Acción al tocar la tarjeta
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (!urlImagen.isNullOrBlank()) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(urlImagen)
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen de ${practica.nombre}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF1B2A3A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No IMG", color = Color.Gray, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = practica.tipo.uppercase(),
                color = colorEtiqueta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = practica.nombre,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = practica.descripcion,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FLOATING_BUTTON(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp, end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                .clickable {
                    navController.navigate("crear") // ruta
                },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }
    }
}

@Composable
fun BOTTOM_BAR(navController: NavHostController){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BOTTOM_ITEM("inicio", true) {
            navController.navigate("home")
        }

        BOTTOM_ITEM("electronica", false) {
            navController.navigate("electronica")
        }

        BOTTOM_ITEM("computo", false) {
            navController.navigate("computo")
        }

        BOTTOM_ITEM("editar", false) {
            navController.navigate("gestion")
        }
    }
}

@Composable
fun BOTTOM_ITEM(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.size(24.dp).background(if (selected) Color.White else Color.Gray, CircleShape))
        Text(text, color = if (selected) Color.White else Color.Gray, fontSize = 12.sp)
    }
}


