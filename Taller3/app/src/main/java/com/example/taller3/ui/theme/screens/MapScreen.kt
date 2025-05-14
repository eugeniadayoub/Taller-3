package com.example.taller3.ui.theme.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().getReference("usuarios").child(uid ?: "")
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember { mutableStateOf(false) }
    var isTracking by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val userPath = remember { mutableStateListOf<LatLng>() }
    val otherUsers = remember { mutableStateMapOf<String, LatLng>() }
    val otherPaths = remember { mutableStateMapOf<String, MutableList<LatLng>>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        hasLocationPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(isTracking) {
        if (isTracking && hasLocationPermission) {
            startLocationUpdates(fusedLocationClient) { location ->
                val newLoc = LatLng(location.latitude, location.longitude)
                userLocation = newLoc
                userPath.add(newLoc)
                uid?.let {
                    database.child("latitud").setValue(location.latitude)
                    database.child("longitud").setValue(location.longitude)
                    database.child("enLinea").setValue(true)
                    database.child("historialUbicacion").push()
                        .setValue(mapOf("lat" to location.latitude, "lng" to location.longitude))
                }
                coroutineScope.launch {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newLoc, 17f))
                }
            }
        } else {
            uid?.let { database.child("enLinea").setValue(false) }
            coroutineScope.launch {
                userLocation = null
                userPath.clear()
            }
        }
    }

    LaunchedEffect(Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("usuarios")
        usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key ?: return
                if (id == uid) return

                val enLinea = snapshot.child("enLinea").getValue(Boolean::class.java) == true
                if (enLinea) {
                    val lat = snapshot.child("latitud").getValue(Double::class.java)
                    val lng = snapshot.child("longitud").getValue(Double::class.java)
                    if (lat != null && lng != null) {
                        otherUsers[id] = LatLng(lat, lng)
                    }

                    val historial = mutableListOf<LatLng>()
                    snapshot.child("historialUbicacion").children.forEach {
                        val hLat = it.child("lat").getValue(Double::class.java)
                        val hLng = it.child("lng").getValue(Double::class.java)
                        if (hLat != null && hLng != null) {
                            historial.add(LatLng(hLat, hLng))
                        }
                    }
                    otherPaths[id] = historial
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val id = snapshot.key ?: return
                if (id == uid) return

                val enLinea = snapshot.child("enLinea").getValue(Boolean::class.java) == true
                if (!enLinea) {
                    otherUsers.remove(id)
                    otherPaths.remove(id)
                    return
                }

                val lat = snapshot.child("latitud").getValue(Double::class.java)
                val lng = snapshot.child("longitud").getValue(Double::class.java)
                if (lat != null && lng != null) {
                    val newLoc = LatLng(lat, lng)
                    otherUsers[id] = newLoc
                    val path = otherPaths[id] ?: mutableListOf()
                    path.add(newLoc)
                    otherPaths[id] = path
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.key ?: return
                otherUsers.remove(id)
                otherPaths.remove(id)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ubicación en tiempo real")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isTracking,
                onCheckedChange = {
                    if (hasLocationPermission) isTracking = it
                    else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            )
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .padding(16.dp)
                    //.align(alignment = Alignment.Start)
            ) {
                Text("Volver")
            }
        }

        if (hasLocationPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                ) {
                    if (isTracking && userLocation != null) {
                        Marker(state = MarkerState(position = userLocation!!), title = "Tú")
                        Polyline(points = userPath.toList(), color = Color.Blue, width = 8f)
                    }

                    otherUsers.forEach { (id, loc) ->
                        Marker(state = MarkerState(position = loc), title = "Usuario $id")
                        otherPaths[id]?.let { path ->
                            Polyline(points = path, color = Color.Red, width = 8f)
                        }
                    }
                }
            }
        } else {
            Text("Se necesita permiso de ubicación", modifier = Modifier.padding(16.dp))
        }
    }
}

@SuppressLint("MissingPermission")
fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationUpdate: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 3000
    ).build()

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocationUpdate(it) }
            }
        },
        null
    )
}

