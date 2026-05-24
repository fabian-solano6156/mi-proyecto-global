package com.example.cordiprueba.Inicio.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import android.content.Context
import android.widget.Toast
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import androidx.compose.ui.res.painterResource
import com.example.cordiprueba.api.RetrofitInstance.IP_SERVIDOR

@Preview(showBackground = true)
@Composable
fun PREVIEW_PRESENTAR_COMPUTO() {
    PRESENTAR_COMPUTO_SCREEN(navController = rememberNavController(), practicaId = 1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PRESENTAR_COMPUTO_SCREEN(
    navController: NavHostController,
    practicaId: Int,
    modifier: Modifier = Modifier
) {
    var practica by remember { mutableStateOf<PracticaResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    var selectedArchivo by remember { mutableStateOf<ArchivoComputo?>(null) }

    LaunchedEffect(practicaId) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.obtenerPracticas()
            }

            if (response.isSuccessful) {
                practica = response.body()?.find { it.id == practicaId }
                if (practica == null) {
                    errorMsg = "Práctica no encontrada."
                }
            } else {
                errorMsg = "Error del servidor: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMsg = "Error de conexión: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF2EC4B6)
            )
        } else if (errorMsg != null) {
            Text(
                text = errorMsg!!,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
        } else if (practica != null) {
            val p = practica!!

            val c = p.detalles_computo

            val archivoImagen = p.archivos.firstOrNull { it.tipo_archivo == "imagen" }
            val rutaImagen = archivoImagen?.ruta

            val urlImagen = remember(rutaImagen) {
                if (!rutaImagen.isNullOrEmpty()) {
                    "http://$IP_SERVIDOR:8000/uploads/$rutaImagen"
                } else null
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {

                item {
                    if (!urlImagen.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(urlImagen)
                                    .crossfade(true)
                                    //.diskCachePolicy(CachePolicy.DISABLED)
                                    //.memoryCachePolicy(CachePolicy.DISABLED)
                                    .build(),
                                contentDescription = "Diagrama o Imagen de Cómputo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(250.dp)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit,
                                error = painterResource(android.R.drawable.ic_dialog_alert),
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                            )
                        }
                    } else {
                        Text(
                            "No se encontró diagrama/imagen visible para esta práctica",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 16.dp, bottom = 8.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.background(Color(0xFF132030), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                    }
                }

                item {
                    val paddingTop = if (urlImagen != null) 20.dp else 8.dp

                    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = paddingTop, bottom = 20.dp)) {
                        BadgeEtiquetaComputo()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(p.nombre, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(p.descripcion, color = Color.Gray, fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        SeccionTituloComputo("Objetivos")
                        ContenedorInfoComputo {
                            p.objetivos.forEach { obj ->
                                Text("• $obj", color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }

                if (c != null) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                            if (c.aplicaciones.isNotEmpty()) {
                                SeccionTituloComputo("Aplicaciones")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    c.aplicaciones.forEach { app ->
                                        ChipDatoComputo(app)
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (c.dependencias.isNotEmpty()) {
                                SeccionTituloComputo("Dependencias")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    c.dependencias.forEach { dep ->
                                        ChipDatoComputo(dep, Color(0xFFE71D36)) // Resaltado en rojo
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (c.comandos.isNotEmpty()) {
                                SeccionTituloComputo("Comandos")
                                c.comandos.forEach { cmd ->
                                    ContenedorInfoComputo {
                                        Text(
                                            text = "Consola / Lenguaje: ${cmd.lenguaje}",
                                            color = Color.Cyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        cmd.comandos.forEach { comando ->
                                            Text(
                                                text = "> $comando",
                                                color = Color.LightGray,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (c.archivos.isNotEmpty()) {
                                SeccionTituloComputo("Archivos")
                                c.archivos.forEach { archivo ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedArchivo = archivo
                                                showSheet = true
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color(0xFF2EC4B6),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = archivo.nombre,
                                            color = Color(0xFF2EC4B6),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("Toca para ampliar", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 100.dp)
                                            .background(Color.Black, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = archivo.contenido,
                                            color = Color(0xFF00FF00).copy(alpha = 0.6f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            maxLines = 4
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }

                    if (c.pasos.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)) {
                                SeccionTituloComputo("Pasos a seguir")
                            }
                        }

                        itemsIndexed(c.pasos) { index, paso ->
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color(0xFF2EC4B6),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.width(30.dp)
                                )
                                Text(paso, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }

        if (showSheet && selectedArchivo != null) {
            val context = androidx.compose.ui.platform.LocalContext.current

            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF132030),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedArchivo!!.nombre,
                            color = Color(0xFF2EC4B6),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                descargarArchivoTextoComputo(context, selectedArchivo!!.nombre, selectedArchivo!!.contenido)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EC4B6))
                        ) {
                            Text("Descargar", color = Color(0xFF061423))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedArchivo!!.contenido,
                            color = Color(0xFF00FF00),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}


@Composable
fun SeccionTituloComputo(titulo: String) {
    Text(
        text = titulo.uppercase(),
        color = Color(0xFF2EC4B6),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ChipDatoComputo(texto: String, color: Color = Color(0xFF2EC4B6)) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
fun ContenedorInfoComputo(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun BadgeEtiquetaComputo() {
    Text(
        "CÓMPUTO",
        color = Color(0xFF4BDDB7),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .background(Color(0xFF4BDDB7).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}


fun descargarArchivoTextoComputo(context: Context, nombre: String, contenido: String) {
    try {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, nombre)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            android.provider.MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                outputStream?.write(contenido.toByteArray())
            }
            Toast.makeText(context, "Archivo de cómputo descargado", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("DESCARGA", "Error: ${e.message}")
        Toast.makeText(context, "Error al descargar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}