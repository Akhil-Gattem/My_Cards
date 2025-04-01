package com.zimneos.mycards.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import com.zimneos.mycards.databinding.LayoutAddCardBinding
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import com.zimneos.mycards.viewmodel.NFCCardDataViewModel

class AddCardDetails : Fragment(), AdapterView.OnItemSelectedListener, ViewModelStoreOwner {

    private var mNfcAdapter: NfcAdapter? = null
    private var _binding: LayoutAddCardBinding? = null
    private val binding get() = _binding!!
    private lateinit var listviewModel: ListViewModel
    private lateinit var nfcViewModel: NFCCardDataViewModel
    private var cardTypes = arrayOf<String?>("VISA", "MASTERCARD", "RUPAY")
    private var getCardNumber: String = ""
    private var getHolderName: String = ""
    private var getMonth: String = ""
    private var getYear: String = ""
    private var getCVV: String = ""
    private var getCardType: String = ""
    private var getCardNotes: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(
                        R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
                    )
                    replace(R.id.main_container, CardDataFragment())
                    commit()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _binding = LayoutAddCardBinding.inflate(inflater, container, false)
        setUpViewModel()
        clearEditTexts()
        mNfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        checkNfc()
        if (savedInstanceState != null) {
            getCardNumber = savedInstanceState.getString("cardNumber", "")
            getHolderName = savedInstanceState.getString("holderName", "")
            getMonth = savedInstanceState.getString("month", "")
            getYear = savedInstanceState.getString("year", "")
            getCVV = savedInstanceState.getString("cvv", "")
            getCardType = savedInstanceState.getString("cardType", cardTypes[0] ?: "VISA")
            getCardNotes = savedInstanceState.getString("cardNotes", "")

            with(binding) {
                editCardNumber.setText(getCardNumber)
                editHolderName.setText(getHolderName)
                editValidDate.setText(getMonth)
                editValidYear.setText(getYear)
                editCvv.setText(getCVV)
                editCardNotes.setText(getCardNotes)
                spinnerCardType.setSelection(cardTypes.indexOf(getCardType))
            }
        }

        return binding.root
    }



    private fun checkNfc() {
        when {
            mNfcAdapter == null -> {
                Toast.makeText(
                    requireContext(),
                    "NFC is not available for device",
                    Toast.LENGTH_SHORT
                ).show()
                binding.nfcButton.visibility = View.INVISIBLE
            }

            mNfcAdapter?.isEnabled == false -> {
                binding.nfcButton.visibility = View.VISIBLE
            }

            mNfcAdapter?.isEnabled == true -> {
                binding.nfcButton.visibility = View.VISIBLE
            }
        }
    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return appViewModelStore
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addButton.setOnTouchListener(MotionOnClickListener(requireContext()) {
                if (checkEditTextCriteria()) {
                    addDataInViewModel()
                    clearEditTexts()
                    navigateToCardDataFragment()
                }
            })

            nfcButton.setOnTouchListener(MotionOnClickListener(requireContext()) {
                clearEditTexts()
                val intent = Intent(requireContext(), NFCActivity::class.java)
                startActivity(intent)
            })

            val customDropDownAdapter = CustomDropDownAdapter(requireContext(), cardTypes)
            spinnerCardType.adapter = customDropDownAdapter
            spinnerCardType.onItemSelectedListener = this@AddCardDetails

            setEditTextFocusToNextTextView(editCardNumber, editHolderName, 16, true)
            setEditTextFocusToNextTextView(editValidDate, editValidYear, 2)
            setEditTextFocusToNextTextView(editValidYear, editCvv, 4)
            setEditTextFocusToNextTextView(editCvv, editCardNotes, 3)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            with(it) {
                outState.putString("cardNumber", editCardNumber.text.toString())
                outState.putString("holderName", editHolderName.text.toString())
                outState.putString("month", editValidDate.text.toString())
                outState.putString("year", editValidYear.text.toString())
                outState.putString("cvv", editCvv.text.toString())
                outState.putString("cardType", spinnerCardType.selectedItem as String)
                outState.putString("cardNotes", editCardNotes.text.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.editCardNumber.text.isEmpty()) {
            subscribeToLivedata()
        }
    }

    override fun onPause() {
        super.onPause()
        with(binding) {
            getCardNumber = editCardNumber.text.toString()
            getHolderName = editHolderName.text.toString()
            getMonth = editValidDate.text.toString()
            getYear = editValidYear.text.toString()
            getCVV = editCvv.text.toString()
            getCardType = spinnerCardType.selectedItem as String
            getCardNotes = editCardNotes.text.toString()
        }
    }

    private fun subscribeToLivedata() {
        val mySharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(NFC_PREF_KEY, Context.MODE_PRIVATE)

        with(binding) {
            editCardNumber.setText(mySharedPreferences.getString(CARD_NUMBER_KEY, ""))

            val month = mySharedPreferences.getString(CARD_MONTH_KEY, "")
            if (month?.isNotEmpty() == true) {
                val formattedMonth = if (month.length == 1) "0$month" else month
                editValidDate.setText(formattedMonth)
            } else {
                editValidDate.setText("")
            }

            editValidYear.setText(mySharedPreferences.getString(CARD_YEAR_KEY, "20"))
            setYearEditTextCursor()
        }
    }

    private fun setEditTextFocusToNextTextView(
        currentEditText: EditText,
        nextEditText: EditText,
        length: Int,
        setSpaceAfterFourDigits: Boolean = false
    ) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = currentEditText.text.toString()
                if (count > before && currentText.length == length) {
                    if (setSpaceAfterFourDigits) {
                        val formattedText = currentText.chunked(4).joinToString(" ")
                        currentEditText.setText(formattedText)
                        currentEditText.setSelection(formattedText.length)
                    }
                    currentEditText.clearFocus()
                    nextEditText.requestFocus()
                    nextEditText.isCursorVisible = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setYearEditTextCursor() {
        binding.editValidYear.setOnFocusChangeListener { _, hasFocus ->
            if (binding.editValidYear.text.length == 2)
                if (hasFocus) binding.editValidYear.setSelection(2)
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
        checkGetCardNumber() && checkGetHolderName() && checkGetMonth() &&
                checkGetYear() && checkGetCVV() && checkGetCardType() && checkGetCardNotes()

    private fun clearEditTexts() {
        with(binding) {
            editCardNotes.setText("")
            editCvv.setText("")
            editValidYear.setText("")
            editValidDate.setText("")
            editHolderName.setText("")
            editCardNumber.setText("")
            editCardNotes.hint = ""
        }
        getCardNumber = ""
        getHolderName = ""
        getMonth = ""
        getYear = ""
        getCVV = ""
        getCardType = ""
        getCardNotes = ""
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
        listviewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        nfcViewModel = ViewModelProvider(requireActivity())[NFCCardDataViewModel::class.java]
    }

    private fun checkGetCardNumber(): Boolean {
        getCardNumber = binding.editCardNumber.text.toString()
        return if (getCardNumber.length == 19) {
            binding.textCardNumber.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            binding.textCardNumberWarning.visibility = View.INVISIBLE
            true
        } else {
            binding.textCardNumberWarning.visibility = View.VISIBLE
            binding.textCardNumber.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
                )
            )
            false
        }
    }

    private fun checkGetHolderName(): Boolean {
        getHolderName = binding.editHolderName.text.toString()
        return if (getHolderName.length > 1) {
            binding.textHolderName.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            binding.textHolderNameWarning.visibility = View.INVISIBLE
            true
        } else {
            binding.textHolderNameWarning.visibility = View.VISIBLE
            binding.textHolderName.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
                )
            )
            false
        }
    }

    private fun checkGetMonth(): Boolean {
        getMonth = binding.editValidDate.text.toString()
        if (getMonth.length == 1) {
            getMonth = "0$getMonth"
            binding.editValidDate.setText(getMonth)
        }
        return if (getMonth.length == 2) {
            binding.textValidTill.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            binding.textValidTillWarning.visibility = View.INVISIBLE
            true
        } else {
            binding.textValidTill.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
                )
            )
            binding.textValidTillWarning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetYear(): Boolean {
        getYear = binding.editValidYear.text.toString()
        return if (getYear.length == 4) {
            binding.textValidTill.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            binding.textValidTillWarning.visibility = View.INVISIBLE
            true
        } else {
            binding.textValidTill.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
                )
            )
            binding.textValidTillWarning.visibility = View.VISIBLE
            false
        }
    }

    private fun checkGetCVV(): Boolean {
        getCVV = binding.editCvv.text.toString()
        return if (getCVV.length == 3) {
            binding.textCvv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            binding.textCvvWarning.visibility = View.INVISIBLE
            true
        } else {
            binding.textCvvWarning.visibility = View.VISIBLE
            binding.textCvv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
                )
            )
            false
        }
    }

    private fun checkGetCardType(): Boolean {
        getCardType = binding.spinnerCardType.selectedItem as String
        return true
    }

    private fun checkGetCardNotes(): Boolean {
        getCardNotes = binding.editCardNotes.text.toString()
        return if (getCardNotes.isEmpty()) {
            binding.editCardNotes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red_fade
                )
            )
            false
        } else {
            binding.editCardNotes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        val mySharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(NFC_PREF_KEY, Context.MODE_PRIVATE)
        val editor = mySharedPreferences.edit()
        editor.putString(CARD_NUMBER_KEY, "")
        editor.putString(CARD_MONTH_KEY, "")
        editor.putString(CARD_YEAR_KEY, "")
        editor.apply()
    }

    companion object {
        const val NFC_PREF_KEY = "MY_CARDS"
        const val CARD_NUMBER_KEY = "0q38uti4"
        const val CARD_MONTH_KEY = "fn2390ut"
        const val CARD_YEAR_KEY = "0e53yhb8"

        fun newInstance(): AddCardDetails {
            return AddCardDetails()
        }
    }
}