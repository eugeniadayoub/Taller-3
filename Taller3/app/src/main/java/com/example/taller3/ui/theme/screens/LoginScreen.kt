package com.example.taller3.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.taller3.repository.FirebaseRepo

@Composable
fun LoginScreen(navController: NavHostController, repo: FirebaseRepo = FirebaseRepo()){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            repo.loginUser(email, password,
                onSuccess = {
                    Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()
                    navController.navigate("home")
                },
                onFailure = {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                })
        }) {
            Text("Iniciar sesión")
        }

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("¿No tienes cuenta? Regístrate")
        }

    }
}