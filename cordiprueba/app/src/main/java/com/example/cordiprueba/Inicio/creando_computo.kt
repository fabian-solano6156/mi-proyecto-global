package com.example.cordiprueba.Inicio.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.clickable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME5() {
    val navController = rememberNavController()
    CREANDO_COMPUTO_SCREEN(
        navController = navController
    )
}

@Composable
fun CREANDO_COMPUTO_SCREEN(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var objetivos by remember { mutableStateOf(listOf<String>()) }
    var aplicaciones by remember { mutableStateOf(listOf<String>()) }
    var dependencias by remember { mutableStateOf(listOf<String>()) }
    var comandos by remember { mutableStateOf(listOf<ComandoComputo>()) }
    var archivos by remember { mutableStateOf(listOf<ArchivoComputo>()) }
    var diagramas by remember { mutableStateOf(listOf<DiagramaComputo>()) }
    var pasos by remember { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Es mejor usar el scope de Compose
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TOP_BAR_CREAR_COMPUTO()
        Spacer(modifier = Modifier.height(20.dp))
        HERO_SECTION_CREAR_COMPUTO()
        Spacer(modifier = Modifier.height(20.dp))
        FORM_SECTION_COMPUTO(
            nombre = nombre,
            descripcion = descripcion,
            onNombreChange = { nombre = it },
            onDescripcionChange = { descripcion = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_SECTION_COMPUTO(
            objetivos = objetivos,
            onSave = { objetivos = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_APLICACIONES(
            aplicaciones = aplicaciones,
            onSave = { aplicaciones = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_DEPENDENCIAS(
            dependencias = dependencias,
            onSave = { dependencias = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_COMANDOS(
            comandos = comandos,
            onSave = { comandos = it }
        )

        Spacer(modifier = Modifier.height(20.dp))
        CODIGO_SECTION_COMPUTO(
            archivos = archivos,
            onSave = { archivos = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        DIAGRAMA_SECTION_COMPUTO(
            imagenes = diagramas,
            onSave = { diagramas = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        PASOS_SECTION_COMPUTO(
            pasos = pasos,
            onSave = { pasos = it }
        )
        Spacer(modifier = Modifier.height(100.dp)) // Espacio para no tapar con el botón
    }
    SAVE_BUTTON_COMPUTO(
        onSave = {
            val practicaRequest = PracticaRequest(
                nombre = nombre,
                descripcion = descripcion,
                tipo = "computo",
                objetivos = objetivos,
                computo = DetallesComputo(
                    aplicaciones = aplicaciones,
                    dependencias = dependencias,
                    comandos = comandos,
                    archivos = archivos,
                    pasos = pasos
                )
            )
            scope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitInstance.api.guardarPractica(practicaRequest)
                    if (response.isSuccessful) {
                        val practicaCreada = response.body()
                        if (practicaCreada != null) {
                            diagramas.firstOrNull()?.let { diagrama ->
                                val uri = Uri.parse(diagrama.uri)
                                val file = uriToFile(context, uri)
                                if (file != null) {
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                                    RetrofitInstance.api.subirArchivo(practicaCreada.id, body)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            navController.popBackStack()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
}
@Composable
fun TOP_BAR_CREAR_COMPUTO() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Creando Práctica de computo",
            color = Color.Cyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun HERO_SECTION_CREAR_COMPUTO() {
    Column {
        Text("Nuevo Proyecto", color = Color.Gray, fontSize = 12.sp)
        Text(
            "Configuración de Laboratorio",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Define los parámetros de tu práctica computo",
            color = Color.Gray
        )
    }
}
@Composable
fun FORM_SECTION_COMPUTO(
    nombre: String,
    descripcion: String,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Nombre de la Práctica", color = Color.Gray)
        TextField(
            value = nombre,
            onValueChange = { onNombreChange(it) },
            placeholder = { Text("Ej: práctica de redes") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Descripción", color = Color.Gray)
        TextField(
            value = descripcion,
            onValueChange = { onDescripcionChange(it) },
            placeholder = { Text("Describe la práctica...") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun OBJETIVOS_SECTION_COMPUTO(
    objetivos: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Objetivos", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        objetivos.forEachIndexed { index, obj ->
            var editando by remember { mutableStateOf(false) }
            var textoEditado by remember { mutableStateOf(obj) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (editando) {
                    TextField(
                        value = textoEditado,
                        onValueChange = { textoEditado = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        "• ${index + 1}. $obj",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row {
                    Text(
                        if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (editando) {
                                    val nueva = objetivos.toMutableList()
                                    nueva[index] = textoEditado
                                    onSave(nueva)
                                }
                                editando = !editando
                            }
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva = objetivos.toMutableList()
                            nueva.removeAt(index)
                            onSave(nueva)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) {
            Text("agregar objetivos")
        }
    }
    if (showDialog) {
        OBJETIVOS_DIALOG_COMPUTO(
            listaExistente = objetivos,
            onDismiss = { showDialog = false },
            onSave = { nuevos ->
                onSave( nuevos)
                showDialog = false
            }
        )
    }
}
@Composable
fun OBJETIVOS_DIALOG_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val lista = remember {
        mutableStateListOf<String>().apply {
            addAll(listaExistente)
        }
    }
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Objetivos", color = Color.White, fontSize = 18.sp)
                Text(
                    "✕",
                    color = Color.White,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("escribe un objetivo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                if (texto.isNotBlank()) {
                    lista.add(texto)
                    texto = ""
                }
            }) {
                Text("agregar")
            }
            Spacer(modifier = Modifier.height(16.dp))
            lista.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${index + 1}. $item", color = Color.Gray)
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            lista.removeAt(index)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                onSave(lista.toList())
            }) {
                Text("guardar")
            }
        }
    }
}

@Composable
fun OBJETIVOS_APLICACIONES(
    aplicaciones: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Aplicaciones", color = Color.White, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))
        aplicaciones.forEachIndexed { index, item ->
            var editando by remember { mutableStateOf(false) }
            var textoEditado by remember { mutableStateOf(item) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (editando) {
                    TextField(
                        value = textoEditado,
                        onValueChange = { textoEditado = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        "• ${index + 1}. $item",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row {
                    Text(
                        if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (editando) {
                                    val nueva = aplicaciones.toMutableList()
                                    nueva[index] = textoEditado
                                    onSave(nueva)
                                }
                                editando = !editando
                            }
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva = aplicaciones.toMutableList()
                            nueva.removeAt(index)
                            onSave(nueva)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) {
            Text("agregar aplicaciones")
        }
    }
    if (showDialog) {
        COMPONENTES_DIALOG_COMPUTO(
            listaExistente = aplicaciones,
            onDismiss = { showDialog = false },
            onSave = { nuevas ->
                onSave(nuevas)
                showDialog = false
            }
        )
    }
}
@Composable
fun COMPONENTES_DIALOG_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val lista = remember {
        mutableStateListOf<String>().apply {
            addAll(listaExistente)
        }
    }
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Añadir aplicación", color = Color.White, fontSize = 18.sp)
                    Text("ingresa los datos", color = Color.Gray, fontSize = 12.sp)
                }
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nombre", color = Color.Cyan, fontSize = 12.sp)
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Ej: Android Studio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Tipo", color = Color.Cyan, fontSize = 12.sp)
            TextField(
                value = tipo,
                onValueChange = { tipo = it },
                placeholder = { Text("IDE, lenguaje, herramienta...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Descripción", color = Color.Cyan, fontSize = 12.sp)
            TextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = { Text("para qué se usa") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        lista.add(
                            "$nombre\n$tipo\n$descripcion"
                        )
                        nombre = ""
                        tipo = ""
                        descripcion = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("agregar aplicación")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                lista.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item, color = Color.White, fontSize = 12.sp)
                        Text(
                            "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                lista.removeAt(index)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .background(Color(0xFF2EC4B6), CircleShape)
                    .clickable {
                        onSave(lista.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("✔", color = Color.Black, fontSize = 22.sp)
            }
        }
    }
}
@Composable
fun OBJETIVOS_DEPENDENCIAS(
    dependencias: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF132030),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            "Dependencias",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))
        dependencias.forEachIndexed { index, item ->
            var editando by remember { mutableStateOf(false) }
            var textoEditado by remember { mutableStateOf(item) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editando) {
                    TextField(
                        value = textoEditado,
                        onValueChange = { textoEditado = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        "• ${index + 1}. $item",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row {
                    Text(
                        if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                if (editando) {
                                    val nueva = dependencias.toMutableList()
                                    nueva[index] = textoEditado
                                    onSave(nueva)
                                }
                                editando = !editando
                            }
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva = dependencias.toMutableList()
                            nueva.removeAt(index)
                            onSave(nueva)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) {
            Text("agregar dependencias")
        }
    }

    if (showDialog) {
        DEPENDENCIAS_DIALOG(
            listaExistente = dependencias,
            onDismiss = { showDialog = false },
            onSave = { nuevaLista ->
                onSave(nuevaLista)
                showDialog = false
            }
        )
    }
}

@Composable
fun DEPENDENCIAS_DIALOG(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember {
        mutableStateOf("")
    }
    val lista = remember {
        mutableStateListOf<String>().apply {
            addAll(listaExistente)
        }
    }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF020F1E),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    "Dependencias",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = texto,
                onValueChange = {
                    texto = it
                },
                placeholder = {
                    Text("ej: librerías, SDK, APIs...")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (
                        texto.isNotBlank() &&
                        !lista.contains(texto)
                    ) {
                        lista.add(texto)
                        texto = ""
                    }
                }
            ) {
                Text("agregar")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                lista.forEachIndexed { index, item ->
                    var editando by remember {
                        mutableStateOf(false)
                    }
                    var textoEditado by remember {
                        mutableStateOf(item)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        if (editando) {
                            TextField(
                                value = textoEditado,
                                onValueChange = {
                                    textoEditado = it
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                "${index + 1}. $item",
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row {
                            Text(
                                if (editando) "✔" else "✎",
                                color = Color.Cyan,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clickable {
                                        if (editando) {
                                            lista[index] =
                                                textoEditado
                                        }
                                        editando = !editando
                                    }
                            )
                            Text(
                                "-",
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    lista.removeAt(index)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .background(
                        Color(0xFF2EC4B6),
                        CircleShape
                    )
                    .clickable {
                        onSave(lista.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✔",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}
@Composable
fun OBJETIVOS_COMANDOS(
    comandos: List<ComandoComputo>,
    onSave: (List<ComandoComputo>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF132030),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            "Comandos",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        comandos.forEachIndexed { indexGrupo, grupo ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(
                        Color(0xFF0D1724),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        grupo.lenguaje,
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva =
                                comandos.toMutableList()
                            nueva.removeAt(indexGrupo)
                            onSave(nueva)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                grupo.comandos.forEachIndexed { indexCmd, cmd ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            "• ${indexCmd + 1}. $cmd",
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                val nueva =
                                    comandos.toMutableList()
                                val comandosEditados =
                                    grupo.comandos.toMutableList()
                                comandosEditados.removeAt(indexCmd)
                                if (comandosEditados.isEmpty()) {
                                    nueva.removeAt(indexGrupo)
                                } else {
                                    nueva[indexGrupo] =
                                        grupo.copy(
                                            comandos = comandosEditados
                                        )
                                }
                                onSave(nueva)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text("agregar comandos")
        }
    }
    if (showDialog) {
        COMANDOS_DIALOG_COMPUTO(
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevos ->
                onSave(comandos + nuevos)
                showDialog = false
            }
        )
    }
}

@Composable
fun COMANDOS_DIALOG_COMPUTO(
    onDismiss: () -> Unit,
    onSave: (List<ComandoComputo>) -> Unit
) {
    var programa by remember {
        mutableStateOf("")
    }
    var comando by remember {
        mutableStateOf("")
    }
    val comandosTemp = remember {
        mutableStateListOf<String>()
    }
    val programasTemp = remember {
        mutableStateListOf<ComandoComputo>()
    }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF020F1E),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Agregar Programas",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "agrega varios programas",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Text(
                    "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = programa,
                onValueChange = {
                    programa = it
                },
                placeholder = {
                    Text("Ej: Python / Bash")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                TextField(
                    value = comando,
                    onValueChange = {
                        comando = it
                    },
                    placeholder = {
                        Text("Ej: pip install numpy")
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (
                            comando.isNotBlank() &&
                            !comandosTemp.contains(comando)
                        ) {
                            comandosTemp.add(comando)
                            comando = ""
                        }
                    }
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                comandosTemp.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            "• ${index + 1}. $item",
                            color = Color.White
                        )
                        Text(
                            "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                comandosTemp.removeAt(index)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (
                        programa.isNotBlank() &&
                        comandosTemp.isNotEmpty()
                    ) {
                        programasTemp.add(
                            ComandoComputo(
                                lenguaje = programa,
                                comandos = comandosTemp.toList()
                            )
                        )
                        programa = ""
                        comando = ""
                        comandosTemp.clear()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("agregar programa")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .heightIn(max = 180.dp)
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                programasTemp.forEachIndexed { index, grupo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(
                                grupo.lenguaje,
                                color = Color.Cyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "-",
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    programasTemp.removeAt(index)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        grupo.comandos.forEach {
                            Text(
                                "• $it",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .background(
                        Color(0xFF2EC4B6),
                        CircleShape
                    )
                    .clickable {

                        onSave(programasTemp.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✔",
                    color = Color.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
}
@Composable
fun CODIGO_SECTION_COMPUTO(
    archivos: List<ArchivoComputo>,
    onSave: (List<ArchivoComputo>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF020F1E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Código", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        archivos.forEachIndexed { index, file ->
            var editando by remember { mutableStateOf(false) }
            var nombreEditado by remember { mutableStateOf(file.nombre) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (editando) {
                    TextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        "• ${index + 1}. ${file.nombre}",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row {
                    Text(
                        if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (editando) {
                                    val nueva = archivos.toMutableList()
                                    nueva[index] = file.copy(nombre = nombreEditado)
                                    onSave(nueva)
                                }
                                editando = !editando
                            }
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva = archivos.toMutableList()
                            nueva.removeAt(index)
                            onSave(nueva)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) {
            Text("agregar código")
        }
    }
    if (showDialog) {
        CODIGO_DIALOG_COMPUTO(
            listaExistente = archivos,
            onDismiss = { showDialog = false },
            onSave = { nuevos ->
                onSave(archivos + nuevos)
                showDialog = false
            }
        )
    }
}
@Composable
fun CODIGO_DIALOG_COMPUTO(
    listaExistente: List<ArchivoComputo>,
    onDismiss: () -> Unit,
    onSave: (List<ArchivoComputo>) -> Unit
) {
    val context = LocalContext.current
    val lista = remember {
        mutableStateListOf<ArchivoComputo>().apply {
            addAll(listaExistente)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "archivo"
            val contenido = context.contentResolver
                .openInputStream(it)
                ?.bufferedReader()
                ?.use { reader -> reader.readText() }
                ?: ""
            lista.add(
                ArchivoComputo(
                    nombre = nombre,
                    contenido = contenido
                )
            )
        }
    }
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Código", color = Color.White, fontSize = 18.sp)

                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .height(250.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                lista.forEachIndexed { index, file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(file.nombre, color = Color.White)
                            Text("archivo agregado", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                lista.removeAt(index)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                        .clickable {
                            onSave(lista.toList())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✔", color = Color.White, fontSize = 20.sp)
                }
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(Color(0xFF132030), RoundedCornerShape(16.dp))
                        .clickable {
                            launcher.launch(
                                arrayOf(
                                    "text/plain",
                                    "application/octet-stream"
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📁", color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}
@Composable
fun DIAGRAMA_SECTION_COMPUTO(
    imagenes: List<DiagramaComputo>,
    onSave: (List<DiagramaComputo>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            "Diagramas",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        imagenes.forEachIndexed { index, img ->
            var editando by remember { mutableStateOf(false) }
            var nombreEditado by remember { mutableStateOf(img.nombre) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(Color(0xFF1B2A3A), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (editando) {
                    TextField(
                        value = nombreEditado,
                        onValueChange = {
                            nombreEditado = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${index + 1}. ${img.nombre}",
                            color = Color.White
                        )
                        Text(
                            img.uri,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    if (editando) "✔" else "✎",
                    color = Color.Cyan,
                    modifier = Modifier.clickable {
                        if (editando) {
                            val nuevaLista = imagenes.toMutableList()
                            nuevaLista[index] = img.copy(
                                nombre = nombreEditado
                            )
                            onSave(nuevaLista)
                        }
                        editando = !editando
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "-",
                    color = Color.Red,
                    modifier = Modifier.clickable {
                        val nuevaLista = imagenes.toMutableList()
                        nuevaLista.removeAt(index)
                        onSave(nuevaLista)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text("agregar diagrama")
        }
    }
    if (showDialog) {
        DIAGRAMA_DIALOG_COMPUTO(
            listaExistente = imagenes,
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevaLista ->
                onSave(nuevaLista)
                showDialog = false
            }
        )
    }
}
@Composable
fun DIAGRAMA_DIALOG_COMPUTO(
    listaExistente: List<DiagramaComputo>,
    onDismiss: () -> Unit,
    onSave: (List<DiagramaComputo>) -> Unit
) {
    val lista = remember {
        mutableStateListOf<DiagramaComputo>().apply {
            addAll(listaExistente)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "imagen"
            val existe = lista.any { item ->
                item.uri == it.toString()
            }
            if (!existe) {
                lista.add(
                    DiagramaComputo(
                        nombre = nombre,
                        uri = it.toString()
                    )
                )
            }
        }
    }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Diagramas",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .height(260.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                lista.forEachIndexed { index, item ->
                    var editando by remember {
                        mutableStateOf(false)
                    }
                    var nombreEditado by remember {
                        mutableStateOf(item.nombre)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editando) {
                            TextField(
                                value = nombreEditado,
                                onValueChange = {
                                    nombreEditado = it
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    item.nombre,
                                    color = Color.White
                                )
                                Text(
                                    "imagen agregada",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            if (editando) "✔" else "✎",
                            color = Color.Cyan,
                            modifier = Modifier.clickable {
                                if (editando) {
                                    lista[index] = item.copy(
                                        nombre = nombreEditado
                                    )
                                }
                                editando = !editando
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                lista.removeAt(index)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFF2EC4B6),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {

                            onSave(lista.toList())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✔",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFF132030),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📁",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
@Composable
fun PASOS_SECTION_COMPUTO(
    pasos: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            "Pasos",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        pasos.forEachIndexed { index, paso ->
            var editando by remember { mutableStateOf(false) }
            var textoEditado by remember { mutableStateOf(paso) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF2EC4B6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            color = Color.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    if (editando) {
                        TextField(
                            value = textoEditado,
                            onValueChange = {
                                textoEditado = it
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            paso,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Row {
                    Text(
                        if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (editando) {
                                    val nueva = pasos.toMutableList()
                                    nueva[index] = textoEditado
                                    onSave(nueva)
                                }
                                editando = !editando
                            }
                    )
                    Text(
                        "-",
                        color = Color.Red,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable {

                            val nueva = pasos.toMutableList()

                            nueva.removeAt(index)

                            onSave(nueva)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text("agregar pasos")
        }
    }
    if (showDialog) {
        PASOS_DIALOG_COMPUTO(
            listaExistente = pasos,
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevaLista ->
                onSave(nuevaLista)
                showDialog = false
            }
        )
    }
}
@Composable
fun PASOS_DIALOG_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val lista = remember {
        mutableStateListOf<String>().apply {
            addAll(listaExistente)
        }
    }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pasos",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                lista.forEachIndexed { index, item ->
                    var editando by remember { mutableStateOf(false) }
                    var textoEditado by remember { mutableStateOf(item) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        Color(0xFF2EC4B6),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (editando) {
                                TextField(
                                    value = textoEditado,
                                    onValueChange = {
                                        textoEditado = it
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    item,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row {
                            Text(
                                if (editando) "✔" else "✎",
                                color = Color.Cyan,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clickable {
                                        if (editando) {
                                            lista[index] = textoEditado
                                        }
                                        editando = !editando
                                    }
                            )
                            Text(
                                "-",
                                color = Color.Red,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    lista.removeAt(index)
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF132030),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = texto,
                        onValueChange = {
                            texto = it
                        },
                        placeholder = {
                            Text("escribe la descripción del paso...")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(
                                Color(0xFF2EC4B6),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (texto.isNotBlank()) {
                                    lista.add(texto)
                                    texto = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .background(
                        Color(0xFF2EC4B6),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {

                        onSave(lista.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✔",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun SAVE_BUTTON_COMPUTO(
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(0xFF4BDDB7), RoundedCornerShape(16.dp))
                .clickable {
                    onSave()
                },
            contentAlignment = Alignment.Center
        ) {
            Text("✔", color = Color.Black, fontSize = 24.sp)
        }
    }
}