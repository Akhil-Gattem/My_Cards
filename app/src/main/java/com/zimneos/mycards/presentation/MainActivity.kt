package com.zimneos.mycards.presentation


import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.presentation.CardsListAdapter.OnItemListener
import com.zimneos.mycards.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.layout_main_content_for_fragment.*
import java.lang.reflect.Type


class MainActivity : AppCompatActivity(), OnItemListener {

    private lateinit var viewModel: ListViewModel
    private lateinit var listGet: Holding
    private var currentSize: Int = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "id.notifications"
    private val description = "my notification"

    private val cardsListAdapter = CardsListAdapter(arrayListOf(), this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.layout_main_activity)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, AddCardDetails::class.java)
        add_new.setOnTouchListener(MotionOnClickListener(this.applicationContext) {
            startActivity(intent)
        })
        setUpViewModel()
        recyclerViewLayoutManager()
        initSwipe()
        subscribeToLiveData()
        saveDataInSharedPreferences()
        getData()
    }

    private fun setViewVisibility(size: Int) {
        if (size != 0) {
            arrow.visibility = View.INVISIBLE
            click_to_add_text.visibility = View.INVISIBLE
        } else {
            arrow.visibility = View.VISIBLE
            click_to_add_text.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun initSwipe() {
        val paint = Paint()
        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val sizeBefore = ListViewModel.mockData.size
                val savedDataRemove = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()

                AlertDialog.Builder(this@MainActivity,R.style.AlertDialog)
                    .setTitle("Delete Card")
                    .setMessage("Are you sure you want to delete this card details?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteCardData(position, sizeBefore, savedDataRemove)
                    }
                    .setNegativeButton("No") { _, _ ->
                        cardsListAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    }.show()
            }

            private fun deleteCardData(
                position: Int,
                sizeBefore: Int,
                savedDataRemove: SharedPreferences.Editor
            ) {
                var position1 = position
                ListViewModel.mockData.removeAt(position1)
                cardsListAdapter.notifyItemRemoved(position1)
                var toBeSaved = position1
                while (position1 <= sizeBefore) {
                    try {
                        val getDataAt = position1 + 1
                        savedDataRemove.remove(toBeSaved.toString())
                        savedDataRemove.apply()
                        saveListInLocal(
                            getListFromLocal(getDataAt.toString()),
                            toBeSaved.toString()
                        )
                        savedDataRemove.apply()
                        toBeSaved += 1
                        position1 += 1
                    } catch (exception: JsonSyntaxException) {
                        break
                    }
                }
                savedDataRemove.remove(toBeSaved.toString())
                savedDataRemove.apply()
                viewModel.refresh()
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float,
                dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(resources, R.drawable.delete)
                        val iconDest = RectF(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        c.drawBitmap(icon, null, iconDest, paint)
                    }
                }
                super.onChildDraw(
                    c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    override fun onDestroy() {
        super.onDestroy()
        val saveDataSize = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        currentSize = ListViewModel.mockData.size
        saveDataSize.putInt("size", currentSize)
        saveDataSize.apply()
    }

    private fun saveDataInSharedPreferences() {
        val extras = intent.extras
        val cardNumber: String?
        val cardHolderName: String?
        val month: String?
        val year: String?
        val cvv: String?
        val cardType: String?
        val cardNote: String?
        val size: Int
        if (extras != null && extras.getBoolean("isAdded", false)) {
            cardNumber = extras.getString(R.string.card_number.toString())
            cardHolderName = extras.getString(R.string.card_holder_name.toString())
            month = extras.getString(R.string.month.toString())
            year = extras.getString(R.string.year.toString())
            cvv = extras.getString(R.string.cvv.toString())
            cardType = extras.getString(R.string.card_type.toString())
            cardNote = extras.getString(R.string.notes_hint.toString())
            size = ListViewModel.mockData.size
            saveListInLocal(
                Holding(
                    cardNumber,
                    cardHolderName,
                    month,
                    year,
                    cvv,
                    cardType,
                    cardNote
                ), size.toString()
            )
        }
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
        ListViewModel.mockData.clear()
        while (i <= getSavedDataSize.getInt("size", 99)) {
            try {
                listGet = getListFromLocal(i.toString())
                ListViewModel.mockData.add(listGet)
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

    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        viewModel.refresh()
    }

    private fun recyclerViewLayoutManager() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cardsListAdapter
        }
    }

    private fun subscribeToLiveData() {
        viewModel.cardData.observe(this) {
            cardsListAdapter.updateCountries(it)
            setViewVisibility(it.size)
        }
    }

    private fun notification(item: Holding) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(applicationContext, channelId)
                .setTimeoutAfter(200000)
                .setSmallIcon(R.drawable.app_logo_avd)
                .setContentTitle("Expiry: " + item.month + "/" + item.year)
                .setContentText("Cvv: " + item.cvv)

        }
        notificationManager.notify(1234, builder.build())
    }


    override fun copyButtonClicked(view: View, item: Holding) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        Snackbar.make(view,
            "CARD NUMBER - Copied to Clipboard\nEXPIRY and CVV in Notification",
            Snackbar.LENGTH_LONG)
            .setAction("DISMISS") {}.show()
        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText(
                "label",
                item.cardNumber?.replace(" ", "")
            )
        )
        notification(item)
    }

    companion object {
        const val MY_PREFS_NAME = "Cards"
    }

}

