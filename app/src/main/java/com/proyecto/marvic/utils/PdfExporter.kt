package com.proyecto.marvic.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.proyecto.marvic.data.MaterialItem
import com.proyecto.marvic.data.Provider
import com.proyecto.marvic.data.Project
import com.proyecto.marvic.data.Movement
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val dateFormatFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    // Colores corporativos
    private val colorPrimary = Color.parseColor("#FFA726")
    private val colorSecondary = Color.parseColor("#1A1A1A")
    private val colorText = Color.parseColor("#333333")
    private val colorTextLight = Color.parseColor("#666666")
    
    /**
     * Exporta el inventario a PDF
     */
    fun exportInventoryToPdf(materials: List<MaterialItem>): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width
            val pageHeight = 842 // A4 height
            val margin = 40f
            
            var pageNumber = 1
            var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = currentPage.canvas
            var yPosition = margin
            
            // Título
            yPosition = drawHeader(canvas, "REPORTE DE INVENTARIO", yPosition, pageWidth, margin)
            yPosition += 20f
            
            // Información general
            val titlePaint = Paint().apply {
                color = colorText
                textSize = 12f
                isFakeBoldText = true
            }
            val normalPaint = Paint().apply {
                color = colorText
                textSize = 10f
            }
            
            canvas.drawText("Fecha: ${dateFormat.format(Date())}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Total de materiales: ${materials.size}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Stock total: ${materials.sumOf { it.cantidad.toInt() }} unidades", margin, yPosition, normalPaint)
            yPosition += 30f
            
            // Encabezados de tabla
            yPosition = drawTableHeader(canvas, yPosition, margin, pageWidth)
            yPosition += 5f
            
            // Contenido
            materials.forEachIndexed { index, material ->
                // Verificar si necesitamos una nueva página
                if (yPosition > pageHeight - 100) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = currentPage.canvas
                    yPosition = margin
                    yPosition = drawHeader(canvas, "REPORTE DE INVENTARIO (Cont.)", yPosition, pageWidth, margin)
                    yPosition += 20f
                    yPosition = drawTableHeader(canvas, yPosition, margin, pageWidth)
                    yPosition += 5f
                }
                
                // Alternar color de fondo
                if (index % 2 == 0) {
                    val bgPaint = Paint().apply {
                        color = Color.parseColor("#F5F5F5")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(margin, yPosition - 12, pageWidth - margin, yPosition + 8, bgPaint)
                }
                
                // Dibujar fila
                canvas.drawText(material.codigo.take(10), margin + 5, yPosition, normalPaint)
                canvas.drawText(material.nombre.take(25), margin + 80, yPosition, normalPaint)
                canvas.drawText(material.categoria.take(15), margin + 250, yPosition, normalPaint)
                canvas.drawText(material.cantidad.toString(), margin + 350, yPosition, normalPaint)
                canvas.drawText("S/. ${String.format("%.2f", material.precioUnitario)}", margin + 420, yPosition, normalPaint)
                
                yPosition += 20f
            }
            
            // Footer
            yPosition = pageHeight - 40f
            val footerPaint = Paint().apply {
                color = colorTextLight
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Grupo Marvic - Sistema de Gestión de Inventario", pageWidth / 2f, yPosition, footerPaint)
            canvas.drawText("Página $pageNumber", pageWidth / 2f, yPosition + 15, footerPaint)
            
            pdfDocument.finishPage(currentPage)
            
            // Guardar archivo
            val fileName = "Inventario_${dateFormatFile.format(Date())}.pdf"
            val filePath = savePdfToFile(pdfDocument, fileName)
            pdfDocument.close()
            
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Exporta movimientos a PDF
     */
    fun exportMovementsToPdf(movements: List<Movement>): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            
            var pageNumber = 1
            var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = currentPage.canvas
            var yPosition = margin
            
            // Título
            yPosition = drawHeader(canvas, "REPORTE DE MOVIMIENTOS", yPosition, pageWidth, margin)
            yPosition += 20f
            
            val normalPaint = Paint().apply {
                color = colorText
                textSize = 10f
            }
            
            canvas.drawText("Fecha: ${dateFormat.format(Date())}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Total de movimientos: ${movements.size}", margin, yPosition, normalPaint)
            yPosition += 30f
            
            // Encabezados de tabla de movimientos
            yPosition = drawMovementsTableHeader(canvas, yPosition, margin, pageWidth)
            yPosition += 5f
            
            // Contenido
            movements.sortedByDescending { it.timestamp }.forEachIndexed { index, movement ->
                if (yPosition > pageHeight - 100) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = currentPage.canvas
                    yPosition = margin
                    yPosition = drawHeader(canvas, "REPORTE DE MOVIMIENTOS (Cont.)", yPosition, pageWidth, margin)
                    yPosition += 20f
                    yPosition = drawMovementsTableHeader(canvas, yPosition, margin, pageWidth)
                    yPosition += 5f
                }
                
                if (index % 2 == 0) {
                    val bgPaint = Paint().apply {
                        color = Color.parseColor("#F5F5F5")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(margin, yPosition - 12, pageWidth - margin, yPosition + 8, bgPaint)
                }
                
                val type = if (movement.delta > 0) "Ingreso" else "Salida"
                val typeColor = if (movement.delta > 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                val typePaint = Paint().apply {
                    color = typeColor
                    textSize = 10f
                    isFakeBoldText = true
                }
                
                canvas.drawText(dateFormat.format(Date(movement.timestamp)), margin + 5, yPosition, normalPaint)
                canvas.drawText(type, margin + 120, yPosition, typePaint)
                canvas.drawText(movement.materialId.take(20), margin + 200, yPosition, normalPaint)
                canvas.drawText(kotlin.math.abs(movement.delta).toString(), margin + 380, yPosition, normalPaint)
                canvas.drawText(movement.userId?.take(15) ?: "Sistema", margin + 440, yPosition, normalPaint)
                
                yPosition += 20f
            }
            
            // Footer
            yPosition = pageHeight - 40f
            val footerPaint = Paint().apply {
                color = colorTextLight
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Grupo Marvic - Sistema de Gestión de Inventario", pageWidth / 2f, yPosition, footerPaint)
            canvas.drawText("Página $pageNumber", pageWidth / 2f, yPosition + 15, footerPaint)
            
            pdfDocument.finishPage(currentPage)
            
            val fileName = "Movimientos_${dateFormatFile.format(Date())}.pdf"
            val filePath = savePdfToFile(pdfDocument, fileName)
            pdfDocument.close()
            
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Exporta proveedores a PDF
     */
    fun exportProvidersToPdf(providers: List<Provider>): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            
            var pageNumber = 1
            var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = currentPage.canvas
            var yPosition = margin
            
            yPosition = drawHeader(canvas, "CATÁLOGO DE PROVEEDORES", yPosition, pageWidth, margin)
            yPosition += 20f
            
            val normalPaint = Paint().apply {
                color = colorText
                textSize = 10f
            }
            
            canvas.drawText("Fecha: ${dateFormat.format(Date())}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Total de proveedores: ${providers.size}", margin, yPosition, normalPaint)
            yPosition += 30f
            
            providers.forEach { provider ->
                if (yPosition > pageHeight - 150) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = currentPage.canvas
                    yPosition = margin
                    yPosition = drawHeader(canvas, "CATÁLOGO DE PROVEEDORES (Cont.)", yPosition, pageWidth, margin)
                    yPosition += 30f
                }
                
                // Card del proveedor
                val cardPaint = Paint().apply {
                    color = Color.parseColor("#F5F5F5")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(margin, yPosition - 10, pageWidth - margin, yPosition + 90, cardPaint)
                
                val borderPaint = Paint().apply {
                    color = colorPrimary
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRect(margin, yPosition - 10, pageWidth - margin, yPosition + 90, borderPaint)
                
                val titlePaint = Paint().apply {
                    color = colorText
                    textSize = 12f
                    isFakeBoldText = true
                }
                
                canvas.drawText(provider.nombre, margin + 10, yPosition + 10, titlePaint)
                canvas.drawText("RUC: ${provider.ruc}", margin + 10, yPosition + 30, normalPaint)
                canvas.drawText("Contacto: ${provider.contactoPrincipal}", margin + 10, yPosition + 45, normalPaint)
                canvas.drawText("Email: ${provider.email}", margin + 10, yPosition + 60, normalPaint)
                canvas.drawText("Calificación: ${"⭐".repeat(provider.calificacion.toInt())} (${String.format("%.1f", provider.calificacion)}/5.0)", margin + 10, yPosition + 75, normalPaint)
                
                yPosition += 110f
            }
            
            val fileName = "Proveedores_${dateFormatFile.format(Date())}.pdf"
            val filePath = savePdfToFile(pdfDocument, fileName)
            pdfDocument.close()
            
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Exporta proyectos a PDF
     */
    fun exportProjectsToPdf(projects: List<Project>): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f
            
            var pageNumber = 1
            var currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = currentPage.canvas
            var yPosition = margin
            
            yPosition = drawHeader(canvas, "PORTAFOLIO DE PROYECTOS", yPosition, pageWidth, margin)
            yPosition += 20f
            
            val normalPaint = Paint().apply {
                color = colorText
                textSize = 10f
            }
            
            canvas.drawText("Fecha: ${dateFormat.format(Date())}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Total de proyectos: ${projects.size}", margin, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("Proyectos activos: ${projects.count { it.estado != "FINALIZADO" }}", margin, yPosition, normalPaint)
            yPosition += 30f
            
            projects.forEach { project ->
                if (yPosition > pageHeight - 180) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = currentPage.canvas
                    yPosition = margin
                    yPosition = drawHeader(canvas, "PORTAFOLIO DE PROYECTOS (Cont.)", yPosition, pageWidth, margin)
                    yPosition += 30f
                }
                
                // Card del proyecto
                val statusColor = when (project.estado) {
                    "EN_CURSO" -> Color.parseColor("#4CAF50")
                    "FINALIZADO" -> Color.parseColor("#2196F3")
                    "PAUSADO" -> Color.parseColor("#FF9800")
                    else -> Color.parseColor("#9E9E9E")
                }
                
                val cardPaint = Paint().apply {
                    color = Color.parseColor("#F5F5F5")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(margin, yPosition - 10, pageWidth - margin, yPosition + 130, cardPaint)
                
                val borderPaint = Paint().apply {
                    color = statusColor
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRect(margin, yPosition - 10, pageWidth - margin, yPosition + 130, borderPaint)
                
                val titlePaint = Paint().apply {
                    color = colorText
                    textSize = 12f
                    isFakeBoldText = true
                }
                
                canvas.drawText("[${project.codigo}] ${project.nombre}", margin + 10, yPosition + 10, titlePaint)
                canvas.drawText("Cliente: ${project.cliente}", margin + 10, yPosition + 30, normalPaint)
                canvas.drawText("Estado: ${project.estado.replace("_", " ")}", margin + 10, yPosition + 45, normalPaint)
                canvas.drawText("Responsable: ${project.responsable}", margin + 10, yPosition + 60, normalPaint)
                canvas.drawText("Presupuesto: S/. ${String.format("%.2f", project.presupuesto)}", margin + 10, yPosition + 75, normalPaint)
                canvas.drawText("Gastado: S/. ${String.format("%.2f", project.gastoReal)}", margin + 10, yPosition + 90, normalPaint)
                canvas.drawText("Avance: ${project.porcentajeAvance}%", margin + 10, yPosition + 105, normalPaint)
                
                // Barra de progreso
                val progressBarWidth = 200f
                val progressBarHeight = 10f
                val progressX = margin + 250
                val progressY = yPosition + 100
                
                val bgProgressPaint = Paint().apply {
                    color = Color.parseColor("#E0E0E0")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(progressX, progressY, progressX + progressBarWidth, progressY + progressBarHeight, bgProgressPaint)
                
                val progressPaint = Paint().apply {
                    color = colorPrimary
                    style = Paint.Style.FILL
                }
                val progressWidth = (progressBarWidth * project.porcentajeAvance) / 100
                canvas.drawRect(progressX, progressY, progressX + progressWidth, progressY + progressBarHeight, progressPaint)
                
                yPosition += 150f
            }
            
            val fileName = "Proyectos_${dateFormatFile.format(Date())}.pdf"
            val filePath = savePdfToFile(pdfDocument, fileName)
            pdfDocument.close()
            
            Result.success(filePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Funciones auxiliares
    
    private fun drawHeader(canvas: Canvas, title: String, yPosition: Float, pageWidth: Int, margin: Float): Float {
        var y = yPosition
        
        // Fondo del header
        val headerPaint = Paint().apply {
            color = colorSecondary
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, y, pageWidth - margin, y + 50, headerPaint)
        
        // Logo/Título
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("GRUPO MARVIC", margin + 10, y + 25, titlePaint)
        
        // Subtítulo
        val subtitlePaint = Paint().apply {
            color = colorPrimary
            textSize = 14f
            isFakeBoldText = true
        }
        canvas.drawText(title, margin + 10, y + 42, subtitlePaint)
        
        return y + 60
    }
    
    private fun drawTableHeader(canvas: Canvas, yPosition: Float, margin: Float, pageWidth: Int): Float {
        val headerBgPaint = Paint().apply {
            color = colorSecondary
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, yPosition - 12, pageWidth - margin, yPosition + 8, headerBgPaint)
        
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
        }
        
        canvas.drawText("CÓDIGO", margin + 5, yPosition, headerTextPaint)
        canvas.drawText("NOMBRE", margin + 80, yPosition, headerTextPaint)
        canvas.drawText("CATEGORÍA", margin + 250, yPosition, headerTextPaint)
        canvas.drawText("STOCK", margin + 350, yPosition, headerTextPaint)
        canvas.drawText("PRECIO", margin + 420, yPosition, headerTextPaint)
        
        return yPosition + 20
    }
    
    private fun drawMovementsTableHeader(canvas: Canvas, yPosition: Float, margin: Float, pageWidth: Int): Float {
        val headerBgPaint = Paint().apply {
            color = colorSecondary
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, yPosition - 12, pageWidth - margin, yPosition + 8, headerBgPaint)
        
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
        }
        
        canvas.drawText("FECHA", margin + 5, yPosition, headerTextPaint)
        canvas.drawText("TIPO", margin + 120, yPosition, headerTextPaint)
        canvas.drawText("MATERIAL", margin + 200, yPosition, headerTextPaint)
        canvas.drawText("CANTIDAD", margin + 380, yPosition, headerTextPaint)
        canvas.drawText("USUARIO", margin + 440, yPosition, headerTextPaint)
        
        return yPosition + 20
    }
    
    private fun savePdfToFile(pdfDocument: PdfDocument, fileName: String): String {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Marvic"
        )
        
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        
        return file.absolutePath
    }
}

