package com.liquidglass.cc

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 14, 20)
        window.navigationBarColor = Color.BLACK

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(24))
            setBackgroundColor(Color.rgb(12, 14, 20))
        }
        page.addView(TextView(this).apply {
            text = "LiquidGlass CC"
            textSize = 28f; setTextColor(Color.WHITE)
        })
        page.addView(TextView(this).apply {
            text = "A glass-style quick panel for ColorOS 16"
            textSize = 15f; setTextColor(0xFFB9C1D0.toInt())
            setPadding(0, dp(8), 0, dp(28))
        })
        val start = Button(this).apply {
            text = "Enable overlay permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            }
        }
        page.addView(start, match())
        page.addView(Button(this).apply {
            text = "Allow modify system settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName")))
            }
        }, match())
        page.addView(Button(this).apply {
            text = "Launch Liquid Glass Control Center"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                } else {
                    startForegroundService(Intent(this@MainActivity, GlassOverlayService::class.java))
                    finish()
                }
            }
        }, match())
        page.addView(TextView(this).apply {
            text = "Alpha build: the panel is an independent overlay. Some system switches open Android settings because Android restricts direct third-party control."
            textSize = 13f; setTextColor(0xFF929BAA.toInt())
            setPadding(0, dp(24), 0, 0)
        })
        setContentView(page)
    }
    private fun match() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { bottomMargin = dp(12) }
}
