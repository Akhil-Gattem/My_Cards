package com.zimneos.mycards.presentation

import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.zimneos.mycards.R
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.layout_main_content_for_fragment.*
import kotlinx.android.synthetic.main.layout_main_screen_recylerview_list.view.*
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private lateinit var listGet: Holding
    private lateinit var viewModel: ListViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.layout_main_activity)
        setUpViewModel()
        getData()
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
        val saveData = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        saveData.putString(key, json)
        saveData.apply()
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
        val getSavedData = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val gson = Gson()
        val json = getSavedData.getString(key, "fail")
        val type: Type = object : TypeToken<Holding>() {}.type
        return gson.fromJson(json, type)
    }

    companion object {
        const val MY_PREFS_NAME = "Cards"
    }
}

