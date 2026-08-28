package com.yino.ai.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yino.ai.core.tools.ActionRisk
import com.yino.ai.core.tools.Tool
import com.yino.ai.core.tools.ToolContext
import com.yino.ai.core.tools.ToolResult
import org.json.JSONObject

class WebSearchTool(private val context: Context) : Tool {
    override val id = "web_search"
    override val description = "Abre una búsqueda web en el navegador"
    override val parametersJsonSchema =
        """{"type":"object","properties":{"query":{"type":"string"}},"required":["query"]}"""
    override val risk = ActionRisk.LOW
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(arguments: JSONObject, ctx: ToolContext): ToolResult {
        val q = arguments.optString("query")
        if (q.isBlank()) return ToolResult(false, "query requerido")
        return try {
            val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(q))
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult(true, "Buscando: $q")
        } catch (e: Exception) {
            ToolResult(false, e.message ?: "error")
        }
    }
}
