package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreProjectRepository
import com.proyecto.marvic.data.Project
import com.proyecto.marvic.data.ProjectMaterial
import com.proyecto.marvic.data.ProjectActivity
import com.proyecto.marvic.data.ProjectRepository
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val repo: ProjectRepository = FirestoreProjectRepository()
) : ViewModel() {
    
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    val projects = mutableStateListOf<Project>()
    val projectMaterials = mutableStateListOf<ProjectMaterial>()
    val projectActivities = mutableStateListOf<ProjectActivity>()
    
    var selectedProject by mutableStateOf<Project?>(null)
        private set
    var projectCosts by mutableStateOf<Map<String, Double>>(emptyMap())
        private set
    var projectProgress by mutableStateOf(0)
        private set
    
    fun loadProjects() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getAllProjects()
            isLoading = false
            if (result.isSuccess) {
                projects.clear()
                projects.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun loadActiveProjects() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getActiveProjects()
            isLoading = false
            if (result.isSuccess) {
                projects.clear()
                projects.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun searchProjects(query: String) {
        if (query.isEmpty()) {
            loadProjects()
            return
        }
        
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.searchProjects(query)
            isLoading = false
            if (result.isSuccess) {
                projects.clear()
                projects.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun createProject(project: Project, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.createProject(project)
            isLoading = false
            if (result.isSuccess) {
                loadProjects()
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun updateProject(project: Project, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.updateProject(project)
            isLoading = false
            if (result.isSuccess) {
                selectedProject = project
                loadProjects()
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun deleteProject(projectId: String, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.deleteProject(projectId)
            isLoading = false
            if (result.isSuccess) {
                projects.removeIf { it.id == projectId }
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun selectProject(project: Project) {
        selectedProject = project
        loadProjectDetails(project.id)
    }
    
    fun loadProjectDetails(projectId: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            // Cargar materiales
            val materialsResult = repo.getProjectMaterials(projectId)
            if (materialsResult.isSuccess) {
                projectMaterials.clear()
                projectMaterials.addAll(materialsResult.getOrDefault(emptyList()))
            }
            
            // Cargar actividades
            val activitiesResult = repo.getProjectActivities(projectId)
            if (activitiesResult.isSuccess) {
                projectActivities.clear()
                projectActivities.addAll(activitiesResult.getOrDefault(emptyList()))
            }
            
            // Cargar costos
            val costsResult = repo.getProjectCosts(projectId)
            if (costsResult.isSuccess) {
                projectCosts = costsResult.getOrDefault(emptyMap())
            }
            
            // Cargar progreso
            val progressResult = repo.getProjectProgress(projectId)
            if (progressResult.isSuccess) {
                projectProgress = progressResult.getOrDefault(0)
            }
            
            isLoading = false
        }
    }
    
    fun assignMaterial(projectMaterial: ProjectMaterial, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.assignMaterialToProject(projectMaterial)
            isLoading = false
            if (result.isSuccess) {
                selectedProject?.let { loadProjectDetails(it.id) }
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun useMaterial(
        projectId: String,
        materialId: String,
        cantidad: Int,
        userId: String,
        onComplete: (Boolean) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.useMaterial(projectId, materialId, cantidad, userId)
            isLoading = false
            if (result.isSuccess) {
                loadProjectDetails(projectId)
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun getProjectsByStatus(status: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getProjectsByStatus(status)
            isLoading = false
            if (result.isSuccess) {
                projects.clear()
                projects.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
}



