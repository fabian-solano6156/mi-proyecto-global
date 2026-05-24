package com.example.cordiprueba.Inicio.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Composable
fun EDITAR_COMPUTO_SCREEN(
    navController: NavHostController,
    practicaId: Int,
    modifier: Modifier = Modifier
){
    // 1. Estados para EDICIÓN
    var nombreEdit by remember { mutableStateOf("") }
    var descripcionEdit by remember { mutableStateOf("") }
    var objetivosEdit by remember { mutableStateOf(listOf<String>()) }
    var aplicacionesEdit by remember { mutableStateOf(listOf<String>()) }
    var dependenciasEdit by remember { mutableStateOf(listOf<String>()) }
    var comandosEdit by remember { mutableStateOf(listOf<ComandoComputo>()) }
    var archivosEdit by remember { mutableStateOf(listOf<ArchivoComputo>()) }
    var diagramasEdit by remember { mutableStateOf(listOf<DiagramaComputo>()) }
    var pasosEdit by remember { mutableStateOf(listOf<String>()) }

    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var archivosAEliminar by remember { mutableStateOf(listOf<Int>()) }

    LaunchedEffect(practicaId) {
        try {
            val response = RetrofitInstance.api.getPracticaById(practicaId)

            if (response.isSuccessful) {
                response.body()?.let { practica ->
                    nombreEdit = practica.nombre
                    descripcionEdit = practica.descripcion
                    objetivosEdit = practica.objetivos
                    val diagramasExistentes = practica.archivos
                        .filter { it.tipo_archivo == "imagen" }
                        .map { archivoSubido ->
                            DiagramaComputo(
                                nombre = archivoSubido.nombre,
                                uri = archivoSubido.id.toString()
                            )
                        }
                    diagramasEdit = diagramasExistentes

                    practica.detalles_computo?.let { detalles ->
                        aplicacionesEdit = detalles.aplicaciones
                        dependenciasEdit = detalles.dependencias
                        comandosEdit = detalles.comandos

                        val archivosDeCodigo = practica.archivos
                            .filter { it.tipo_archivo == "codigo" }
                            .map { archivoSubido ->
                                ArchivoComputo(nombre = archivoSubido.nombre, contenido = "")
                            }
                        archivosEdit = detalles.archivos + archivosDeCodigo
                        pasosEdit = detalles.pasos

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Cyan)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TOP_BAR_EDITAR_COMPUTO()
        Spacer(modifier = Modifier.height(20.dp))
        HERO_SECTION_EDITAR_COMPUTO()
        Spacer(modifier = Modifier.height(20.dp))

        FORM_SECTION_EDITAR_COMPUTO(
            nombre = nombreEdit,
            descripcion = descripcionEdit,
            onNombreChange = { nombreEdit = it },
            onDescripcionChange = { descripcionEdit = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OBJETIVOS_SECTION_EDITAR_COMPUTO(
            objetivos = objetivosEdit,
            onSave = { objetivosEdit = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        APLICACIONES_SECTION_EDITAR_COMPUTO(
            aplicaciones = aplicacionesEdit,
            onSave = { aplicacionesEdit = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        DEPENDENCIAS_SECTION_EDITAR_COMPUTO(
            dependencias = dependenciasEdit,
            onSave = { dependenciasEdit = it }
        )

        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_COMANDOS_EDITAR_COMPUTO(
            comandosEdit = comandosEdit,
            onSave = { comandosEdit = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        CODIGO_SECTION_EDITAR_COMPUTO(
            archivosEdit = archivosEdit,
            onSave = { archivosEdit = it }
        )
        Spacer(modifier = Modifier.height(20.dp))

        DIAGRAMA_SECTION_EDITAR_COMPUTO(
            diagramasEdit = diagramasEdit,
            onSave = { nuevaLista ->
                val borrados = diagramasEdit.filter { vieja ->
                    !nuevaLista.any { nueva -> nueva.uri == vieja.uri }
                }

                borrados.forEach { borrado ->
                    borrado.uri.toIntOrNull()?.let { id ->
                        archivosAEliminar = archivosAEliminar + id
                    }
                }

                diagramasEdit = nuevaLista
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        PASOS_SECTION_EDITAR_COMPUTO(
            pasosEdit = pasosEdit,
            onSave = { pasosEdit = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Spacer(modifier = Modifier.height(100.dp))
    }

    SAVE_BUTTON_EDITAR_COMPUTO(
        onSave = {
            val practicaUpdateRequest = PracticaRequest(
                nombre = nombreEdit,
                descripcion = descripcionEdit,
                tipo = "computo",
                objetivos = objetivosEdit,
                computo = DetallesComputo(
                    aplicaciones = aplicacionesEdit,
                    dependencias = dependenciasEdit,
                    comandos = comandosEdit,
                    archivos = archivosEdit,
                    pasos = pasosEdit
                )
            )

            scope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitInstance.api.actualizarPractica(practicaId, practicaUpdateRequest)

                    if (response.isSuccessful) {

                        archivosAEliminar.forEach { idAEliminar ->
                            RetrofitInstance.api.eliminarArchivo(idAEliminar)
                        }

                        diagramasEdit.forEach { diagrama ->
                            if (diagrama.uri.startsWith("content://")) {
                                val uri = Uri.parse(diagrama.uri)
                                val file = uriToFile(context, uri)

                                if (file != null) {
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                                    RetrofitInstance.api.subirArchivo(practicaId, body)
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
fun TOP_BAR_EDITAR_COMPUTO() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Editando Práctica de Cómputo",
            color = Color.Cyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HERO_SECTION_EDITAR_COMPUTO() {
    Column {
        Text("Modificar Proyecto", color = Color.Gray, fontSize = 12.sp)
        Text(
            "Ajustes de Laboratorio",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Actualiza los parámetros de tu práctica de cómputo",
            color = Color.Gray
        )
    }
}

@Composable
fun FORM_SECTION_EDITAR_COMPUTO(
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
            onValueChange = onNombreChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Descripción", color = Color.Gray)
        TextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OBJETIVOS_SECTION_EDITAR_COMPUTO(
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
                    Text("• ${index + 1}. $obj", color = Color.Gray, modifier = Modifier.weight(1f))
                }
                Row {
                    Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.padding(horizontal = 6.dp).clickable {
                        if (editando) {
                            val nueva = objetivos.toMutableList()
                            nueva[index] = textoEditado
                            onSave(nueva)
                        }
                        editando = !editando
                    })
                    Text("-", color = Color.Red, modifier = Modifier.clickable {
                        val nueva = objetivos.toMutableList()
                        nueva.removeAt(index)
                        onSave(nueva)
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) { Text("Agregar objetivos") }
    }

    if (showDialog) {
        OBJETIVOS_DIALOG_EDITAR_COMPUTO(
            listaExistente = objetivos,
            onDismiss = { showDialog = false },
            onSave = { nuevos ->
                onSave(nuevos)
                showDialog = false
            }
        )
    }
}

@Composable
fun OBJETIVOS_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val lista = remember { mutableStateListOf<String>().apply { addAll(listaExistente) } }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF020F1E), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Objetivos", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = texto, onValueChange = { texto = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                if (texto.isNotBlank()) {
                    lista.add(texto)
                    texto = ""
                }
            }) { Text("Añadir") }
            Spacer(modifier = Modifier.height(16.dp))
            lista.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${index + 1}. $item", color = Color.Gray)
                    Text("-", color = Color.Red, modifier = Modifier.clickable { lista.removeAt(index) })
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onSave(lista.toList()) }) { Text("Guardar cambios") }
        }
    }
}

@Composable
fun APLICACIONES_SECTION_EDITAR_COMPUTO(
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
                    TextField(value = textoEditado, onValueChange = { textoEditado = it }, modifier = Modifier.weight(1f))
                } else {
                    Text("• ${index + 1}. $item", color = Color.Gray, modifier = Modifier.weight(1f))
                }
                Row {
                    Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.padding(horizontal = 6.dp).clickable {
                        if (editando) {
                            val nueva = aplicaciones.toMutableList()
                            nueva[index] = textoEditado
                            onSave(nueva)
                        }
                        editando = !editando
                    })
                    Text("-", color = Color.Red, modifier = Modifier.clickable {
                        val nueva = aplicaciones.toMutableList()
                        nueva.removeAt(index)
                        onSave(nueva)
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) { Text("Agregar aplicaciones") }
    }

    if (showDialog) {
        APLICACIONES_DIALOG_EDITAR_COMPUTO(
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
fun APLICACIONES_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val lista = remember { mutableStateListOf<String>().apply { addAll(listaExistente) } }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF020F1E), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Añadir aplicación", color = Color.White, fontSize = 18.sp)
                    Text("Ingresa los datos", color = Color.Gray, fontSize = 12.sp)
                }
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Nombre", color = Color.Cyan, fontSize = 12.sp)
            TextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Text("Tipo", color = Color.Cyan, fontSize = 12.sp)
            TextField(value = tipo, onValueChange = { tipo = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Text("Descripción", color = Color.Cyan, fontSize = 12.sp)
            TextField(value = descripcion, onValueChange = { descripcion = it }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        lista.add("$nombre\n$tipo\n$descripcion")
                        nombre = ""
                        tipo = ""
                        descripcion = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Añadir") }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                lista.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFF132030), RoundedCornerShape(10.dp)).padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item, color = Color.White, fontSize = 12.sp)
                        Text("-", color = Color.Red, modifier = Modifier.clickable { lista.removeAt(index) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).size(60.dp).background(Color(0xFF2EC4B6), CircleShape)
                    .clickable { onSave(lista.toList()) },
                contentAlignment = Alignment.Center
            ) { Text("✔", color = Color.Black, fontSize = 22.sp) }
        }
    }
}

@Composable
fun DEPENDENCIAS_SECTION_EDITAR_COMPUTO(
    dependencias: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Dependencias", color = Color.White, fontWeight = FontWeight.Bold)
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
                    TextField(value = textoEditado, onValueChange = { textoEditado = it }, modifier = Modifier.weight(1f))
                } else {
                    Text("• ${index + 1}. $item", color = Color.Gray, modifier = Modifier.weight(1f))
                }

                Row {
                    Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.padding(horizontal = 8.dp).clickable {
                        if (editando) {
                            val nueva = dependencias.toMutableList()
                            nueva[index] = textoEditado
                            onSave(nueva)
                        }
                        editando = !editando
                    })
                    Text("-", color = Color.Red, modifier = Modifier.clickable {
                        val nueva = dependencias.toMutableList()
                        nueva.removeAt(index)
                        onSave(nueva)
                    })
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialog = true }) { Text("Agregar dependencias") }
    }

    if (showDialog) {
        DEPENDENCIAS_DIALOG_EDITAR_COMPUTO(
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
fun DEPENDENCIAS_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val lista = remember { mutableStateListOf<String>().apply { addAll(listaExistente) } }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF020F1E), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dependencias", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = texto, onValueChange = { texto = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                if (texto.isNotBlank() && !lista.contains(texto)) {
                    lista.add(texto)
                    texto = ""
                }
            }) { Text("Añadir") }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                lista.forEachIndexed { index, item ->
                    var editando by remember { mutableStateOf(false) }
                    var textoEditado by remember { mutableStateOf(item) }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFF132030), RoundedCornerShape(10.dp)).padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editando) {
                            TextField(value = textoEditado, onValueChange = { textoEditado = it }, modifier = Modifier.weight(1f))
                        } else {
                            Text("${index + 1}. $item", color = Color.White, modifier = Modifier.weight(1f))
                        }
                        Row {
                            Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.padding(horizontal = 8.dp).clickable {
                                if (editando) lista[index] = textoEditado
                                editando = !editando
                            })
                            Text("-", color = Color.Red, modifier = Modifier.clickable { lista.removeAt(index) })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).size(60.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                    .clickable { onSave(lista.toList()) },
                contentAlignment = Alignment.Center
            ) { Text("✔", color = Color.White, fontSize = 20.sp) }
        }
    }
}
@Composable
fun OBJETIVOS_COMANDOS_EDITAR_COMPUTO(
    comandosEdit: List<ComandoComputo>,
    onSave: (List<ComandoComputo>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Comandos", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        comandosEdit.forEachIndexed { indexGrupo, grupo ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color(0xFF0D1724), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(grupo.lenguaje, color = Color.Cyan, fontWeight = FontWeight.Bold)

                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            val nueva = comandosEdit.toMutableList()
                            nueva.removeAt(indexGrupo)
                            onSave(nueva)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                grupo.comandos.forEachIndexed { indexCmd, cmd ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${indexCmd + 1}. $cmd",
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                val nueva = comandosEdit.toMutableList()
                                val comandosActualizados = grupo.comandos.toMutableList()
                                comandosActualizados.removeAt(indexCmd)

                                if (comandosActualizados.isEmpty()) {
                                    nueva.removeAt(indexGrupo)
                                } else {
                                    nueva[indexGrupo] = grupo.copy(comandos = comandosActualizados)
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

        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar comandos")
        }
    }

    if (showDialogEdit) {
        COMANDOS_DIALOG_EDITAR_COMPUTO(
            onDismiss = { showDialogEdit = false },
            onSave = { nuevos ->
                onSave(comandosEdit + nuevos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun COMANDOS_DIALOG_EDITAR_COMPUTO(
    onDismiss: () -> Unit,
    onSave: (List<ComandoComputo>) -> Unit
) {
    var programaEdit by remember { mutableStateOf("") }
    var comandoEdit by remember { mutableStateOf("") }

    val comandosTempEdit = remember { mutableStateListOf<String>() }
    val programasTempEdit = remember { mutableStateListOf<ComandoComputo>() }

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
                    Text("Agregar Programas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Agrega varios programas", color = Color.Gray, fontSize = 12.sp)
                }
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = programaEdit,
                onValueChange = { programaEdit = it },
                placeholder = { Text("Ej: Python / Bash") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                TextField(
                    value = comandoEdit,
                    onValueChange = { comandoEdit = it },
                    placeholder = { Text("Ej: pip install numpy") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (comandoEdit.isNotBlank() && !comandosTempEdit.contains(comandoEdit)) {
                        comandosTempEdit.add(comandoEdit)
                        comandoEdit = ""
                    }
                }) {
                    Text("+")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                comandosTempEdit.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${index + 1}. $item", color = Color.White)
                        Text("-", color = Color.Red, modifier = Modifier.clickable { comandosTempEdit.removeAt(index) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (programaEdit.isNotBlank() && comandosTempEdit.isNotEmpty()) {
                        programasTempEdit.add(ComandoComputo(lenguaje = programaEdit, comandos = comandosTempEdit.toList()))
                        programaEdit = ""
                        comandoEdit = ""
                        comandosTempEdit.clear()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir programa")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
                programasTempEdit.forEachIndexed { index, grupo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(grupo.lenguaje, color = Color.Cyan, fontWeight = FontWeight.Bold)
                            Text("-", color = Color.Red, modifier = Modifier.clickable { programasTempEdit.removeAt(index) })
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        grupo.comandos.forEach { Text("• $it", color = Color.Gray) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .background(Color(0xFF2EC4B6), CircleShape)
                    .clickable { onSave(programasTempEdit.toList()) },
                contentAlignment = Alignment.Center
            ) {
                Text("✔", color = Color.Black, fontSize = 22.sp)
            }
        }
    }
}

@Composable
fun CODIGO_SECTION_EDITAR_COMPUTO(
    archivosEdit: List<ArchivoComputo>,
    onSave: (List<ArchivoComputo>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF020F1E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Código", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        archivosEdit.forEachIndexed { index, file ->
            var editando by remember { mutableStateOf(false) }
            var nombreEditado by remember { mutableStateOf(file.nombre) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (editando) {
                    TextField(value = nombreEditado, onValueChange = { nombreEditado = it }, modifier = Modifier.weight(1f))
                } else {
                    Text("• ${index + 1}. ${file.nombre}", color = Color.Gray, modifier = Modifier.weight(1f))
                }

                Row {
                    Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.padding(horizontal = 6.dp).clickable {
                        if (editando) {
                            val nueva = archivosEdit.toMutableList()
                            nueva[index] = file.copy(nombre = nombreEditado)
                            onSave(nueva)
                        }
                        editando = !editando
                    })
                    Text("-", color = Color.Red, modifier = Modifier.clickable {
                        val nueva = archivosEdit.toMutableList()
                        nueva.removeAt(index)
                        onSave(nueva)
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar código")
        }
    }

    if (showDialogEdit) {
        CODIGO_DIALOG_EDITAR_COMPUTO(
            listaExistente = archivosEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevos ->
                onSave(archivosEdit + nuevos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun CODIGO_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<ArchivoComputo>,
    onDismiss: () -> Unit,
    onSave: (List<ArchivoComputo>) -> Unit
) {
    val context = LocalContext.current
    val listaEdit = remember { mutableStateListOf<ArchivoComputo>().apply { addAll(listaExistente) } }

    val launcherEdit = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "archivo"
            val contenido = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
            listaEdit.add(ArchivoComputo(nombre = nombre, contenido = contenido))
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Código", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.height(250.dp).verticalScroll(rememberScrollState())) {
                listaEdit.forEachIndexed { index, file ->
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
                            Text("Archivo agregado", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("-", color = Color.Red, modifier = Modifier.clickable { listaEdit.removeAt(index) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(55.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                        .clickable { onSave(listaEdit.toList()) },
                    contentAlignment = Alignment.Center
                ) { Text("✔", color = Color.White, fontSize = 20.sp) }

                Box(
                    modifier = Modifier.size(55.dp).background(Color(0xFF132030), RoundedCornerShape(16.dp))
                        .clickable { launcherEdit.launch(arrayOf("text/plain", "application/octet-stream")) },
                    contentAlignment = Alignment.Center
                ) { Text("📁", color = Color.White, fontSize = 20.sp) }
            }
        }
    }
}
@Composable
fun DIAGRAMA_SECTION_EDITAR_COMPUTO(
    diagramasEdit: List<DiagramaComputo>,
    onSave: (List<DiagramaComputo>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Diagramas", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        diagramasEdit.forEachIndexed { index, img ->
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
                    TextField(value = nombreEditado, onValueChange = { nombreEditado = it }, modifier = Modifier.weight(1f))
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${index + 1}. ${img.nombre}", color = Color.White)
                        Text(img.uri, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.clickable {
                    if (editando) {
                        val nuevaLista = diagramasEdit.toMutableList()
                        nuevaLista[index] = img.copy(nombre = nombreEditado)
                        onSave(nuevaLista)
                    }
                    editando = !editando
                })

                Spacer(modifier = Modifier.width(12.dp))

                Text("-", color = Color.Red, modifier = Modifier.clickable {
                    val nuevaLista = diagramasEdit.toMutableList()
                    nuevaLista.removeAt(index)
                    onSave(nuevaLista)
                })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar diagrama")
        }
    }

    if (showDialogEdit) {
        DIAGRAMA_DIALOG_EDITAR_COMPUTO(
            listaExistente = diagramasEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevaLista ->
                onSave(nuevaLista)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun DIAGRAMA_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<DiagramaComputo>,
    onDismiss: () -> Unit,
    onSave: (List<DiagramaComputo>) -> Unit
) {
    val listaEdit = remember { mutableStateListOf<DiagramaComputo>().apply { addAll(listaExistente) } }
    val launcherEdit = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "imagen"
            if (!listaEdit.any { item -> item.uri == it.toString() }) {
                listaEdit.add(DiagramaComputo(nombre = nombre, uri = it.toString()))
            }
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Diagramas", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.height(260.dp).verticalScroll(rememberScrollState())) {
                listaEdit.forEachIndexed { index, item ->
                    var editando by remember { mutableStateOf(false) }
                    var nombreEditado by remember { mutableStateOf(item.nombre) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editando) {
                            TextField(value = nombreEditado, onValueChange = { nombreEditado = it }, modifier = Modifier.weight(1f))
                        } else {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.nombre, color = Color.White)
                                Text("Imagen agregada", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(if (editando) "✔" else "✎", color = Color.Cyan, modifier = Modifier.clickable {
                            if (editando) listaEdit[index] = item.copy(nombre = nombreEditado)
                            editando = !editando
                        })

                        Spacer(modifier = Modifier.width(12.dp))

                        Text("-", color = Color.Red, modifier = Modifier.clickable { listaEdit.removeAt(index) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(60.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                        .clickable { onSave(listaEdit.toList()) },
                    contentAlignment = Alignment.Center
                ) { Text("✔", color = Color.White, fontSize = 20.sp) }

                Box(
                    modifier = Modifier.size(60.dp).background(Color(0xFF132030), RoundedCornerShape(16.dp))
                        .clickable { launcherEdit.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) { Text("📁", color = Color.White, fontSize = 20.sp) }
            }
        }
    }
}
@Composable
fun PASOS_SECTION_EDITAR_COMPUTO(
    pasosEdit: List<String>,
    onSave: (List<String>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Pasos", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        pasosEdit.forEachIndexed { index, paso ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("• ${index + 1}. $paso", color = Color.Gray, modifier = Modifier.weight(1f))
                Text("-", color = Color.Red, modifier = Modifier.clickable {
                    val nueva = pasosEdit.toMutableList()
                    nueva.removeAt(index)
                    onSave(nueva)
                })
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) { Text("Agregar pasos") }
    }

    if (showDialogEdit) {
        PASOS_DIALOG_EDITAR_COMPUTO(
            listaExistente = pasosEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosPasos ->
                onSave(nuevosPasos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun PASOS_DIALOG_EDITAR_COMPUTO(
    listaExistente: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var textoEdit by remember { mutableStateOf("") }
    val listaEdit = remember { mutableStateListOf<String>().apply { addAll(listaExistente) } }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF020F1E), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pasos", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                listaEdit.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFF132030), RoundedCornerShape(12.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).background(Color(0xFF2EC4B6), CircleShape), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", color = Color.Black, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(item, color = Color.White)
                        }
                        Text("-", color = Color.Red, modifier = Modifier.clickable { listaEdit.removeAt(index) })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF132030), RoundedCornerShape(12.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = textoEdit, onValueChange = { textoEdit = it }, placeholder = { Text("Escribe un paso") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(45.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(10.dp)).clickable {
                            if (textoEdit.isNotBlank()) {
                                listaEdit.add(textoEdit)
                                textoEdit = ""
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) { Text("+", color = Color.White, fontSize = 20.sp) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).size(60.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp)).clickable { onSave(listaEdit.toList()) },
                contentAlignment = Alignment.Center
            ) { Text("✔", color = Color.White, fontSize = 20.sp) }
        }
    }
}

@Composable
fun SAVE_BUTTON_EDITAR_COMPUTO(
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
                .clickable { onSave() },
            contentAlignment = Alignment.Center
        ) {
            Text("✔", color = Color.Black, fontSize = 24.sp)
        }
    }
}
