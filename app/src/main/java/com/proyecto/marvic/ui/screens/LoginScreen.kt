package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.viewmodel.AuthViewModel
import com.proyecto.marvic.data.UserSession
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.ui.theme.MarvicOrange
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.proyecto.marvic.viewmodel.RoleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit, 
    vm: AuthViewModel = viewModel(),
    roleVm: RoleViewModel = viewModel()
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    
    // Cargar roles desde Firebase
    LaunchedEffect(Unit) {
        roleVm.loadRoles()
    }
    
    // Dropdown de rol (sincronizado con Firebase con fallback)
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Seleccione su rol") }
    
    // Usar roles de Firebase, o fallback a roles por defecto si está vacío/cargando
    val roles = if (roleVm.getRoleDisplayNames().isNotEmpty()) {
        roleVm.getRoleDisplayNames()
    } else {
        listOf("Almacenero", "Jefe de Logística", "Gerente")
    }

    // Función para hacer login CON validación de rol
    fun performLogin() {
        vm.signIn(email.value, password.value, selectedRole) { success ->
            if (success) {
                onLoginSuccess()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Header dinámico con gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(MarvicOrange, Color(0xFFFF6B00))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "GRUPO MARVIC",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        "Sistema de Inventario",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.size(24.dp))
            // Icono circular naranja
            Column(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(MarvicOrange),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.size(24.dp))
            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Seleccione su rol para acceder", color = Color(0xFFBDBDBD), fontSize = 14.sp)
            Spacer(Modifier.size(24.dp))

            // Dropdown de selección de rol
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol de Usuario", color = Color(0xFFBDBDBD)) },
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MarvicOrange
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MarvicOrange)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF2A2A2A))
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, color = Color.White) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.size(12.dp))

            // Campos con icono - Solución simple con colores explícitos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Usuario o Email", color = Color(0xFFBDBDBD)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MarvicOrange) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { /* Focus al siguiente campo */ }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
            }
            
            Spacer(Modifier.size(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Contraseña", color = Color(0xFFBDBDBD)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MarvicOrange) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { performLogin() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
            }
            Spacer(Modifier.size(16.dp))

            Button(
                onClick = { performLogin() },
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MarvicOrange,
                    contentColor = Color.White
                )
            ) {
                Text("ENTRAR AL SISTEMA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            if (vm.errorMessage != null) {
                Spacer(Modifier.size(8.dp))
                Text(vm.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
