package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.data.UserSession
import com.proyecto.marvic.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        vm.loadUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF6B00),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a2e))
                .padding(padding)
        ) {
            if (vm.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFF6B00)
                )
            } else if (vm.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFF44336)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        vm.errorMessage ?: "Error desconocido",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { vm.loadUserProfile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B00)
                        )
                    ) {
                        Text("Reintentar")
                    }
                }
            } else {
                val user = vm.currentUser
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    
                    // Avatar circular
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Nombre completo
                    Text(
                        "${user?.nombre ?: ""} ${user?.apellido ?: ""}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Email
                    Text(
                        user?.email ?: "",
                        fontSize = 14.sp,
                        color = Color(0xFFBDBDBD)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Badge de rol
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when(user?.rol) {
                            "gerente" -> Color(0xFF9C27B0)
                            "jefe_logistica" -> Color(0xFF2196F3)
                            else -> Color(0xFF4CAF50)
                        }
                    ) {
                        Text(
                            text = when(user?.rol) {
                                "gerente" -> "Gerente"
                                "jefe_logistica" -> "Jefe de Logística"
                                "almacenero" -> "Almacenero"
                                else -> user?.rol ?: ""
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Información del usuario
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF16213e)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Información Personal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(Modifier.height(20.dp))
                            
                            ProfileInfoRow(
                                icon = Icons.Default.Badge,
                                label = "Nombre",
                                value = user?.nombre ?: "N/A"
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFF2A2A2A)
                            )
                            
                            ProfileInfoRow(
                                icon = Icons.Default.Badge,
                                label = "Apellido",
                                value = user?.apellido ?: "N/A"
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFF2A2A2A)
                            )
                            
                            ProfileInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = user?.email ?: "N/A"
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFF2A2A2A)
                            )
                            
                            ProfileInfoRow(
                                icon = Icons.Default.WorkOutline,
                                label = "Rol",
                                value = when(user?.rol) {
                                    "gerente" -> "Gerente"
                                    "jefe_logistica" -> "Jefe de Logística"
                                    "almacenero" -> "Almacenero"
                                    else -> user?.rol ?: "N/A"
                                }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Información de la cuenta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF16213e)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Información de la Cuenta",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(Modifier.height(20.dp))
                            
                            ProfileInfoRow(
                                icon = Icons.Default.CheckCircle,
                                label = "Estado",
                                value = if (user?.activo == true) "Activo" else "Inactivo",
                                valueColor = if (user?.activo == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFF2A2A2A)
                            )
                            
                            ProfileInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Fecha de creación",
                                value = formatDate(user?.fechaCreacion ?: 0L)
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFF2A2A2A)
                            )
                            
                            ProfileInfoRow(
                                icon = Icons.Default.Schedule,
                                label = "Último acceso",
                                value = formatDate(user?.ultimoAcceso ?: 0L)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Permisos
                    if (!user?.permisos.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF16213e)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Permisos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(Modifier.height(16.dp))
                                
                                user?.permisos?.forEach { permiso ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            permiso,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    // Botón de cerrar sesión
                    Button(
                        onClick = {
                            UserSession.logout()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Cerrar Sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFF6B00),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                color = Color(0xFFBDBDBD),
                fontSize = 14.sp
            )
        }
        Text(
            value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "N/A"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



