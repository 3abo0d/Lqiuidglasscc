package com.liquidglass.cc

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import android.graphics.BlurMaskFilter

class GlassOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var panel: View? = null
    private val channel = "liquidglass_overlay"
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        val n = Notification.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("LiquidGlass CC is active")
            .setContentText("Tap to return to the app to close the panel")
            .setOngoing(true).build()
        startForeground(7, n)
        showPanel()
    }

    private fun glass(bg: Int = 0x553B465B): GradientDrawable =
        GradientDrawable().apply {
            setColor(bg)
            cornerRadius = dp(28).toFloat()
            setStroke(dp(1), 0x66FFFFFF)
        }

    private fun showPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(22))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xEE10131C.toInt(), 0xD910121A.toInt())
            ).apply { cornerRadius = dp(34).toFloat(); setStroke(dp(1), 0x77FFFFFF) }
        }
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        header.addView(TextView(this).apply {
            text = "3:24   •   Control Center"; textSize = 18f
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, dp(44), 1f))
        header.addView(Button(this).apply {
            text = "×"; setOnClickListener { stopSelf() }
        })
        root.addView(header)

        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        top.addView(card("Wi-Fi", "Open network controls") {
            startActivitySafe(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
        }, weight())
        top.addView(card("Bluetooth", "Connected devices") {
            startActivitySafe(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }, weight())
        root.addView(top)

        root.addView(card("Now Playing", "Media controls appear when audio is active") {}, full())
        val sliders = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        sliders.addView(sliderCard("Brightness", 0, 255) { value ->
            if (Settings.System.canWrite(this@GlassOverlayService))
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        }, weight())
        sliders.addView(sliderCard("Volume", 0, 100) { value ->
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, value * max / 100, 0)
        }, weight())
        root.addView(sliders)

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(card("Flashlight", "Toggle torch") { toggleTorch() }, weight())
        row1.addView(card("Airplane", "Open network settings") {
            startActivitySafe(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
        }, weight())
        root.addView(row1)
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(card("Location", "Location settings") {
            startActivitySafe(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }, weight())
        row2.addView(card("Auto-rotate", "Display settings") {
            startActivitySafe(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }, weight())
        root.addView(row2)
        root.addView(TextView(this).apply {
            text = "LIQUID GLASS  •  OPPO COLOROS"; textSize = 10f
            gravity = Gravity.CENTER; setTextColor(0x99FFFFFF.toInt())
            setPadding(0, dp(18), 0, 0)
        })

        val scroll = ScrollView(this).apply { addView(root) }
        panel = scroll
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else
            WindowManager.LayoutParams.TYPE_PHONE
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            dimAmount = 0.22f
            if (Build.VERSION.SDK_INT >= 31) {
                flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                setBlurBehindRadius(dp(42))
            }
        }
        try { wm.addView(scroll, p) } catch (_: Exception) { stopSelf() }
    }

    private fun card(title: String, sub: String, action: () -> Unit): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = glass()
            setPadding(dp(16), dp(14), dp(12), dp(14))
            isClickable = true; isFocusable = true; setOnClickListener { action() }
        }
        box.addView(TextView(this).apply {
            text = title; textSize = 17f; setTextColor(Color.WHITE)
        })
        box.addView(TextView(this).apply {
            text = sub; textSize = 11f; setTextColor(0xFFCFD5E0.toInt())
            setPadding(0, dp(5), 0, 0)
        })
        return box
    }
    private fun sliderCard(title: String, min: Int, max: Int, change: (Int) -> Unit): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = glass(0x66434C60)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        box.addView(TextView(this).apply { text = title; setTextColor(Color.WHITE) })
        box.addView(SeekBar(this).apply {
            this.max = max - min
            progress = if (title == "Brightness") try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (_: Exception) { 130 }
            else 35
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                    if (user) change(p + min)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })
        return box
    }
    private fun toggleTorch() {
        try {
            val cm = getSystemService(CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
            if (id != null) {
                val prefs = getSharedPreferences("torch", MODE_PRIVATE)
                val next = !prefs.getBoolean("on", false)
                cm.setTorchMode(id, next); prefs.edit().putBoolean("on", next).apply()
            }
        } catch (_: Exception) {}
    }
    private fun startActivitySafe(i: Intent) {
        try { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) } catch (_: Exception) {}
    }
    private fun weight() = LinearLayout.LayoutParams(0, dp(90), 1f).apply {
        setMargins(dp(4), dp(6), dp(4), dp(6))
    }
    private fun full() = LinearLayout.LayoutParams(-1, dp(86)).apply {
        setMargins(dp(4), dp(8), dp(4), dp(8))
    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(channel, "LiquidGlass overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
    }
    override fun onDestroy() {
        try { panel?.let { wm.removeView(it) } } catch (_: Exception) {}
        panel = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
