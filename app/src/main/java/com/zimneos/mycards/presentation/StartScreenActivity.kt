package com.zimneos.mycards.presentation

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.zimneos.mycards.R
import kotlinx.android.synthetic.main.layout_start_screen.*


@Suppress("DEPRECATION")
class StartScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        window.statusBarColor = ContextCompat.getColor(this, R.color.light_dark)
        setContentView(R.layout.layout_start_screen)

        val animatedVectorDrawable = AppCompatResources.getDrawable(this, R.drawable.app_logo_avd)
                as AnimatedVectorDrawable
        logo.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable.start()
    }

    override fun onStart() {
        super.onStart()
        intent = Intent(this, MainActivity::class.java)
        Handler().postDelayed({
            startActivity(intent)
            finish()
        }, 1000)
    }
}
