package com.proyecto.marvic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.data.User
import com.proyecto.marvic.data.UserActivity
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.utils.DateUtils
import com.proyecto.marvic.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    vm: UserManagementViewModel,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("almacenero") }
    var selectedPermissions by remember { mutableStateOf(setOf<String>()) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Usuario", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico", color = Color(0xFFBDBDBD)) },
                    modifier = Modifier.fillMaxWidth(),
                    // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.weight(1f),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                    
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.weight(1f),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                }
                
                // Role selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = vm.availableRoles.find { it.first == selectedRole }?.second ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol", color = Color(0xFFBDBDBD)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vm.availableRoles.forEach { (roleKey, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName, color = Color.White) },
                                onClick = {
                                    selectedRole = roleKey
                                    selectedPermissions = vm.getRolePermissions(roleKey).toSet()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Permissions (only show for custom roles)
                if (selectedRole in listOf("supervisor", "auditor", "operario")) {
                    Text(
                        text = "Permisos",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(vm.availablePermissions) { (permissionKey, permissionName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedPermissions.contains(permissionKey),
                                        onClick = {
                                            selectedPermissions = if (selectedPermissions.contains(permissionKey)) {
                                                selectedPermissions - permissionKey
                                            } else {
                                                selectedPermissions + permissionKey
                                            }
                                        }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedPermissions.contains(permissionKey),
                                    onCheckedChange = {
                                        selectedPermissions = if (selectedPermissions.contains(permissionKey)) {
                                            selectedPermissions - permissionKey
                                        } else {
                                            selectedPermissions + permissionKey
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MarvicOrange,
                                        uncheckedColor = Color(0xFF424242)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = permissionName,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    vm.createUser(
                        email = email,
                        nombre = nombre,
                        apellido = apellido,
                        rol = selectedRole,
                        permisos = selectedPermissions.toList(),
                        onSuccess = onDismiss,
                        onError = { /* Handle error */ }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange),
                enabled = email.isNotBlank() && nombre.isNotBlank() && apellido.isNotBlank()
            ) {
                Text("Crear", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
}

@Composable
fun UserDetailsDialog(
    user: User,
    vm: UserManagementViewModel,
    onDismiss: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Usuario", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserDetailRow("Nombre", "${user.nombre} ${user.apellido}")
                UserDetailRow("Email", user.email)
                UserDetailRow("Rol", user.rol.replaceFirstChar { it.uppercase() })
                UserDetailRow("Estado", if (user.activo) "Activo" else "Inactivo")
                UserDetailRow("Fecha Creación", DateUtils.formatDate(user.fechaCreacion))
                UserDetailRow("Último Acceso", DateUtils.formatDate(user.ultimoAcceso))
                
                if (user.permisos.isNotEmpty()) {
                    Text(
                        text = "Permisos",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    user.permisos.forEach { permission ->
                        Text(
                            text = "• ${vm.availablePermissions.find { it.first == permission }?.second ?: permission}",
                            color = Color(0xFFBDBDBD),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
            ) {
                Text("Editar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color.White)
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
    
    if (showEditDialog) {
        EditUserDialog(
            user = user,
            vm = vm,
            onDismiss = { showEditDialog = false },
            onSuccess = onDismiss
        )
    }
}

@Composable
fun UserDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFFBDBDBD),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: User,
    vm: UserManagementViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var apellido by remember { mutableStateOf(user.apellido) }
    var selectedRole by remember { mutableStateOf(user.rol) }
    var isActive by remember { mutableStateOf(user.activo) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Email: ${user.email}",
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.weight(1f),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                    
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido", color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.weight(1f),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                }
                
                // Role selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = vm.availableRoles.find { it.first == selectedRole }?.second ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol", color = Color(0xFFBDBDBD)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vm.availableRoles.forEach { (roleKey, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName, color = Color.White) },
                                onClick = {
                                    selectedRole = roleKey
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Active status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usuario Activo",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MarvicOrange,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF424242)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = user.copy(
                        nombre = nombre,
                        apellido = apellido,
                        rol = selectedRole,
                        activo = isActive
                    )
                    vm.updateUser(
                        user = updatedUser,
                        onSuccess = onSuccess,
                        onError = { /* Handle error */ }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange),
                enabled = nombre.isNotBlank() && apellido.isNotBlank()
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
}

@Composable
fun ActivityLogDialog(
    vm: UserManagementViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro de Actividad", color = Color.White) },
        text = {
            Column(
                modifier = Modifier.height(400.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vm.activities) { activity ->
                        ActivityLogItem(activity = activity, vm = vm)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
            ) {
                Text("Cerrar", color = Color.White)
            }
        },
        containerColor = Color(0xFF2A2A2A)
    )
}

@Composable
fun ActivityLogItem(
    activity: UserActivity,
    vm: UserManagementViewModel
) {
    val user = vm.getUserById(activity.userId)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (activity.accion) {
                        "login" -> Icons.Default.Login
                        "movement" -> Icons.Default.SwapVert
                        "search" -> Icons.Default.Search
                        "report" -> Icons.Default.Assessment
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = MarvicOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.accion.uppercase(),
                    color = MarvicOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.descripcion,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "${user?.nombre ?: "Usuario desconocido"} • ${DateUtils.formatDate(activity.timestamp)}",
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp
            )
        }
    }
}

