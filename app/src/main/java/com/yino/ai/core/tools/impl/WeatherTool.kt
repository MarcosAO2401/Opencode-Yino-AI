package com.yino.ai.core.tools.impl

import android.content.Context
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

class WeatherTool(private val context: Context) : Tool {
    override val id = "weather"
    override val description = "Obtiene clima actual sin abrir navegador. Parametros: location opcional."
    override val parametersJsonSchema = """{"type":"object","properties":{"location":{"type":"string"}}}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()
    private val client = HttpClient()
    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val location = arguments.optString("location", "auto").ifBlank { "auto" }
        return try {
            val city = if (location.equals("auto", true)) "Madrid" else location
            val encoded = java.net.URLEncoder.encode(city, "UTF-8")
            val text = client.get("https://wttr.in/$encoded?format=j1").bodyAsText()
            val json = JSONObject(text)
            val curr = json.getJSONArray("current_condition").getJSONObject(0)
            val temp = curr.optString("temp_C", "?")
            val feels = curr.optString("FeelsLikeC", "?")
            val humidity = curr.optString("humidity", "?")
            val desc = curr.optJSONArray("weatherDesc")?.getJSONObject(0)?.optString("value") ?: "?"
            val wind = curr.optString("windspeedKmph", "?")
            val msg = "Clima en $city: $temp°C (sensación $feels°C), $desc, humedad $humidity%, viento $wind km/h. Mostrado dentro de Yino, sin abrir navegador."
            ToolResult(true, msg)
        } catch (e: Exception) {
            ToolResult(false, "Error obteniendo clima: ${e.message}")
        }
    }
}