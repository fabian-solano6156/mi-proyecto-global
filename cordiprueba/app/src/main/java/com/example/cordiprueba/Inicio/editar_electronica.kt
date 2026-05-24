package com.example.cordiprueba.Inicio.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Composable
fun EDITAR_ELECTRONICA_SCREEN(
    navController: NavHostController,
    practicaId: Int, // Recibimos el ID de la práctica seleccionada en GESTION_PRACTICAS_SCREEN
    modifier: Modifier = Modifier
) {

    var nombreEdit by remember { mutableStateOf("") }
    var descripcionEdit by remember { mutableStateOf("") }

    var objetivosEdit by remember { mutableStateOf(listOf<String>()) }
    var componentesEdit by remember { mutableStateOf(listOf<String>()) }
    var herramientasEdit by remember { mutableStateOf(listOf<String>()) }
    var conexionesEdit by remember { mutableStateOf(listOf<ConexionComputo>()) }
    var codigoEdit by remember { mutableStateOf(listOf<ArchivoCodigo>()) }
    var diagramasEdit by remember { mutableStateOf(listOf<DiagramaComputo>()) }
    var pasosEdit by remember { mutableStateOf(listOf<String>()) }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(practicaId) {
        try {
            val response = RetrofitInstance.api.getPracticaById(practicaId)

            if (response.isSuccessful) {
                response.body()?.let { practica ->
                    nombreEdit = practica.nombre
                    descripcionEdit = practica.descripcion
                    objetivosEdit = practica.objetivos

                    practica.detalles_electronica?.let { detalles ->
                        componentesEdit = detalles.componentes
                        herramientasEdit = detalles.herramientas
                        conexionesEdit = detalles.conexiones
                        codigoEdit = detalles.codigo
                        diagramasEdit = detalles.diagramas
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

        TOP_BAR_EDITAR()
        Spacer(modifier = Modifier.height(20.dp))
        HERO_SECTION_EDITAR()
        Spacer(modifier = Modifier.height(20.dp))
        FORM_SECTION_EDITAR(
            nombreEdit = nombreEdit,
            descripcionEdit = descripcionEdit,
            onNombreChange = { nombreEdit = it },
            onDescripcionChange = { descripcionEdit = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_SECTION_EDITAR(
            objetivosEdit = objetivosEdit,
            onObjetivosChange = {
                objetivosEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_COMPONENTES_EDITAR(
            componentesEdit = componentesEdit,
            onComponentesChange = {
                componentesEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_HERRAMIENTAS_EDITAR(
            herramientasEdit = herramientasEdit,
            onHerramientasChange = {
                herramientasEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_CONEXIONES_EDITAR(
            conexionesEdit = conexionesEdit,
            onConexionesChange = {
                conexionesEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_CONEXIONES_EDITAR(
            conexionesEdit = conexionesEdit,
            onConexionesChange = {
                conexionesEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        CODIGO_SECTION_EDITAR(
            codigoEdit = codigoEdit,
            onCodigoChange = {
                codigoEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        DIAGRAMA_SECTION_EDITAR(
            diagramasEdit = diagramasEdit,
            onDiagramasChange = {
                diagramasEdit = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        PASOS_SECTION_EDITAR(
            pasosEdit = pasosEdit,
            onPasosChange = {
                pasosEdit = it
            }
        )
        Spacer(modifier = Modifier.height(80.dp))
    }

    val context = LocalContext.current

    SAVE_BUTTON(
        onSave = {
            val practicaUpdateRequest = PracticaRequest(
                nombre = nombreEdit,
                descripcion = descripcionEdit,
                tipo = "electronica",
                objetivos = objetivosEdit,
                electronica = DetallesElectronica(
                    componentes = componentesEdit,
                    herramientas = herramientasEdit,
                    conexiones = conexionesEdit,
                    codigo = codigoEdit,
                    diagramas = diagramasEdit,
                    pasos = pasosEdit
                )
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.actualizarPractica(practicaId, practicaUpdateRequest)

                    if (response.isSuccessful) {

                        diagramasEdit.firstOrNull()?.let { diagrama ->
                            if(diagrama.uri.startsWith("content://")) {
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
                            navController.popBackStack() // Regresa a la pantalla de gestión
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
fun TOP_BAR_EDITAR() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Editando Práctica de electrónica",
            color = Color(0xFF2EC4B6),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HERO_SECTION_EDITAR() {
    Column {
        Text("Modificar Proyecto", color = Color.Gray, fontSize = 12.sp)
        Text(
            "Ajustes de Laboratorio",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Actualiza los parámetros de tu práctica",
            color = Color.Gray
        )
    }
}

@Composable
fun FORM_SECTION_EDITAR(
    nombreEdit: String,
    descripcionEdit: String,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF132030),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(text = "Nombre de la práctica", color = Color.Gray)
        TextField(
            value = nombreEdit,
            onValueChange = onNombreChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Descripción", color = Color.Gray)
        TextField(
            value = descripcionEdit,
            onValueChange = onDescripcionChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OBJETIVOS_SECTION_EDITAR(
    objetivosEdit: List<String>,
    onObjetivosChange: (List<String>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = "Objetivos", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        objetivosEdit.forEachIndexed { index, objetivo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "• ${index + 1}. $objetivo", color = Color.Gray)
                Text(
                    text = "-",
                    color = Color.Red,
                    modifier = Modifier.clickable {
                        onObjetivosChange(objetivosEdit.toMutableList().apply { removeAt(index) })
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar objetivos")
        }
    }

    if (showDialogEdit) {
        OBJETIVOS_DIALOG_EDITAR(
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosObjetivos ->
                onObjetivosChange(objetivosEdit + nuevosObjetivos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun OBJETIVOS_DIALOG_EDITAR(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var textoNuevo by remember { mutableStateOf("") }
    val listaObjTemp = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF020F1E), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Añadir Objetivos", color = Color.White, fontSize = 18.sp)
                Text(text = "✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = textoNuevo,
                onValueChange = { textoNuevo = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                if (textoNuevo.isNotBlank()) {
                    listaObjTemp.add(textoNuevo)
                    textoNuevo = ""
                }
            }) {
                Text("Añadir")
            }
            Spacer(modifier = Modifier.height(16.dp))
            listaObjTemp.forEachIndexed { index, item ->
                Text(text = "${index + 1}. $item", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onSave(listaObjTemp.toList()) }) {
                Text("Guardar cambios")
            }
        }
    }
}

@Composable
fun OBJETIVOS_COMPONENTES_EDITAR(
    componentesEdit: List<String>,
    onComponentesChange: (List<String>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = "Componentes", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        componentesEdit.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "• ${index + 1}. $item", color = Color.Gray)
                Text(
                    text = "-",
                    color = Color.Red,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {
                        onComponentesChange(componentesEdit.toMutableList().apply { removeAt(index) })
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar componentes")
        }
    }

    if (showDialogEdit) {
        COMPONENTES_DIALOG(
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosComponentes ->
                onComponentesChange(componentesEdit + nuevosComponentes)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun OBJETIVOS_HERRAMIENTAS_EDITAR(
    herramientasEdit: List<String>,
    onHerramientasChange: (List<String>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = "Herramientas", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        herramientasEdit.forEachIndexed { index, herramienta ->
            var inlineEditing by remember { mutableStateOf(false) }
            var inlineText by remember { mutableStateOf(herramienta) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (inlineEditing) {
                    TextField(
                        value = inlineText,
                        onValueChange = { inlineText = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "• ${index + 1}. $herramienta",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row {
                    Text(
                        text = if (inlineEditing) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (inlineEditing) {
                                    onHerramientasChange(herramientasEdit.toMutableList().apply { this[index] = inlineText })
                                }
                                inlineEditing = !inlineEditing
                            }
                    )
                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            onHerramientasChange(herramientasEdit.toMutableList().apply { removeAt(index) })
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar herramientas")
        }
    }

    if (showDialogEdit) {
        HERRAMIENTAS_DIALOG(
            onDismiss = { showDialogEdit = false },
            onSave = { nuevasHerramientas ->
                onHerramientasChange(herramientasEdit + nuevasHerramientas)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun OBJETIVOS_CONEXIONES_EDITAR(
    conexionesEdit: List<ConexionComputo>,
    onConexionesChange: (List<ConexionComputo>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

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
            text = "Conexiones",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        conexionesEdit.forEachIndexed { index, conexion ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${conexion.componenteA} → ${conexion.componenteB}",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            onConexionesChange(
                                conexionesEdit.toMutableList().apply {
                                    removeAt(index)
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                conexion.pines.forEach { pin ->
                    Text(
                        text = "• ${pin.first} → ${pin.second}",
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                showDialogEdit = true
            }
        ) {
            Text("Agregar conexiones")
        }
    }

    if (showDialogEdit) {
        CONEXIONES_DIALOG_EDITAR(
            onDismiss = {
                showDialogEdit = false
            },
            onSave = { nuevaConexion ->
                onConexionesChange(
                    conexionesEdit + nuevaConexion
                )
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun CONEXIONES_DIALOG_EDITAR(
    onDismiss: () -> Unit,
    onSave: (ConexionComputo) -> Unit
) {
    var componenteAEdit by remember { mutableStateOf("") }
    var componenteBEdit by remember { mutableStateOf("") }

    val listaPinesEdit = remember {
        mutableStateListOf<Pair<String, String>>()
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Agregar conexión",
                    color = Color.White
                )
                Text(
                    text = "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = componenteAEdit,
                    onValueChange = {
                        componenteAEdit = it
                    },
                    placeholder = {
                        Text("Componente A")
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "→",
                    color = Color.Cyan,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = componenteBEdit,
                    onValueChange = {
                        componenteBEdit = it
                    },
                    placeholder = {
                        Text("Componente B")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mapeo de pines",
                color = Color.Cyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            listaPinesEdit.forEachIndexed { index, pin ->
                var pinAEdit by remember { mutableStateOf(pin.first) }
                var pinBEdit by remember { mutableStateOf(pin.second) }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = pinAEdit,
                        onValueChange = {
                            pinAEdit = it
                            listaPinesEdit[index] = Pair(it, pinBEdit)
                        },
                        placeholder = { Text("pin") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "→", color = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    TextField(
                        value = pinBEdit,
                        onValueChange = {
                            pinBEdit = it
                            listaPinesEdit[index] = Pair(pinAEdit, it)
                        },
                        placeholder = { Text("pin") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    listaPinesEdit.add("" to "")
                }
            ) {
                Text("Agregar pin")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFF2EC4B6),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (componenteAEdit.isNotBlank() &&
                                componenteBEdit.isNotBlank() &&
                                listaPinesEdit.isNotEmpty()
                            ) {
                                onSave(
                                    ConexionComputo(
                                        componenteA = componenteAEdit,
                                        componenteB = componenteBEdit,
                                        pines = listaPinesEdit.toList()
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✔", color = Color.Black, fontSize = 20.sp)
                }
            }
        }
    }
}
@Composable
fun CODIGO_SECTION_EDITAR(
    codigoEdit: List<ArchivoCodigo>,
    onCodigoChange: (List<ArchivoCodigo>) -> Unit
) {
    var showDialogEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF020F1E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Código Embebido",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        codigoEdit.forEachIndexed { index, archivo ->
            var inlineEditing by remember { mutableStateOf(false) }
            var nombreEditado by remember { mutableStateOf(archivo.nombre) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (inlineEditing) {
                    TextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "• ${index + 1}. ${archivo.nombre}",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {
                    Text(
                        text = if (inlineEditing) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                if (inlineEditing) {
                                    onCodigoChange(
                                        codigoEdit.toMutableList().apply {
                                            this[index] = archivo.copy(nombre = nombreEditado)
                                        }
                                    )
                                }
                                inlineEditing = !inlineEditing
                            }
                    )
                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            onCodigoChange(
                                codigoEdit.toMutableList().apply { removeAt(index) }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { showDialogEdit = true }) {
            Text("Agregar código")
        }
    }

    if (showDialogEdit) {
        CODIGO_DIALOG_EDITAR(
            listaExistente = codigoEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosArchivos ->
                onCodigoChange(codigoEdit + nuevosArchivos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun CODIGO_DIALOG_EDITAR(
    listaExistente: List<ArchivoCodigo>,
    onDismiss: () -> Unit,
    onSave: (List<ArchivoCodigo>) -> Unit
) {
    val context = LocalContext.current
    val listaEdit = remember {
        mutableStateListOf<ArchivoCodigo>().apply {
            addAll(listaExistente)
        }
    }

    val launcherEdit = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "archivo"
            val contenido = context.contentResolver
                .openInputStream(it)
                ?.bufferedReader()
                ?.use { reader -> reader.readText() } ?: ""

            listaEdit.add(ArchivoCodigo(nombre = nombre, contenido = contenido))
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
                Text("Código Embebido", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.height(250.dp).verticalScroll(rememberScrollState())) {
                listaEdit.forEachIndexed { index, archivo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(archivo.nombre, color = Color.White)
                            Text("archivo cargado", color = Color.Gray, fontSize = 12.sp)
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
                        .clickable {
                            launcherEdit.launch(arrayOf("text/plain", "application/octet-stream"))
                        },
                    contentAlignment = Alignment.Center
                ) { Text("📁", color = Color.White, fontSize = 20.sp) }
            }
        }
    }
}


@Composable
fun DIAGRAMA_SECTION_EDITAR(
    diagramasEdit: List<DiagramaComputo>,
    onDiagramasChange: (List<DiagramaComputo>) -> Unit
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

        diagramasEdit.forEachIndexed { index, diagrama ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• ${index + 1}. ${diagrama.nombre}", color = Color.Gray)
                Text("-", color = Color.Red, modifier = Modifier.clickable {
                    onDiagramasChange(diagramasEdit.toMutableList().apply { removeAt(index) })
                })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) { Text("Agregar diagrama") }
    }

    if (showDialogEdit) {
        DIAGRAMA_DIALOG_EDITAR(
            listaExistente = diagramasEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosDiagramas ->
                onDiagramasChange(diagramasEdit + nuevosDiagramas)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun DIAGRAMA_DIALOG_EDITAR(
    listaExistente: List<DiagramaComputo>,
    onDismiss: () -> Unit,
    onSave: (List<DiagramaComputo>) -> Unit
) {
    val listaEdit = remember {
        mutableStateListOf<DiagramaComputo>().apply { addAll(listaExistente) }
    }

    val launcherEdit = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val nombre = it.lastPathSegment ?: "imagen"
            listaEdit.add(DiagramaComputo(nombre = nombre, uri = it.toString()))
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF020F1E), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Diagramas", color = Color.White, fontSize = 18.sp)
                Text("✕", color = Color.White, modifier = Modifier.clickable { onDismiss() })
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.height(250.dp).verticalScroll(rememberScrollState())) {
                listaEdit.forEachIndexed { index, diagrama ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. ${diagrama.nombre}", color = Color.White)
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
fun PASOS_SECTION_EDITAR(
    pasosEdit: List<String>,
    onPasosChange: (List<String>) -> Unit
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
                    onPasosChange(pasosEdit.toMutableList().apply { removeAt(index) })
                })
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { showDialogEdit = true }) { Text("Agregar pasos") }
    }

    if (showDialogEdit) {
        PASOS_DIALOG_EDITAR(
            listaExistente = pasosEdit,
            onDismiss = { showDialogEdit = false },
            onSave = { nuevosPasos ->
                onPasosChange(nuevosPasos)
                showDialogEdit = false
            }
        )
    }
}

@Composable
fun PASOS_DIALOG_EDITAR(
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .background(Color(0xFF132030), RoundedCornerShape(12.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).background(Color(0xFF2EC4B6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("${index + 1}", color = Color.Black, fontSize = 12.sp) }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(item, color = Color.White)
                        }
                        Text("-", color = Color.Red, modifier = Modifier.clickable { listaEdit.removeAt(index) })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF132030), RoundedCornerShape(12.dp)).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textoEdit,
                        onValueChange = { textoEdit = it },
                        placeholder = { Text("escribe un paso") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(45.dp).background(Color(0xFF2EC4B6), RoundedCornerShape(10.dp))
                            .clickable {
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
                modifier = Modifier.align(Alignment.CenterHorizontally).size(60.dp)
                    .background(Color(0xFF2EC4B6), RoundedCornerShape(16.dp))
                    .clickable { onSave(listaEdit.toList()) },
                contentAlignment = Alignment.Center
            ) { Text("✔", color = Color.White, fontSize = 20.sp) }
        }
    }
}