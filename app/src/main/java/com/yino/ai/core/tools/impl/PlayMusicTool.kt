package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

/**
 * Reproduce música buscando en Spotify (si está instalado); si no, abre
 * YouTube con la búsqueda. Funciona con una consulta en lenguaje natural.
 */
class PlayMusicTool(private val context: Context) : Tool {
    override val id = "play_music"
    override val description = "Reproduce música desde una búsqueda (artista, canción o género). Ej: play_music{query:\"Bad Bunny\"}."
    override val parametersJsonSchema =
        """{"type":"object","properties":{"query":{"type":"string"}},"required":["query"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val q = arguments.optString("query")
        if (q.isBlank()) return ToolResult(false, "query requerida")
        return try {
            val spotify = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$q"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (spotify.resolveActivity(context.packageManager) != null) {
                context.startActivity(spotify)
                ToolResult(true, "Buscando \"$q\" en Spotify")
            } else {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(q)}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                ToolResult(true, "Spotify no instalado; buscando \"$q\" en YouTube")
            }
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error al reproducir")
        }
    }
}
