package com.zimneos.mycards.presentation

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.zimneos.mycards.R
import kotlinx.android.synthetic.main.layout_start_screen.logo


class StartScreenActivity : AppCompatActivity(), BiometricAuthListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_dark)
        setContentView(R.layout.layout_start_screen)

        splashScreenLogoAnimation()
        checkBiometricIsSupported()

    }

    private fun checkBiometricIsSupported() {
        if (BiometricUtils.isBiometricReady(this)) {
            BiometricUtils.showBiometricPrompt(
                activity = this,
                listener = this,
                cryptoObject = null
            )
        } else launchMainScreen()
    }

    private fun splashScreenLogoAnimation() {
        val animatedVectorDrawable = AppCompatResources.getDrawable(this, R.drawable.app_logo_avd)
                as AnimatedVectorDrawable
        logo.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable.start()
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        this.finishAffinity()
    }

    override fun onBiometricAuthenticateSuccess(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        launchMainScreen()
    }

    private fun launchMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 500)
    }
}
