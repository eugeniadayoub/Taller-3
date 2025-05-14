package com.example.taller3.model

data class User(
    val nombre: String = "",
    val telefono: String = "",
    val enLinea: Boolean = false,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)