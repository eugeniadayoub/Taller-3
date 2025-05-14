package com.example.taller3.repository

import com.example.taller3.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepo {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    fun registerUser(
        nombre: String,
        email: String,
        password: String,
        telefono: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val user = User(
                        nombre = nombre,
                        telefono = telefono,
                        enLinea = false,
                        latitud = 0.0,
                        longitud = 0.0
                    )

                    if (uid != null) {
                        database.child("usuarios").child(uid).setValue(user)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onFailure(it.localizedMessage ?: "Error al guardar en DB")
                            }
                    } else {
                        onFailure("No se pudo obtener el UID")
                    }
                } else {
                    onFailure(task.exception?.localizedMessage ?: "Error al registrar")
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure(task.exception?.message ?: "Error al iniciar sesión")
            }
        }
    }
}
