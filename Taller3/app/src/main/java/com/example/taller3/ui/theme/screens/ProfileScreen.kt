package com.example.taller3.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val email = auth.currentUser?.email ?: ""
    var datosCargados by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }

    if (userId == null) {
        Toast.makeText(context, "Usuario no logueado", Toast.LENGTH_SHORT).show()
        navController.navigate("login")
        return
    }

    val databaseRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "Cargando datos para UID: $userId")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    datosCargados = true
                } else {
                    Toast.makeText(context, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error cargando perfil", Toast.LENGTH_SHORT).show()
                Log.e("ProfileScreen", "Firebase error: ${error.message}")
            }
        })
    }

    if (datosCargados) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nuevaPassword,
                onValueChange = { nuevaPassword = it },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                databaseRef.child("nombre").setValue(nombre)
                databaseRef.child("telefono").setValue(telefono)
                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                navController.navigate("home")
            }) {
                Text("Guardar cambios")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                if (nuevaPassword.length >= 6) {
                    auth.currentUser?.updatePassword(nuevaPassword)
                        ?.addOnSuccessListener {
                            Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "Contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Cambiar contraseña")
            }
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Start)
            ) {
                Text("Volver")
            }

        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
