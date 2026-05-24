package com.example.cordiprueba.Inicio.composables

data class PracticaRequest(
    val nombre: String,
    val descripcion: String,
    val tipo: String, // "electronica" o "computo"
    val objetivos: List<String>,
    val electronica: DetallesElectronica? = null,
    val computo: DetallesComputo? = null
)

data class DetallesElectronica(
    val componentes: List<String>,
    val herramientas: List<String>,
    val conexiones: List<ConexionComputo>,
    val codigo: List<ArchivoCodigo>,
    val diagramas: List<DiagramaComputo>,
    val pasos: List<String>
)
data class DetallesComputo(
    val aplicaciones: List<String>,
    val dependencias: List<String>,
    val comandos: List<ComandoComputo>,
    val archivos: List<ArchivoComputo>,
    val pasos: List<String>
)

data class ComandoComputo(
    val lenguaje: String,
    val comandos: List<String>
)

data class DiagramaComputo(
    val nombre: String,
    val uri: String
)

data class ConexionComputo(
    val componenteA: String,
    val componenteB: String,
    val pines: List<Pair<String, String>>
)
data class ArchivoComputo(
    val nombre: String,
    val contenido: String
)
data class ArchivoCodigo(
    val nombre: String,
    val contenido: String
)
data class PracticaResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val objetivos: List<String>,
    val archivos: List<ArchivoResponse> = emptyList(),
    // Aquí vienen las imágenes
    val detalles_electronica: DetallesElectronica? = null,
    val detalles_computo: DetallesComputo? = null
)
data class ArchivoResponse(
    val id: Int,
    val nombre: String,
    val ruta: String,
    val tipo_archivo: String,
    val extension: String
)