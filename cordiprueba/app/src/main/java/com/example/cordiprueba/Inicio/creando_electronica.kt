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
import java.io.File
import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.withContext


@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME4() {

    CREANDO_ELECTRONICA_SCREEN(
        navController = rememberNavController()
    )
}
@Composable
fun CREANDO_ELECTRONICA_SCREEN(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var objetivos by remember { mutableStateOf(listOf<String>()) }
    var componentes by remember { mutableStateOf(listOf<String>()) }
    var herramientas by remember { mutableStateOf(listOf<String>()) }
    var conexiones by remember { mutableStateOf(listOf<ConexionComputo>()) }
    var codigo by remember { mutableStateOf(listOf<ArchivoCodigo>()) }
    var diagramas by remember { mutableStateOf(listOf<DiagramaComputo>()) }
    var pasos by remember { mutableStateOf(listOf<String>()) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TOP_BAR_CREAR()
        Spacer(modifier = Modifier.height(20.dp))
        HERO_SECTION_CREAR()
        Spacer(modifier = Modifier.height(20.dp))
        FORM_SECTION(
            nombre = nombre,
            descripcion = descripcion,
            onNombreChange = { nombre = it },
            onDescripcionChange = { descripcion = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_SECTION(
            objetivos = objetivos,
            onObjetivosChange = {
                objetivos = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_COMPONENTES(
            componentes = componentes,
            onComponentesChange = {
                componentes = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_HERRAMIENTAS(
            herramientas = herramientas,
            onHerramientasChange = {
                herramientas = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        OBJETIVOS_CONEXIONES(
            conexiones = conexiones,
            onConexionesChange = {
                conexiones = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        CODIGO_SECTION(
            codigo = codigo,
            onCodigoChange = {
                codigo = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        DIAGRAMA_SECTION(
            diagramas = diagramas,
            onDiagramasChange = {
                diagramas = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        PASOS_SECTION(
            pasos = pasos,
            onPasosChange = {
                pasos = it
            }
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
    val context = LocalContext.current // Necesario para procesar el archivo
    SAVE_BUTTON(
        onSave = {
            val practicaRequest = PracticaRequest(
                nombre = nombre,
                descripcion = descripcion,
                tipo = "electronica",
                objetivos = objetivos,
                electronica = DetallesElectronica(
                    componentes = componentes,
                    herramientas = herramientas,
                    conexiones = conexiones,
                    codigo = codigo,
                    diagramas = diagramas,
                    pasos = pasos
                )
            )
            CoroutineScope(Dispatchers.IO).launch {
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
fun TOP_BAR_CREAR() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Creando Práctica de electrónica",
            color = Color.Cyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun HERO_SECTION_CREAR() {
    Column {
        Text("Nuevo Proyecto", color = Color.Gray, fontSize = 12.sp)
        Text(
            "Configuración de Laboratorio",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Define los parámetros de tu práctica electrónica",
            color = Color.Gray
        )
    }
}
@Composable
fun FORM_SECTION(
    nombre: String,
    descripcion: String,
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
        Text(
            text = "Nombre de la práctica",
            color = Color.Gray
        )
        TextField(
            value = nombre,
            onValueChange = onNombreChange,
            placeholder = {
                Text("Ej: Control de Motor DC")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Descripción",
            color = Color.Gray
        )
        TextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            placeholder = {
                Text("Describe la práctica...")
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun OBJETIVOS_SECTION(
    objetivos: List<String>,
    onObjetivosChange: (List<String>) -> Unit
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
            text = "Objetivos",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        objetivos.forEachIndexed { index, objetivo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "• ${index + 1}. $objetivo",
                    color = Color.Gray
                )
                Text(
                    text = "-",
                    color = Color.Red,
                    modifier = Modifier.clickable {
                        onObjetivosChange(
                            objetivos.toMutableList().apply {
                                removeAt(index)
                            }
                        )
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
            Text("agregar objetivos")
        }
    }
    if (showDialog) {
        OBJETIVOS_DIALOG(
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevosObjetivos ->
                onObjetivosChange(
                    objetivos + nuevosObjetivos
                )
                showDialog = false
            }
        )
    }
}
@Composable
fun OBJETIVOS_DIALOG(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    val listaObjetivos = remember {
        mutableStateListOf<String>()
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
                    text = "Objetivos",
                    color = Color.White,
                    fontSize = 18.sp
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
            TextField(
                value = texto,
                onValueChange = {
                    texto = it
                },
                placeholder = {
                    Text("escribe un objetivo")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {

                    if (texto.isNotBlank()) {

                        listaObjetivos.add(texto)

                        texto = ""
                    }
                }
            ) {
                Text("agregar")
            }

            Spacer(modifier = Modifier.height(16.dp))
            listaObjetivos.forEachIndexed { index, item ->

                Text(
                    text = "${index + 1}. $item",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(listaObjetivos.toList())
                }
            ) {
                Text("guardar")
            }
        }
    }
}
@Composable
fun OBJETIVOS_COMPONENTES(
    componentes: List<String>,
    onComponentesChange: (List<String>) -> Unit
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
            text = "Componentes",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))
        componentes.forEachIndexed { index, item ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "• ${index + 1}. $item",
                    color = Color.Gray
                )

                Text(
                    text = "-",
                    color = Color.Red,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable {

                        onComponentesChange(
                            componentes.toMutableList().apply {
                                removeAt(index)
                            }
                        )
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
            Text("agregar componentes")
        }
    }
    if (showDialog) {
        COMPONENTES_DIALOG(
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevosComponentes ->
                onComponentesChange(
                    componentes + nuevosComponentes
                )
                showDialog = false
            }
        )
    }
}
@Composable
fun COMPONENTES_DIALOG(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val listaComponentes = remember {
        mutableStateListOf<String>()
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

                Column {

                    Text(
                        text = "Añadir Componente",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "ingresa los datos",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "✕",
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nombre",
                color = Color.Cyan,
                fontSize = 12.sp
            )

            TextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                placeholder = {
                    Text("Ej: resistencia carbón")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Modelo",
                color = Color.Cyan,
                fontSize = 12.sp
            )

            TextField(
                value = modelo,
                onValueChange = {
                    modelo = it
                },
                placeholder = {
                    Text("Ej: RC-10K")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Cantidad",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )

                    TextField(
                        value = cantidad,
                        onValueChange = {
                            cantidad = it
                        },
                        placeholder = {
                            Text("0")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Valor",
                        color = Color.Cyan,
                        fontSize = 12.sp
                    )

                    TextField(
                        value = valor,
                        onValueChange = {
                            valor = it
                        },
                        placeholder = {
                            Text("10kΩ")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Descripción",
                color = Color.Cyan,
                fontSize = 12.sp
            )

            TextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                },
                placeholder = {
                    Text("notas del componente")
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {

                    if (nombre.isNotBlank()) {

                        listaComponentes.add(
                            "${listaComponentes.size + 1}. $cantidad x $nombre ($valor)\n$modelo\n$descripcion"
                        )

                        nombre = ""
                        modelo = ""
                        cantidad = ""
                        valor = ""
                        descripcion = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("agregar componente")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                listaComponentes.forEachIndexed { index, item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = item,
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                listaComponentes.removeAt(index)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFF2EC4B6),
                            CircleShape
                        )
                        .clickable {

                            onSave(
                                listaComponentes.toList()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✔",
                        color = Color.Black,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}
@Composable
fun OBJETIVOS_HERRAMIENTAS(
    herramientas: List<String>,
    onHerramientasChange: (List<String>) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "Herramientas",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        herramientas.forEachIndexed { index, herramienta ->

            var editando by remember { mutableStateOf(false) }
            var textoEditado by remember { mutableStateOf(herramienta) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        text = "• ${index + 1}. $herramienta",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {

                    Text(
                        text = if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {

                                if (editando) {

                                    onHerramientasChange(
                                        herramientas.toMutableList().apply {
                                            this[index] = textoEditado
                                        }
                                    )
                                }

                                editando = !editando
                            }
                    )

                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {

                            onHerramientasChange(
                                herramientas.toMutableList().apply {
                                    removeAt(index)
                                }
                            )
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
            Text("agregar herramientas")
        }
    }

    if (showDialog) {

        HERRAMIENTAS_DIALOG(
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevasHerramientas ->

                onHerramientasChange(
                    herramientas + nuevasHerramientas
                )

                showDialog = false
            }
        )
    }
}
@Composable
fun HERRAMIENTAS_DIALOG(
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {

    var herramienta by remember { mutableStateOf("") }

    val lista = remember {
        mutableStateListOf<String>()
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
                    text = "Herramientas",
                    color = Color.White,
                    fontSize = 18.sp
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

            TextField(
                value = herramienta,
                onValueChange = {
                    herramienta = it
                },
                placeholder = {
                    Text("ej: multimetro")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {

                    if (herramienta.isNotBlank()) {

                        lista.add(herramienta)

                        herramienta = ""
                    }
                }
            ) {
                Text("agregar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            lista.forEachIndexed { index, item ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "${index + 1}. $item",
                        color = Color.Gray
                    )

                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {
                            lista.removeAt(index)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSave(lista.toList())
                }
            ) {
                Text("guardar")
            }
        }
    }
}
@Composable
fun OBJETIVOS_CONEXIONES(
    conexiones: List<ConexionComputo>,
    onConexionesChange: (List<ConexionComputo>) -> Unit
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
            text = "Conexiones",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        conexiones.forEachIndexed { index, conexion ->

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
                                conexiones.toMutableList().apply {
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
                showDialog = true
            }
        ) {
            Text("agregar conexiones")
        }
    }

    if (showDialog) {

        CONEXIONES_DIALOG(
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevaConexion ->

                onConexionesChange(
                    conexiones + nuevaConexion
                )

                showDialog = false
            }
        )
    }
}
@Composable
fun CONEXIONES_DIALOG(
    onDismiss: () -> Unit,
    onSave: (ConexionComputo) -> Unit
) {

    var componenteA by remember { mutableStateOf("") }
    var componenteB by remember { mutableStateOf("") }

    val listaPines = remember {
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
                    value = componenteA,
                    onValueChange = {
                        componenteA = it
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
                    value = componenteB,
                    onValueChange = {
                        componenteB = it
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

            listaPines.forEachIndexed { index, pin ->

                var pinA by remember { mutableStateOf(pin.first) }
                var pinB by remember { mutableStateOf(pin.second) }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextField(
                        value = pinA,
                        onValueChange = {

                            pinA = it

                            listaPines[index] = Pair(
                                it,
                                pinB
                            )
                        },
                        placeholder = {
                            Text("pin")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "→",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    TextField(
                        value = pinB,
                        onValueChange = {

                            pinB = it

                            listaPines[index] = Pair(
                                pinA,
                                it
                            )
                        },
                        placeholder = {
                            Text("pin")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    listaPines.add("" to "")
                }
            ) {
                Text("agregar pin")
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

                            if (
                                componenteA.isNotBlank() &&
                                componenteB.isNotBlank() &&
                                listaPines.isNotEmpty()
                            ) {

                                onSave(
                                    ConexionComputo(
                                        componenteA = componenteA,
                                        componenteB = componenteB,
                                        pines = listaPines.toList()
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "✔",
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CODIGO_SECTION(
    codigo: List<ArchivoCodigo>,
    onCodigoChange: (List<ArchivoCodigo>) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

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

        codigo.forEachIndexed { index, archivo ->

            var editando by remember { mutableStateOf(false) }
            var nombreEditado by remember { mutableStateOf(archivo.nombre) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

                    Text(
                        text = "• ${index + 1}. ${archivo.nombre}",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {

                    Text(
                        text = if (editando) "✔" else "✎",
                        color = Color.Cyan,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {

                                if (editando) {

                                    onCodigoChange(
                                        codigo.toMutableList().apply {
                                            this[index] =
                                                archivo.copy(nombre = nombreEditado)
                                        }
                                    )
                                }

                                editando = !editando
                            }
                    )

                    Text(
                        text = "-",
                        color = Color.Red,
                        modifier = Modifier.clickable {

                            onCodigoChange(
                                codigo.toMutableList().apply {
                                    removeAt(index)
                                }
                            )
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
            Text("agregar código")
        }
    }

    if (showDialog) {

        CODIGO_DIALOG(
            listaExistente = codigo,
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevosArchivos ->

                onCodigoChange(codigo + nuevosArchivos)

                showDialog = false
            }
        )
    }
}
@Composable
fun CODIGO_DIALOG(
    listaExistente: List<ArchivoCodigo>,
    onDismiss: () -> Unit,
    onSave: (List<ArchivoCodigo>) -> Unit
) {

    val context = LocalContext.current

    val lista = remember {
        mutableStateListOf<ArchivoCodigo>().apply {
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
                ?.use { reader ->
                    reader.readText()
                } ?: ""

            lista.add(
                ArchivoCodigo(
                    nombre = nombre,
                    contenido = contenido
                )
            )
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
                    text = "Código Embebido",
                    color = Color.White,
                    fontSize = 18.sp
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

            Column(
                modifier = Modifier
                    .height(250.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                lista.forEachIndexed { index, archivo ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {

                            Text(
                                text = archivo.nombre,
                                color = Color.White
                            )

                            Text(
                                text = "archivo agregado",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "-",
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
                        text = "✔",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(
                            Color(0xFF132030),
                            RoundedCornerShape(16.dp)
                        )
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

                    Text(
                        text = "📁",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DIAGRAMA_SECTION(
    diagramas: List<DiagramaComputo>,
    onDiagramasChange: (List<DiagramaComputo>) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "Diagramas",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        diagramas.forEachIndexed { index, diagrama ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "• ${index + 1}. ${diagrama.nombre}",
                    color = Color.Gray
                )

                Text(
                    text = "-",
                    color = Color.Red,
                    modifier = Modifier.clickable {

                        onDiagramasChange(
                            diagramas.toMutableList().apply {
                                removeAt(index)
                            }
                        )
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

        DIAGRAMA_DIALOG(
            listaExistente = diagramas,
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevosDiagramas ->

                onDiagramasChange(diagramas + nuevosDiagramas)

                showDialog = false
            }
        )
    }
}

@Composable
fun DIAGRAMA_DIALOG(
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

            lista.add(
                DiagramaComputo(
                    nombre = nombre,
                    uri = it.toString()
                )
            )
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
                    text = "Diagramas",
                    color = Color.White,
                    fontSize = 18.sp
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

            Column(
                modifier = Modifier
                    .height(250.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                lista.forEachIndexed { index, diagrama ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "${index + 1}. ${diagrama.nombre}",
                            color = Color.White
                        )

                        Text(
                            text = "-",
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
                        text = "✔",
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
                        text = "📁",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}
@Composable
fun PASOS_SECTION(
    pasos: List<String>,
    onPasosChange: (List<String>) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF132030), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "Pasos",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        pasos.forEachIndexed { index, paso ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "• ${index + 1}. $paso",
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "-",
                    color = Color.Red,
                    modifier = Modifier.clickable {

                        onPasosChange(
                            pasos.toMutableList().apply {
                                removeAt(index)
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
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

        PASOS_DIALOG(
            listaExistente = pasos,
            onDismiss = {
                showDialog = false
            },
            onSave = { nuevosPasos ->

                onPasosChange(nuevosPasos)
                showDialog = false
            }
        )
    }
}
@Composable
fun PASOS_DIALOG(
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
                    text = "Pasos",
                    color = Color.White,
                    fontSize = 18.sp
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

            Column(
                modifier = Modifier
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                lista.forEachIndexed { index, item ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                Color(0xFF132030),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Color(0xFF2EC4B6),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                Text(
                                    text = "${index + 1}",
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = item,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "-",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                lista.removeAt(index)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF132030),
                            RoundedCornerShape(12.dp)
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
                            Text("escribe un paso")
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
                            text = "+",
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
                    text = "✔",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun SAVE_BUTTON(
    modifier: Modifier = Modifier,
    onSave: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    Color(0xFF4BDDB7),
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    onSave()
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "✔",
                color = Color.Black,
                fontSize = 24.sp
            )
        }
    }
}