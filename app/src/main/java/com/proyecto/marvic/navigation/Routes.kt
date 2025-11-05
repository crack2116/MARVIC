package com.proyecto.marvic.navigation

sealed class Routes(val route: String) {
    data object RoleSelection : Routes("role_selection")
    data object Login : Routes("login")
    data object Dashboard : Routes("dashboard")
    data object SmartDashboard : Routes("smart_dashboard")
    data object Movement : Routes("movement")
    data object Search : Routes("search")
    data object AdvancedSearch : Routes("advanced_search")
    data object Scanner : Routes("scanner")
    data object Reports : Routes("reports")
    data object ExecutiveReports : Routes("executive_reports")
    data object NotificationSettings : Routes("notification_settings")
    data object UserManagement : Routes("user_management")
    data object Providers : Routes("providers")
    data object Projects : Routes("projects")
    data object Transfers : Routes("transfers")
    data object Analytics : Routes("analytics")
    data object Profile : Routes("profile")
    data object MaterialGallery : Routes("material_gallery/{materialId}") {
        fun createRoute(materialId: String) = "material_gallery/$materialId"
    }
}


