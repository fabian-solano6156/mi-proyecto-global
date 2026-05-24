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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable

enum class TipoPractica {
    ELECTRONICA,
    COMPUTO
}

@Preview(showBackground = true)
@Composable
fun PREVIEW_HOME3() {
    val navController = rememberNavController()
    CREAR_NUEVO_SCREEN(navController = navController)
}

@Composable
fun CREAR_NUEVO_SCREEN(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    var tipoSeleccionado by remember { mutableStateOf<TipoPractica?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF061423))
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Creación de Práctica",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF132030), CircleShape)
                    .clickable {
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Selecciona el área de estudio para tu nueva práctica técnica.",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {

            // ELECTRÓNICA
            NUEVA_CARD(
                title = "Electrónica",
                description = "Diseño de circuitos, prototipado y análisis de señales.",
                color = Color(0xFF2EC4B6),
                selected = tipoSeleccionado == TipoPractica.ELECTRONICA,
                onClick = {
                    tipoSeleccionado = TipoPractica.ELECTRONICA
                    navController.navigate("creando_electronica")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // COMPUTO
            NUEVA_CARD(
                title = "Computación",
                description = "Desarrollo de algoritmos, scripting y arquitectura.",
                color = Color(0xFF4BDDB7),
                selected = tipoSeleccionado == TipoPractica.COMPUTO,
                onClick = {
                    tipoSeleccionado = TipoPractica.COMPUTO
                    navController.navigate("creando_computo")
                }
            )
        }
    }
}

@Composable
fun NUEVA_CARD(
    title: String,
    description: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (selected) Color(0xFF1B2A3A) else Color(0xFF132030),
                RoundedCornerShape(28.dp)
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = color,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
    ) {

        Box(
            modifier = Modifier
                .size(70.dp)
                .background(color, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (title == "Electrónica") "▣" else "<>",
                color = Color.White,
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            description,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "NUEVA PRACTICA",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}