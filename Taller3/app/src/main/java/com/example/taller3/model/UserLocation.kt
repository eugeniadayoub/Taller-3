package com.example.taller3.model

data class UsuarioUbicacion(
    val uid: String = "",
    val nombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val enLinea: Boolean = false
)
