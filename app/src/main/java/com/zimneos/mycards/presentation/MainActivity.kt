package com.zimneos.mycards.presentation

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.zimneos.mycards.R
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private lateinit var listGet: Holding
    private lateinit var viewModel: ListViewModel
    private lateinit var masterKey : MasterKey
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeEncryptedSharedPreferences()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.fade_in,R.anim.fade_out)
        }
        setContentView(R.layout.layout_main_activity)
        setUpViewModel()
        getData()
    }

    private fun initializeEncryptedSharedPreferences() {
        masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        sharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            "nfc_secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(this)[ListViewModel::class.java]
    }

    override fun onPause() {
        val saveData = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        saveData.clear()
        saveData.apply()
        subscribeToLiveData()
        super.onPause()
    }

    private fun subscribeToLiveData() {
        viewModel.cardData.observe(this@MainActivity) { data ->
            Log.d("akki", "size ${data.size} ")
            try {
                for (s in 0..data.size) {
                    saveDataInSharedPreferences(data[s], s)
                    Log.d("akki", data[s].toString())
                }
            } catch (_: IndexOutOfBoundsException) {
            }
            val saveDataSize = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
            saveDataSize.putInt("size", data.size)
            saveDataSize.apply()
        }
    }

    private fun saveDataInSharedPreferences(holding: Holding, size: Int) {
        saveListInLocal(holding, size.toString())
    }

    private fun saveListInLocal(list: Holding, key: String?) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        sharedPreferences.edit().putString(key,json).apply()
    }

    private fun getData() {
        val getSavedDataSize = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        var i = 0
        viewModel.clearData()
        while (i <= getSavedDataSize.getInt("size", 99)) {
            try {
                listGet = getListFromLocal(i.toString())
                viewModel.addData(listGet)
                viewModel.refresh()
            } catch (exception: JsonSyntaxException) {
                break
            }
            i++
        }
    }

    private fun getListFromLocal(key: String?): Holding {
        getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(key, "fail")
        val type: Type = object : TypeToken<Holding>() {}.type
        return gson.fromJson(json, type)
    }

    companion object {
        const val MY_PREFS_NAME = "Cards"
    }
}

