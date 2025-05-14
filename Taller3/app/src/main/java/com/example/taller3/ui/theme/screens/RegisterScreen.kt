package com.example.taller3.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taller3.repository.FirebaseRepo

@Composable
fun RegisterScreen(navController: NavController, repo: FirebaseRepo = FirebaseRepo()) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation())
        TextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (nombre.isBlank() || email.isBlank() || password.isBlank() || telefono.isBlank()) {
                Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                repo.registerUser(nombre, email, password, telefono,
                    onSuccess = {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onFailure = {
                        Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }) {
            Text("Registrarse")
        }
    }
}
