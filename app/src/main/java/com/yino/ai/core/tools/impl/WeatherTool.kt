package com.yino.ai.core.tools.impl

import android.content.Context
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runCatching

class WeatherTool(private val context: Context) : Tool {

    override val id = "weather"
    override val description = "Obtiene el clima actual de una ubicación. Parámetros: location (opcional, ej. 'Madrid', 'Barcelona', 'auto' para ubicación automática)"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"location":{"type":"string"}},"required":[]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    private val client = HttpClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class WeatherApiResponse(
        val main: Main,
        val weather: List<Weather>,
        val name: String,
        val sys: Sys,
        val wind: Wind
    )

    @Serializable
    data class Main(
        val temp: Double,
        val feels_like: Double,
        val humidity: Int,
        val pressure: Int
    )

    @Serializable
    data class Weather(
        val main: String,
        val description: String,
        val icon: String
    )

    @Serializable
    data class Sys(
        val country: String,
        val sunrise: Long,
        val sunset: Long
    )

    @Serializable
    data class Wind(
        val speed: Double,
        val deg: Int
    )

    override suspend fun execute(arguments: org.json.JSONObject, ctx: ToolContext): ToolResult {
        val location = arguments.optString("location", "auto")
        
        return withContext(Dispatchers.IO) {
            try {
                val city = when (location.lowercase()) {
                    "auto", "" -> getCityFromIP()
                    else -> location
                }
                
                val weatherData = fetchWeather(city)
                formatWeatherResponse(weatherData)
            } catch (e: Exception) {
                ToolResult(false, "Error obteniendo clima: ${e.message}")
            }
        }
    }

    private suspend fun getCityFromIP(): String = withContext(Dispatchers.IO) {
        try {
            val response = client.get<String>("http://ip-api.com/json/")
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<IpApiResponse>(response)
            json.city.ifBlank { "Madrid" }
        } catch (e: Exception) {
            "Madrid"
        }
    }

    @Serializable
    data class IpApiResponse(
        val city: String = "",
        val country: String = "",
        val lat: Double = 0.0,
        val lon: Double = 0.0
    )

    private suspend fun fetchWeather(city: String): WeatherApiResponse = withContext(Dispatchers.IO) {
        // Usando OpenWeatherMap (requiere API key) o alternativa gratuita
        // Usamos wttr.in que es gratuito y no requiere API key
        val response = client.get<String>("https://wttr.in/${city.urlEncode()}?format=j1")
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<WttrResponse>(response)
        
        // Convertir wttr.in response a nuestro formato
        val current = json.current_condition.firstOrNull()
        val main = Main(
            temp = current?.temp_C?.toDouble() ?: 0.0,
            feels_like = current?.FeelsLikeC?.toDouble() ?: 0.0,
            humidity = current?.humidity?.toInt() ?: 0,
            pressure = current?.pressure?.toInt() ?: 0
        )
        val weather = listOf(Weather(
            main = current?.weatherDesc?.firstOrNull()?.value ?: "Desconocido",
            description = current?.weatherDesc?.firstOrNull()?.value ?: "Sin descripción",
            icon = ""
        ))
        val wind = Wind(
            speed = current?.windspeedKmph?.toDouble() ?: 0.0,
            deg = current?.winddirDegree?.toInt() ?: 0
        )
        val sys = Sys(country = "", sunrise = 0, sunset = 0)
        
        WeatherApiResponse(main, weather, city, sys, wind)
    }

    @Serializable
    data class WttrResponse(
        val current_condition: List<WttrCurrent>
    )

    @Serializable
    data class WttrCurrent(
        val temp_C: String? = null,
        val FeelsLikeC: String? = null,
        val humidity: String? = null,
        val pressure: String? = null,
        val weatherDesc: List<WttrDesc>? = null,
        val windspeedKmph: String? = null,
        val winddirDegree: String? = null
    )

    @Serializable
    data class WttrDesc(
        val value: String = ""
    )

    private fun formatWeatherResponse(weather: WeatherApiResponse): ToolResult {
        val temp = weather.main.temp.toInt()
        val feelsLike = weather.main.feels_like.toInt()
        val condition = weather.weather.firstOrNull()?.description ?: "Desconocido"
        val humidity = weather.main.humidity
        val windSpeed = weather.wind.speed.toInt()
        val cityName = weather.name
        val country = weather.sys.country
        
        val message = StringBuilder()
        message.appendLine("🌤️ Clima en $cityName${if (country.isNotBlank()) ", ${country}" else ""}")
        message.appendLine("🌡️ Temperatura: ${temp}°C (sensación $feelsLike°)")
        message.appendLine("☁️ Estado: $condition")
        message.appendLine("💧 Humedad: ${humidity}%")
        message.appendLine("💨 Viento: ${windSpeed} km/h")
        
        return ToolResult(true, message.toString())
    }
}