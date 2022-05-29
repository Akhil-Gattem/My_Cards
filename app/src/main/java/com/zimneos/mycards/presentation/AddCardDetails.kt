package com.zimneos.mycards.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import kotlinx.android.synthetic.main.layout_add_card.*

class AddCardDetails : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var intentSend: Intent
    private lateinit var getCardNumber: String
    private lateinit var getHolderName: String
    private lateinit var getMonth: String
    private lateinit var getYear: String
    private lateinit var getCVV: String
    private lateinit var getCardType: String
    private lateinit var getCardNotes: String
    private var courses = arrayOf<String?>("VISA", "MASTERCARD", "RUPAY")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.layout_add_card)
        intentSend = Intent(this, MainActivity::class.java)
        add_button.setOnTouchListener(MotionOnClickListener(this.applicationContext) {
            if (checkGetCardNumber()
                && checkGetHolderName() &&
                checkGetMonth() && checkGetYear() &&
                checkGetCVV() && checkGetCardType() && checkGetCardNotes()
            ) {
                card_notes.setText("")
                edit_cvv.setText("")
                edit_valid_year.setText("")
                edit_valid_date.setText("")
                edit_holder_name.setText("")
                edit_card_number.setText("")
                card_notes.hint = ""
                intentSend.putExtra("isAdded", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intentSend)
                this.finish()
            }
        })

        val customDropDownAdapter = CustomDropDownAdapter(this, courses)
        spinner_card_type.adapter = customDropDownAdapter
        spinner_card_type.onItemSelectedListener = this

        edit_card_number.addTextChangedListener(object : SeparatorTextWatcher(' ', 4) {
            override fun onAfterTextChanged(text: String) {
                edit_card_number.run {
                    setText(text)
                    setSelection(text.length)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun checkGetCardNumber(): Boolean {
        getCardNumber = edit_card_number.text.toString()
        return if (getCardNumber.length == 19) {
            text_card_number.setTextColor(ContextCompat.getColor(this, R.color.light_white))
            text_card_number_warning.visibility = View.INVISIBLE
            intentSend.putExtra(R.string.card_number.toString(), getCardNumber)
            true
        } else {
            text_card_number_warning.visibility = View.VISIBLE
            text_card_number.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            false
        }
    }

    private fun checkGetHolderName(): Boolean {
        getHolderName = edit_holder_name.text.toString()
        return if (getHolderName.length > 1) {
            text_holder_name.setTextColor(ContextCompat.getColor(this, R.color.light_white))
            text_holder_name_warning.visibility = View.INVISIBLE
            intentSend.putExtra(R.string.card_holder_name.toString(), getHolderName)
            true
        } else {
            text_holder_name_warning.visibility = View.VISIBLE
            text_holder_name.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            false
        }
    }

    private fun checkGetMonth(): Boolean {
        getMonth = edit_valid_date.text.toString()
        return if (getMonth.length == 2) {
            text_valid_till.setTextColor(ContextCompat.getColor(this, R.color.light_white))
            text_valid_till_warning.visibility = View.INVISIBLE
            intentSend.putExtra(R.string.month.toString(), getMonth)
            true
        } else {
            text_valid_till.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            text_valid_till_warning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetYear(): Boolean {
        getYear = edit_valid_year.text.toString()
        return if (getYear.length == 4) {
            text_valid_till.setTextColor(ContextCompat.getColor(this, R.color.light_white))
            text_valid_till_warning.visibility = View.INVISIBLE
            intentSend.putExtra(R.string.year.toString(), getYear)
            true
        } else {
            text_valid_till.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            text_valid_till_warning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetCVV(): Boolean {
        getCVV = edit_cvv.text.toString()
        return if (getCVV.length == 3) {
            text_cvv.setTextColor(ContextCompat.getColor(this, R.color.light_white))
            text_cvv_warning.visibility = View.INVISIBLE
            intentSend.putExtra(R.string.cvv.toString(), getCVV)
            true
        } else {
            text_cvv_warning.visibility = View.VISIBLE
            text_cvv.setTextColor(ContextCompat.getColor(this, R.color.light_red))
            false
        }
    }

    private fun checkGetCardType(): Boolean {
        getCardType = spinner_card_type.selectedItem as String
        intentSend.putExtra(R.string.card_type.toString(), getCardType)
        return true
    }

    private fun checkGetCardNotes(): Boolean {
        getCardNotes = card_notes.text.toString()
        return if (getCardNotes.isEmpty()) {
            card_notes.setHintTextColor(ContextCompat.getColor(this, R.color.light_red_fade))
            false
        } else {
            card_notes.setHintTextColor(ContextCompat.getColor(this, R.color.light_white))
            intentSend.putExtra(R.string.notes_hint.toString(), getCardNotes)
            true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}