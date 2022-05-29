package com.zimneos.mycards.presentation


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.zimneos.mycards.R
import com.zimneos.mycards.model.Holding
import kotlinx.android.synthetic.main.layout_main_screen_recylerview_list.view.*


class CardsListAdapter(private var cards: ArrayList<Holding>, var clickListener: OnItemListener) :
    RecyclerView.Adapter<CardsListAdapter.ViewHolder>() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "id.notifications"
    private val description = "my notification"
    private var context: Context? = null

    fun updateCountries(newCountries: List<Holding>) {
        cards.clear()
        cards.addAll(newCountries)
    }

    inner class ViewHolder internal constructor(itemVew: View) :
        RecyclerView.ViewHolder(itemVew)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_main_screen_recylerview_list, parent, false)

        context = parent.context

        return ViewHolder(view)
    }

    override fun getItemCount() = cards.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply {
            card_number.text = cards[position].cardNumber
            holder_name.text = cards[position].cardHolderName
            valid_till_date.text = cards[position].month + "/" + cards[position].year
            cvv_number.text = cards[position].cvv
            card_info.text = cards[position].cardNote
            copy.setOnClickListener {
                clickListener.copyButtonClicked(holder.itemView, cards[position])
            }
            when (cards[position].cardType) {
                "VISA" -> {
                    card_type_logo.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.visa_logo
                    )
                    bg.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.black_card_bg_one
                    )
                }
                "RUPAY" -> {
                    card_type_logo.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.rupay_logo
                    )
                    bg.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.black_card_bg_two
                    )
                }
                else -> {
                    card_type_logo.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.master_card_logo
                    )
                    bg.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.black_card_bg_three
                    )
                }
            }

        }
    }

    interface OnItemListener {
        fun copyButtonClicked(view: View, item: Holding)
    }
}