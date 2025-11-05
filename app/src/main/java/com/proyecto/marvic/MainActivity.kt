package com.proyecto.marvic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge // No disponible en versiones antiguas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyecto.marvic.ui.theme.MARVICTheme
import com.proyecto.marvic.navigation.Routes
import com.proyecto.marvic.ui.screens.DashboardScreen
import com.proyecto.marvic.ui.screens.SmartDashboardScreen
import com.proyecto.marvic.ui.screens.LoginScreen
import com.proyecto.marvic.ui.screens.MovementScreen
import com.proyecto.marvic.ui.screens.SearchScreen
import com.proyecto.marvic.ui.screens.AdvancedSearchScreen
import com.proyecto.marvic.ui.screens.ScannerScreen
import com.proyecto.marvic.ui.screens.ReportsScreen
import com.proyecto.marvic.ui.screens.ExecutiveReportsScreen
import com.proyecto.marvic.ui.screens.NotificationSettingsScreen
import com.proyecto.marvic.ui.screens.UserManagementScreen
import com.proyecto.marvic.ui.screens.ProvidersScreen
import com.proyecto.marvic.ui.screens.ProjectsScreen
import com.proyecto.marvic.ui.screens.TransfersScreen
import com.proyecto.marvic.ui.screens.MaterialGalleryScreen
import com.proyecto.marvic.ui.screens.AnalyticsScreen
import com.proyecto.marvic.ui.screens.ProfileScreen
import com.proyecto.marvic.viewmodel.InventoryViewModel
import com.proyecto.marvic.data.FirebaseInitializer
import com.proyecto.marvic.data.FirestoreInitializer
import com.proyecto.marvic.data.FirestoreRoleRepository
import com.proyecto.marvic.data.UserInitializer
import com.proyecto.marvic.notifications.StockMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-inicialización de Firebase y datos
        initializeApp()
        
        // enableEdgeToEdge() // No disponible en versiones antiguas
        setContent {
            MARVICTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    /**
     * Inicializa automáticamente Firebase y todos los datos necesarios
     */
    private fun initializeApp() {
        // Firebase
        FirebaseInitializer.init(application)
        
        // Datos iniciales en segundo plano
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Inicializar TODAS las colecciones (cada una verifica si está vacía)
                // Esto asegura que proveedores, proyectos, transferencias, etc. se creen
                FirestoreInitializer.initializeIfEmpty(forceReload = false)
                
                // Inicializar roles si no existen
                FirestoreRoleRepository().initializeDefaultRoles()
                
                // Inicializar usuarios con UserInitializer (usa IDs específicos)
                UserInitializer.initializeDefaultUsers()
                
                println("✅ Inicialización automática completada - Todas las colecciones verificadas")
            } catch (e: Exception) {
                println("❌ Error en inicialización: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Monitoreo de stock
        StockMonitor.startMonitoring(this@MainActivity)
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login.route,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(Routes.Dashboard.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
            composable(Routes.Dashboard.route) {
                DashboardScreen(
                    onGoToMovement = { navController.navigate(Routes.Movement.route) },
                    onGoToSearch = { navController.navigate(Routes.AdvancedSearch.route) },
                    onGoToReports = { navController.navigate(Routes.Reports.route) },
                    onGoToSmartDashboard = { navController.navigate(Routes.SmartDashboard.route) }
                )
            }
            composable(Routes.SmartDashboard.route) {
                SmartDashboardScreen(
                    onGoToMovement = { navController.navigate(Routes.Movement.route) },
                    onGoToSearch = { navController.navigate(Routes.AdvancedSearch.route) },
                    onGoToReports = { navController.navigate(Routes.ExecutiveReports.route) },
                    onGoToNotifications = { navController.navigate(Routes.NotificationSettings.route) },
                    onGoToUserManagement = { navController.navigate(Routes.UserManagement.route) },
                    onGoToProviders = { navController.navigate(Routes.Providers.route) },
                    onGoToProjects = { navController.navigate(Routes.Projects.route) },
                    onGoToTransfers = { navController.navigate(Routes.Transfers.route) },
                    onGoToAnalytics = { navController.navigate(Routes.Analytics.route) },
                    onGoToProfile = { navController.navigate(Routes.Profile.route) }
                )
            }
        composable(Routes.Movement.route) {
            MovementScreen(
                onBack = { navController.popBackStack() },
                onGoToScanner = { navController.navigate(Routes.Scanner.route) },
                navController = navController
            )
        }
        composable(Routes.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onGoToGallery = { materialId ->
                    navController.navigate(Routes.MaterialGallery.createRoute(materialId))
                }
            )
        }
        composable(Routes.AdvancedSearch.route) {
            AdvancedSearchScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Scanner.route) {
            ScannerScreen(
                onBack = { navController.popBackStack() },
                onScanResult = { code ->
                    // Pasar el código escaneado de vuelta a Movement
                    println("📱 MainActivity: Código escaneado recibido: $code")
                    navController.previousBackStackEntry?.savedStateHandle?.set("scannedCode", code)
                    println("📱 MainActivity: Código guardado en savedStateHandle")
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.Reports.route) {
            ReportsScreen(
                onGoToExecutive = { navController.navigate(Routes.ExecutiveReports.route) }
            )
        }
        composable(Routes.ExecutiveReports.route) {
            ExecutiveReportsScreen(onBack = { navController.popBackStack() })
        }
            composable(Routes.NotificationSettings.route) {
                NotificationSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.UserManagement.route) {
                UserManagementScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Providers.route) {
                ProvidersScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Projects.route) {
                ProjectsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Transfers.route) {
                TransfersScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Analytics.route) {
                AnalyticsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable("material_gallery/{materialId}") { backStackEntry ->
                val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
                val vm: InventoryViewModel = viewModel()
                
                LaunchedEffect(materialId) {
                    vm.refreshTotals()
                }
                
                val material = vm.allMaterials.find { it.id == materialId }
                
                if (material != null) {
                    MaterialGalleryScreen(
                        material = material,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    // Fallback si no se encuentra el material
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Material no encontrado", color = Color.White)
                    }
                }
            }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MARVICTheme {
        val navController = rememberNavController()
        AppNavHost(navController)
    }
}