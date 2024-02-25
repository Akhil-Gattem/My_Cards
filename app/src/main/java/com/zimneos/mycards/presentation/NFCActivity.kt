package com.zimneos.mycards.presentation

import android.animation.ObjectAnimator
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.devnied.emvnfccard.parser.EmvTemplate
import com.zimneos.mycards.R
import kotlinx.android.synthetic.main.layout_nfc_screen.bg
import kotlinx.android.synthetic.main.layout_nfc_screen.bg_gradient
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId


private var mNfcAdapter: NfcAdapter? = null
private lateinit var vibratorManager: VibratorManager
lateinit var vibration: Vibrator
private val handler = Handler()

@RequiresApi(Build.VERSION_CODES.S)
class NFCActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibratorManager = this.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibration = vibratorManager.defaultVibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
        }
        setContentView(R.layout.layout_nfc_screen)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        checkNfc()
        startVibration()
        performAnim()
    }


    private fun checkNfc() {
        when {
            mNfcAdapter == null -> {
                Toast.makeText(this, "NFC is not available for device", Toast.LENGTH_SHORT).show()
            }

            !mNfcAdapter!!.isEnabled -> {
                Toast.makeText(this, "NFC is available for device. But, not enabled", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                this.finish()
            }

            mNfcAdapter!!.isEnabled -> {

            }
        }
    }

    private fun performAnim() {
        val alphaAnim = ObjectAnimator.ofFloat(
            bg_gradient,
            View.ALPHA,
            0.25f,
            0.6f
        ).apply {
            duration = 1111
            interpolator = AccelerateDecelerateInterpolator()
        }
        alphaAnim.repeatMode = ObjectAnimator.REVERSE
        alphaAnim.repeatCount = ObjectAnimator.INFINITE
        alphaAnim.start()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()

        if (mNfcAdapter != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            mNfcAdapter!!.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }

    }

    private fun startVibration() {
        handler.postDelayed({
            vibration.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50), -1))
            startVibration()
        }, 1111)
    }


    override fun onTagDiscovered(tag: Tag?) {
        val isoDep: IsoDep?
        try {
            isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                startVibration()
            }
            isoDep.connect()
            val provider = PcscProvider()
            provider.setmTagCom(isoDep)
            val config = EmvTemplate.Config()
                .setContactLess(true)
                .setReadAllAids(true)
                .setReadTransactions(true)
                .setRemoveDefaultParsers(false)
                .setReadAt(true)
            val parser = EmvTemplate.Builder()
                .setProvider(provider)
                .setConfig(config)
                .build()
            val card = parser.readEmvCard()
            val cardNumber = card.cardNumber
            val expireDate = card.expireDate
            var date = LocalDate.of(1999, 12, 31)
            if (expireDate != null) {
                date = expireDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            val mySharedPreferences = getSharedPreferences(NFC_PREF_KEY, MODE_PRIVATE)
            val editor = mySharedPreferences.edit()
            editor.putString(CARD_NUMBER_KEY, cardNumber)
            editor.putString(CARD_MONTH_KEY, date.monthValue.toString())
            editor.putString(CARD_YEAR_KEY, date.year.toString())
            editor.apply()
            try {
                isoDep.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handler.removeCallbacksAndMessages(null)
        vibration.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300), -1))
        bg.postOnAnimationDelayed({
            this.finish()
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        vibratorManager.cancel()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val NFC_PREF_KEY = "MY_CARDS"
        const val CARD_NUMBER_KEY = "0q38uti4"
        const val CARD_MONTH_KEY = "fn2390ut"
        const val CARD_YEAR_KEY = "0e53yhb8"
    }
}