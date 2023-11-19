package com.zimneos.mycards.presentation


import android.annotation.SuppressLint
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zimneos.mycards.R
import com.zimneos.mycards.model.Holding
import kotlinx.android.synthetic.main.layout_main_screen_recylerview_list.view.*


class CardsListAdapter(
    private var cards: MutableList<Holding> = mutableListOf(),
    var clickListener: OnItemListener
) :
    RecyclerView.Adapter<CardsListAdapter.ViewHolder>() {

    private var context: Context? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateCountries(newCountries: MutableList<Holding>) {
        this.cards.clear()
        this.cards.addAll(newCountries)
        notifyDataSetChanged()
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
            setOnLongClickListener {
                when (delete_btn.visibility) {
                    View.GONE -> delete_btn.visibility = View.VISIBLE
                    View.VISIBLE -> delete_btn.visibility = View.GONE
                    View.INVISIBLE -> delete_btn.visibility = View.VISIBLE
                }
                this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                true
            }
            card_number.text = cards[position].cardNumber
            holder_name.text = cards[position].cardHolderName
            valid_till_date.text = cards[position].month + "/" + cards[position].year
            cvv_number.text = cards[position].cvv
            card_info.text = cards[position].cardNote
            copy.setOnClickListener {
                clickListener.copyButtonClicked(holder.itemView, cards[position])
            }
            delete_btn.setOnClickListener {
                clickListener.deletedButtonClicked(
                    holder.itemView,
                    cards[position].cardNumber,
                    cards[position].cardHolderName,
                    cards[position].month,
                    cards[position].year,
                    cards[position].cvv,
                    cards[position].cardType,
                    cards[position].cardNote,
                    position
                )
            }
            when {
                position % 2 == 0 -> {
                    card_type_logo.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.visa_logo
                    )
                    bg.background = ContextCompat.getDrawable(
                        card_type_logo.context,
                        R.drawable.black_card_bg_one
                    )
                }

                cards[position].cardType == "MASTERCARD" -> {
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
        fun deletedButtonClicked(
            view: View,
            cardNumber: String?,
            cardHolderName: String?,
            month: String?,
            year: String?,
            cvv: String?,
            cardType: String?,
            cardNote: String?,
            position: Int
        )
    }
}