package com.aistra.hail.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aistra.hail.R
import com.aistra.hail.app.HailData
import com.aistra.hail.databinding.ActivityMainBinding
import com.aistra.hail.extensions.*
import com.aistra.hail.utils.HPolicy
import com.aistra.hail.utils.HUI
import com.aistra.hail.utils.HShortcuts
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    lateinit var fab: ExtendedFloatingActionButton
    lateinit var appbar: AppBarLayout

    private val access by viewModels<MainAccessState>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var biometricPrompt: BiometricPrompt
    var biometricLoginEnabled by mutableStateOf(HailData.biometricLogin)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = initView()
        binding.root.isVisible = !biometricLoginEnabled || access.unlocked
        // Register on every recreation, so the retained system prompt has the new activity callback.
        biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!access.authenticating) return
                    access.failed()
                    HUI.showToast(errString)
                    if (HailData.biometricLogin && !access.unlocked) finishAndRemoveTask()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (!access.authenticating) return
                    val setting = access.authenticated()
                    if (setting != null) {
                        HailData.setBiometricLogin(setting)
                        biometricLoginEnabled = setting
                        HShortcuts.removeAllDynamicShortcuts()
                        HShortcuts.addDynamicShortcutAction(HailData.dynamicShortcutAction)
                    }
                    binding.root.isVisible = true
                }
            })
    }

    override fun onStart() {
        super.onStart()
        biometricLoginEnabled = HailData.biometricLogin
        if (biometricLoginEnabled && !access.unlocked) {
            binding.root.isVisible = false
            authenticate()
        }
    }

    override fun onStop() {
        if (!isChangingConfigurations && HailData.biometricLogin) {
            access.background()
            binding.root.isVisible = false
        }
        super.onStop()
    }

    fun requestBiometricSetting(enabled: Boolean) {
        if (enabled != biometricLoginEnabled) authenticate(enabled)
    }

    private fun authenticate(setting: Boolean? = null) {
        if (access.authenticating) return
        // Accept system-supported face/fingerprint authentication and the lock-screen credential.
        val authenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            // Never turn an unavailable authenticator into an unlocked session.
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_biometric)
                .setMessage(R.string.biometric_unavailable)
                .setCancelable(false)
                .setPositiveButton(R.string.open_security_settings) { _, _ ->
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    if (HailData.biometricLogin && !access.unlocked) finishAndRemoveTask()
                }.show()
            return
        }
        if (!access.begin(setting)) return
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.action_biometric))
            .setSubtitle(getString(R.string.msg_biometric))
            .setAllowedAuthenticators(authenticators)
            .build()
        biometricPrompt.authenticate(info)
    }

    private fun initView() = ActivityMainBinding.inflate(layoutInflater).apply {
        setContentView(root)
        setSupportActionBar(appBarMain.toolbar)
        fab = appBarMain.fab
        appbar = appBarMain.appBarLayout

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(this@MainActivity)
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_apps, R.id.nav_settings
        ).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNav?.setupWithNavController(navController)
        navRail?.setupWithNavController(navController)

        val isRtl = isRtl
        val isLandscape = isLandscape
        appBarMain.appBarLayout.applyDefaultInsetter {
            paddingRelative(isRtl, start = !isLandscape, end = true, top = true)
        }
        bottomNav?.applyDefaultInsetter { paddingRelative(isRtl, start = true, end = true, bottom = true) }
        navRail?.applyDefaultInsetter { paddingRelative(isRtl, start = true, top = true, bottom = true) }
        fab.applyDefaultInsetter { marginRelative(isRtl, end = true, bottom = isLandscape) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let { MenuCompat.setGroupDividerEnabled(it, true) }
        return super.onCreateOptionsMenu(menu)
    }

    fun ownerRemoveDialog() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.title_remove_owner).setMessage(R.string.msg_remove_owner)
            .setPositiveButton(R.string.action_continue) { _, _ ->
                HPolicy.setOrganizationName()
                HPolicy.removeDeviceOwner()
            }.setNegativeButton(android.R.string.cancel, null).show()
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        fab.tag = destination.id == R.id.nav_home
        if (fab.tag == true) fab.show() else fab.hide()
    }
}
