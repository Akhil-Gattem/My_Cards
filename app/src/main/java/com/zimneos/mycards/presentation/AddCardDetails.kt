package com.zimneos.mycards.presentation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.security.crypto.MasterKey
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import com.zimneos.mycards.viewmodel.NFCCardDataViewModel
import kotlinx.android.synthetic.main.layout_add_card.add_button
import kotlinx.android.synthetic.main.layout_add_card.edit_card_notes
import kotlinx.android.synthetic.main.layout_add_card.edit_card_number
import kotlinx.android.synthetic.main.layout_add_card.edit_cvv
import kotlinx.android.synthetic.main.layout_add_card.edit_holder_name
import kotlinx.android.synthetic.main.layout_add_card.edit_valid_date
import kotlinx.android.synthetic.main.layout_add_card.edit_valid_year
import kotlinx.android.synthetic.main.layout_add_card.nfc_button
import kotlinx.android.synthetic.main.layout_add_card.spinner_card_type
import kotlinx.android.synthetic.main.layout_add_card.text_card_number
import kotlinx.android.synthetic.main.layout_add_card.text_card_number_warning
import kotlinx.android.synthetic.main.layout_add_card.text_cvv
import kotlinx.android.synthetic.main.layout_add_card.text_cvv_warning
import kotlinx.android.synthetic.main.layout_add_card.text_holder_name
import kotlinx.android.synthetic.main.layout_add_card.text_holder_name_warning
import kotlinx.android.synthetic.main.layout_add_card.text_valid_till
import kotlinx.android.synthetic.main.layout_add_card.text_valid_till_warning


class AddCardDetails : Fragment(), AdapterView.OnItemSelectedListener, ViewModelStoreOwner {

    private lateinit var getCardNumber: String
    private lateinit var getHolderName: String
    private lateinit var getMonth: String
    private lateinit var getYear: String
    private lateinit var getCVV: String
    private lateinit var getCardType: String
    private lateinit var getCardNotes: String
    private lateinit var listviewModel: ListViewModel
    private lateinit var nfcViewModel: NFCCardDataViewModel
    private var cardTypes = arrayOf<String?>("VISA", "MASTERCARD", "RUPAY")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.layout_add_card, container, false)
        setUpViewModel()
        return rootView
    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_button.setOnTouchListener(MotionOnClickListener(requireContext()) {
            if (checkEditTextCriteria()) {
                addDataInViewModel()
                clearEditTexts()
                navigateToCardDataFragment()
            }
        })
        nfc_button.setOnTouchListener(MotionOnClickListener(requireContext()) {
            val intent = Intent(requireContext(), NFCActivity::class.java)
            startActivity(intent)
        })
        val customDropDownAdapter = CustomDropDownAdapter(requireContext(), cardTypes)
        spinner_card_type.adapter = customDropDownAdapter
        spinner_card_type.onItemSelectedListener = this

        setEditTextFocusToNextTextView(edit_card_number, edit_holder_name, 16, true)
        setEditTextFocusToNextTextView(edit_valid_date, edit_valid_year, 2)
        setEditTextFocusToNextTextView(edit_valid_year, edit_cvv, 4)
        setEditTextFocusToNextTextView(edit_cvv, edit_card_notes, 3)

    }

    override fun onResume() {
        super.onResume()
        subscribeToLivedata()
    }

    private fun subscribeToLivedata() {
        val mySharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(NFCActivity.NFC_PREF_KEY, Context.MODE_PRIVATE)
        edit_card_number.setText(mySharedPreferences.getString(NFCActivity.CARD_NUMBER_KEY, ""))
        edit_valid_date.setText(mySharedPreferences.getString(NFCActivity.CARD_MONTH_KEY, ""))
        edit_valid_year.setText(mySharedPreferences.getString(NFCActivity.CARD_YEAR_KEY, "20"))
        setYearEditTextCursor()
    }

    private fun setEditTextFocusToNextTextView(
        currentEditText: EditText,
        nextEditText: EditText,
        length: Int,
        setSpaceAfterFourDigits: Boolean = false
    ) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (currentEditText.text.toString().length == length) {
                    currentEditText.clearFocus()
                    nextEditText.requestFocus()
                    nextEditText.isCursorVisible = true
                    if (setSpaceAfterFourDigits) {
                        val stringWithSpaceAfterEvery4thChar =
                            currentEditText.text.replace("....".toRegex(), "$0 ")
                        currentEditText.setText(stringWithSpaceAfterEvery4thChar)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun setYearEditTextCursor() {
        edit_valid_year.setOnFocusChangeListener { _, hasFocus ->
            if (edit_valid_year.text.length == 2)
            if (hasFocus) edit_valid_year.setSelection(2)
        }
    }

    private fun navigateToCardDataFragment() {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
            )
            replace(R.id.main_container, CardDataFragment())
            commit()
        }
    }

    private fun checkEditTextCriteria() =
        checkGetCardNumber()
                && checkGetHolderName()
                && checkGetMonth()
                && checkGetYear()
                && checkGetCVV()
                && checkGetCardType()
                && checkGetCardNotes()

    private fun clearEditTexts() {
        edit_card_notes.setText("")
        edit_cvv.setText("")
        edit_valid_year.setText("")
        edit_valid_date.setText("")
        edit_holder_name.setText("")
        edit_card_number.setText("")
        edit_card_notes.hint = ""
    }

    private fun addDataInViewModel() {
        listviewModel.addData(
            Holding(
                cardNumber = getCardNumber,
                cardHolderName = getHolderName,
                month = getMonth,
                year = getYear,
                cvv = getCVV,
                cardType = getCardType,
                cardNote = getCardNotes
            )
        )
    }

    private fun setUpViewModel() {
        listviewModel = ViewModelProviders.of(requireActivity())[ListViewModel::class.java]
        nfcViewModel = ViewModelProvider(requireActivity())[NFCCardDataViewModel::class.java]
    }

    private fun checkGetCardNumber(): Boolean {
        getCardNumber = edit_card_number.text.toString()
        return if (getCardNumber.length == 19) {
            text_card_number.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_white
                )
            )
            text_card_number_warning.visibility = View.INVISIBLE
            true
        } else {
            text_card_number_warning.visibility = View.VISIBLE
            text_card_number.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_red
                )
            )
            false
        }
    }

    private fun checkGetHolderName(): Boolean {
        getHolderName = edit_holder_name.text.toString()
        return if (getHolderName.length > 1) {
            text_holder_name.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_white
                )
            )
            text_holder_name_warning.visibility = View.INVISIBLE
            true
        } else {
            text_holder_name_warning.visibility = View.VISIBLE
            text_holder_name.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_red
                )
            )
            false
        }
    }

    private fun checkGetMonth(): Boolean {
        getMonth = edit_valid_date.text.toString()
        return if (getMonth.length == 2) {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_white
                )
            )
            text_valid_till_warning.visibility = View.INVISIBLE
            true
        } else {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_red
                )
            )
            text_valid_till_warning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetYear(): Boolean {
        getYear = edit_valid_year.text.toString()
        return if (getYear.length == 4) {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_white
                )
            )
            text_valid_till_warning.visibility = View.INVISIBLE
            true
        } else {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_red
                )
            )
            text_valid_till_warning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetCVV(): Boolean {
        getCVV = edit_cvv.text.toString()
        return if (getCVV.length == 3) {
            text_cvv.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_white))
            text_cvv_warning.visibility = View.INVISIBLE
            true
        } else {
            text_cvv_warning.visibility = View.VISIBLE
            text_cvv.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_red))
            false
        }
    }

    private fun checkGetCardType(): Boolean {
        getCardType = spinner_card_type.selectedItem as String
        return true
    }

    private fun checkGetCardNotes(): Boolean {
        getCardNotes = edit_card_notes.text.toString()
        return if (getCardNotes.isEmpty()) {
            edit_card_notes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_red_fade
                )
            )
            false
        } else {
            edit_card_notes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.light_white
                )
            )
            true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onStop() {
        super.onStop()
        val mySharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(NFCActivity.NFC_PREF_KEY, Context.MODE_PRIVATE)
        val editor = mySharedPreferences.edit()
        editor.putString(NFCActivity.CARD_NUMBER_KEY, "")
        editor.putString(NFCActivity.CARD_MONTH_KEY, "")
        editor.putString(NFCActivity.CARD_YEAR_KEY, "")
        editor.apply()
    }
}