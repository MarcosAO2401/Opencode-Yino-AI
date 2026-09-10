package com.yino.ai.core.tools.impl

import android.content.Context
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import org.json.JSONObject

class WeatherTool(private val context: Context) : Tool {
    override val id = "weather"
    override val description = "Obtiene clima actual sin abrir navegador. Parametros: location opcional, si es auto detecta ubicacion."
    override val parametersJsonSchema = """{"type":"object","properties":{"location":{"type":"string"}}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()
    private val client = HttpClient()
    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val location = arguments.optString("location", "auto").ifBlank { "auto" }
        return try {
            val url = if (location.equals("auto", true)) "https://wttr.in/?format=j1" else {
                val encoded = java.net.URLEncoder.encode(location, "UTF-8")
                "https://wttr.in/$encoded?format=j1"
            }
            val text = client.get(url).bodyAsText()
            val json = JSONObject(text)
            val nearest = try { json.getJSONArray("nearest_area").getJSONObject(0) } catch (_: Exception) { null }
            val area = nearest?.getJSONArray("areaName")?.getJSONObject(0)?.optString("value") ?: location
            val country = nearest?.getJSONArray("country")?.getJSONObject(0)?.optString("value") ?: ""
            val curr = json.getJSONArray("current_condition").getJSONObject(0)
            val temp = curr.optString("temp_C", "?")
            val feels = curr.optString("FeelsLikeC", "?")
            val humidity = curr.optString("humidity", "?")
            val desc = curr.optJSONArray("weatherDesc")?.getJSONObject(0)?.optString("value") ?: "?"
            val wind = curr.optString("windspeedKmph", "?")
            val cityLabel = if (location.equals("auto", true) && area.isNotBlank()) "$area${if (country.isNotBlank()) ", $country" else ""}" else location
            val msg = "Clima en $cityLabel: $temp°C (sensación $feels°C), $desc, humedad $humidity%, viento $wind km/h."
            ToolResult(true, msg)
        } catch (e: Exception) {
            ToolResult(false, "Error obteniendo clima: ${e.message}")
        }
    }
}