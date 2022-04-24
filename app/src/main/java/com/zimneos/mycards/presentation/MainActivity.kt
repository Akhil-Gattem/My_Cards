package com.zimneos.mycards.presentation


import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    var p = Paint()
    private val cardsListAdapter = CardsListAdapter(arrayListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        val intent = Intent(this, AddCardDetails::class.java)
        setContentView(R.layout.layout_main_activity)
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

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun initSwipe() {
        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                var position = viewHolder.adapterPosition
                val sizeBefore = ListViewModel.mockData.size
                val savedDataRemove = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
                ListViewModel.mockData.removeAt(position)
                cardsListAdapter.notifyItemRemoved(position)
                var toBeSaved = position
                while (position <= sizeBefore) {
                    try {
                        val getDataAt = position + 1
                        savedDataRemove.remove(toBeSaved.toString())
                        savedDataRemove.apply()
                        saveListInLocal(
                            getListFromLocal(getDataAt.toString()),
                            toBeSaved.toString()
                        )
                        savedDataRemove.apply()
                        toBeSaved += 1
                        position += 1
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
                        c.drawBitmap(icon, null, iconDest, p)
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
        }
    }

    override fun onItemClick(getTitle: CharSequence) {
        Toast.makeText(this, getTitle, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val MY_PREFS_NAME = "Cards"
    }
}

