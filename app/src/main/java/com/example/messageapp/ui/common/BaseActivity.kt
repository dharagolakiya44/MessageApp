package com.example.messageapp.ui.common

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.messageapp.R
import com.example.messageapp.data.PreferenceHelper
import com.example.messageapp.ui.onboarding.ActivityManageSystemAlert
import com.example.messageapp.ui.home.MainActivity
import com.example.messageapp.utils.BetterActivityResult
import com.example.messageapp.utils.LocaleHelper
import com.example.messageapp.utils.isTiramisuPlusNew
import com.example.messageapp.utils.showToast

abstract class BaseActivity : AppCompatActivity() {

    open lateinit var activityLauncher: BetterActivityResult<Intent, ActivityResult>
    val GENERIC_PERM_HANDLER = 100
    val FULLSCREEN_INTENT_PERMISSION_REQ = 101
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false

    companion object {
        const val GENERIC_PERM_HANDLER = 101
        const val OVERLAY_PERMISSION_REQ_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        activityLauncher = BetterActivityResult.Companion.registerActivityForResult(this)
        setupStatusBar()

        applySystemBarInsets(R.color.colorBgMain, !isNightModeActive())
    }

    fun isNightModeActive(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun applySystemBarInsets(statusBarColorResId: Int, lightStatusBar: Boolean) {
        val window: Window = window

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        val decorView: View = window.decorView
        val controller = WindowInsetsControllerCompat(window, decorView)
        controller.isAppearanceLightStatusBars = lightStatusBar

        val root: FrameLayout = findViewById(android.R.id.content)
        root.setBackgroundColor(
            ContextCompat.getColor(this, statusBarColorResId)
        )

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime: Insets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val isKeyboardVisible = ime.bottom > 0

            // If keyboard is open → don't paint bottom padding area
            if (isKeyboardVisible) {
                v.setPadding(sysBars.left, sysBars.top, sysBars.right, 0)
            } else {
                v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            }

            insets
        }
    }

    private fun setupStatusBar() {
        // Set status bar color to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlackCommon)

        // Ensure status bar icons are light (white) for better visibility on black background
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
    }

    fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        // Smooth transition animation (optional)
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        // Kill current instance (optional)
        finishAffinity()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceHelper.getLanguageCode(context = newBase)
        val localizedContext = LocaleHelper.setLocale(newBase, lang)
        val fontScaledContext = com.example.messageapp.utils.FontSizeHelper.applyFontScale(localizedContext)
        super.attachBaseContext(fontScaledContext)
    }

    fun openPrivacyPolicy() {
        try {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://nexios.in/alarm/privacy-policy"))
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            showToast(getString(R.string.no_app_available_to_open_privacy_policy_url))
        }
    }

    fun handleFullScreenIntentPermission(callback: (granted: Boolean) -> Unit) {

        if (!isTiramisuPlusNew()) {
            callback(true)
            return
        }

        val permissionStr = Manifest.permission.USE_FULL_SCREEN_INTENT

        if (ContextCompat.checkSelfPermission(this, permissionStr)
            == PackageManager.PERMISSION_GRANTED
        ) {
            callback(true)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissionStr),
                FULLSCREEN_INTENT_PERMISSION_REQ
            )
        }
    }

    fun handleNotificationPermission(callback: (granted: Boolean) -> Unit) {
        if (!isTiramisuPlusNew()) {
            callback(true)
        } else {
            handlePermission { granted ->
                callback(granted)
            }
        }
    }

    fun handlePermission(callback: (granted: Boolean) -> Unit) {
        val permissionStr = Manifest.permission.POST_NOTIFICATIONS

        if (ContextCompat.checkSelfPermission(
                this,
                permissionStr
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(this, arrayOf(permissionStr), GENERIC_PERM_HANDLER)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false

        if (requestCode == GENERIC_PERM_HANDLER || requestCode == FULLSCREEN_INTENT_PERMISSION_REQ) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                actionOnPermission?.invoke(true)
            } else {
                // Permission denied
                if (permissions.isNotEmpty() &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                ) {
                    // "Don’t ask again" → open settings
                    showSettingsDialog(this@BaseActivity)
                } else {
                    actionOnPermission?.invoke(false)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Settings.canDrawOverlays(this)) {
                actionOnPermission?.invoke(true)
            } else {
                // Open your custom permission guidance screen instead of dialog
                val hintIntent = Intent(this, ActivityManageSystemAlert::class.java).apply {
                    putExtra("PERMISSION_TYPE", "overlay")
                }
                startActivity(hintIntent)

                actionOnPermission?.invoke(false)
            }
        }
    }

    fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.this_permission_is_required_for_the_app_to_function_properly_please_enable_it_in_the_app_settings))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                onSettingsPermissionCancel()
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.go_to_setting)) { dialog, _ ->

                isAskingPermissions = true
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)

            }
            .show()
    }

    open fun onSettingsPermissionCancel(){

    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isInternetAvailable(videoConferenceActivity: Activity): Boolean {
        var checkInternetBoolean = false
        val connectivityManager =
            videoConferenceActivity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                checkInternetBoolean =
                    networkCapabilities != null && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                checkInternetBoolean = networkInfo != null && networkInfo.isConnectedOrConnecting
            }
        } catch (videoConferenceException: Exception) {
            videoConferenceException.message
        }
        return checkInternetBoolean
    }

}