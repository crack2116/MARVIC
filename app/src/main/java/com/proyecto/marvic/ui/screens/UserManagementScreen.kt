package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.data.User
import com.proyecto.marvic.data.UserActivity
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.utils.DateUtils
import com.proyecto.marvic.viewmodel.UserManagementViewModel
import com.proyecto.marvic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(onBack: () -> Unit, vm: UserManagementViewModel = viewModel()) {
    var showCreateUser by remember { mutableStateOf(false) }
    var showUserDetails by remember { mutableStateOf(false) }
    var showActivityLog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Usuarios", "Actividad", "Estadísticas")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A)),
                actions = {
                    IconButton(onClick = { showCreateUser = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Usuario", tint = Color.White)
                    }
                    IconButton(onClick = { showActivityLog = true }) {
                        Icon(Icons.Default.History, contentDescription = "Ver Actividad", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = Color.White) },
                        selectedContentColor = MarvicOrange,
                        unselectedContentColor = Color.White
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> UsersListContent(
                    vm = vm,
                    onUserClick = { user ->
                        vm.selectUser(user)
                        showUserDetails = true
                    }
                )
                1 -> ActivityListContent(vm = vm)
                2 -> UserStatisticsContent(vm = vm)
            }
        }
    }
    
    // Dialogs
    if (showCreateUser) {
        CreateUserDialog(
            vm = vm,
            onDismiss = { showCreateUser = false }
        )
    }
    
    if (showUserDetails && vm.selectedUser != null) {
        UserDetailsDialog(
            user = vm.selectedUser!!,
            vm = vm,
            onDismiss = { 
                showUserDetails = false
                vm.clearSelectedUser()
            }
        )
    }
    
    if (showActivityLog) {
        ActivityLogDialog(
            vm = vm,
            onDismiss = { showActivityLog = false }
        )
    }
}

@Composable
fun UsersListContent(
    vm: UserManagementViewModel,
    onUserClick: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterRole by remember { mutableStateOf("Todos") }
    
    val roles = listOf("Todos") + vm.availableRoles.map { it.second }
    val filteredUsers = if (searchQuery.isBlank() && filterRole == "Todos") {
        vm.users
    } else {
        vm.searchUsers(searchQuery).filter { 
            if (filterRole == "Todos") true else it.rol == filterRole.lowercase()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search and filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuarios...") },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Role filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roles.size) { index ->
                val role = roles[index]
                Card(
                    onClick = { filterRole = role },
                    modifier = Modifier.padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterRole == role) MarvicOrange else Color(0xFF2A2A2A)
                    )
                ) {
                    Text(
                        text = role,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        
        // Users list
        if (vm.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MarvicOrange)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onClick = { onUserClick(user) }
                    )
                }
            }
        }
        
        if (vm.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C).copy(alpha = 0.1f))
            ) {
                Text(
                    text = vm.errorMessage!!,
                    color = Color(0xFFE74C3C),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (user.activo) MarvicOrange else Color(0xFF757575),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user.nombre.firstOrNull() ?: ""}${user.apellido.firstOrNull() ?: ""}".uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.nombre} ${user.apellido}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = user.email,
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
                Text(
                    text = user.rol.replaceFirstChar { it.uppercase() },
                    color = if (user.activo) MarvicGreen else Color(0xFFE74C3C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Status indicator
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    if (user.activo) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (user.activo) MarvicGreen else Color(0xFFE74C3C),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (user.activo) "Activo" else "Inactivo",
                    color = if (user.activo) MarvicGreen else Color(0xFFE74C3C),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ActivityListContent(vm: UserManagementViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Actividad Reciente",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (vm.activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay actividad registrada", color = Color(0xFFBDBDBD))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.activities.take(50)) { activity ->
                    ActivityCard(activity = activity, vm = vm)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: UserActivity,
    vm: UserManagementViewModel
) {
    val user = vm.getUserById(activity.userId)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.descripcion,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${user?.nombre ?: "Usuario desconocido"} • ${DateUtils.formatDate(activity.timestamp)}",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun UserStatisticsContent(vm: UserManagementViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Estadísticas de Usuarios",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Statistics cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserStatCard(
                title = "Total Usuarios",
                value = vm.users.size.toString(),
                color = MarvicOrange,
                modifier = Modifier.weight(1f)
            )
            UserStatCard(
                title = "Usuarios Activos",
                value = vm.getActiveUsers().size.toString(),
                color = MarvicGreen,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserStatCard(
                title = "Total Actividad",
                value = vm.activities.size.toString(),
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            UserStatCard(
                title = "Roles Únicos",
                value = vm.users.map { it.rol }.distinct().size.toString(),
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Role distribution
        Text(
            text = "Distribución por Roles",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        vm.availableRoles.forEach { (roleKey, roleName) ->
            val count = vm.getUsersByRole(roleKey).size
            if (count > 0) {
                RoleDistributionCard(
                    role = roleName,
                    count = count,
                    total = vm.users.size
                )
            }
        }
    }
}

@Composable
fun UserStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RoleDistributionCard(
    role: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count * 100 / total) else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = role,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count usuarios ($percentage%)",
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp
            )
        }
    }
}
