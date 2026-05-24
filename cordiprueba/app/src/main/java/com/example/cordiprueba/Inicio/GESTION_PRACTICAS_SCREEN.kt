package com.example.cordiprueba.Inicio.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cordiprueba.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GESTION_PRACTICAS_SCREEN(navController: NavHostController) {
    var listaPracticas by remember { mutableStateOf<List<PracticaResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var practicaAEliminar by remember { mutableStateOf<PracticaResponse?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.obtenerPracticas()
            }
            if (response.isSuccessful) {
                listaPracticas = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("GESTION", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF061423))
                    .padding(16.dp)
            ) {
                Text(
                    "Panel de Gestión",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFF061423)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF2EC4B6))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(listaPracticas) { practica ->
                        TARJETA_GESTION(
                            practica = practica,
                            onEdit = {
                                val ruta = if(practica.tipo == "electronica") "editar_electronica" else "editar_computo"
                                navController.navigate("$ruta/${practica.id}")
                            },
                            onDelete = {
                                practicaAEliminar = practica
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && practicaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF132030),
            title = { Text("¿Eliminar práctica?", color = Color.White) },
            text = { Text("Esta acción no se puede deshacer. Se borrará '${practicaAEliminar?.nombre}'.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitInstance.api.eliminarPractica(practicaAEliminar!!.id)
                            }
                            if (response.isSuccessful) {
                                listaPracticas = listaPracticas.filter { it.id != practicaAEliminar!!.id }
                            }
                        } catch (e: Exception) { /* manejar error */ }
                        showDeleteDialog = false
                    }
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun TARJETA_GESTION(
    practica: PracticaResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF132030))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = practica.tipo.uppercase(),
                color = if(practica.tipo == "electronica") Color(0xFF2EC4B6) else Color(0xFF4BDDB7),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(practica.nombre, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Cyan)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = 0.2f))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text("-", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
