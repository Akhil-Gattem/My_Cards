package com.zimneos.mycards.presentation


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zimneos.mycards.R
import com.zimneos.mycards.model.Holding
import kotlinx.android.synthetic.main.layout_main_screen_recylerview_list.view.*

class CardsListAdapter(var cards: ArrayList<Holding>, var clickListener: OnItemListener) :
    RecyclerView.Adapter<CardsListAdapter.ViewHolder>() {

    fun updateCountries(newCountries: List<Holding>) {
        cards.clear()
        cards.addAll(newCountries)
    }

    inner class ViewHolder internal constructor(itemVew: View) :
        RecyclerView.ViewHolder(itemVew)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_main_screen_recylerview_list, parent, false)
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
            card_type_logo.background = when (cards[position].cardType) {
                "VISA" -> {
                    ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.visa_logo
                    )
                }
                "RUPAY" -> {
                    ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.rupay_logo
                    )
                }
                else ->   ContextCompat.getDrawable(
                    card_type_logo.context,
                    R.drawable.master_card_logo
                )
            }
        }
    }

    interface OnItemListener {
        fun onItemClick(getTitle: CharSequence)
    }
}