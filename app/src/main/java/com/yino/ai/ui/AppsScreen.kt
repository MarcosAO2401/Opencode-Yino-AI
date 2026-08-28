package com.yino.ai.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AppsScreen(viewModel: YinoViewModel) {
    val context = LocalContext.current

    val apps = listOf(
        AppItems.SuggestedApp("WhatsApp", "com.whatsapp"),
        AppItems.SuggestedApp("Calendar", "com.google.android.calendar"),
        AppItems.SuggestedApp("Gmail", "com.google.android.gm"),
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Apps sugeridas", style = MaterialTheme.typography.headlineSmall)
        apps.forEach { app ->
            Button(
                onClick = {
                    val pm = context.packageManager
                    val launch = pm.getLaunchIntentForPackage(app.packageId)
                    if (launch != null) {
                        context.startActivity(launch)
                    } else {
                        val market = Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=${app.packageId}"),
                        )
                        context.startActivity(market)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(app.name)
            }
        }
    }
}

private object AppItems {
    data class SuggestedApp(val name: String, val packageId: String)
}

private typealias SuggestedApp = AppItems.SuggestedApp
