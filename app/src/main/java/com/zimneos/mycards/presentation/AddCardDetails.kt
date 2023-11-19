package com.zimneos.mycards.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.layout_add_card.*

class AddCardDetails : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var getCardNumber: String
    private lateinit var getHolderName: String
    private lateinit var getMonth: String
    private lateinit var getYear: String
    private lateinit var getCVV: String
    private lateinit var getCardType: String
    private lateinit var getCardNotes: String
    private lateinit var viewModel: ListViewModel
    private var courses = arrayOf<String?>("VISA", "MASTERCARD", "RUPAY")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.layout_add_card, container, false)
        setUpViewModel()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_button.setOnTouchListener(MotionOnClickListener(requireContext()) {
            if (checkGetCardNumber()
                && checkGetHolderName() &&
                checkGetMonth() && checkGetYear() &&
                checkGetCVV() && checkGetCardType() && checkGetCardNotes()
            ) {
                viewModel.addData(
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
                card_notes.setText("")
                edit_cvv.setText("")
                edit_valid_year.setText("")
                edit_valid_date.setText("")
                edit_holder_name.setText("")
                edit_card_number.setText("")
                card_notes.hint = ""
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, CardDataFragment())
                    .addToBackStack(null)
                    .commit()

            }
        })

        val customDropDownAdapter = CustomDropDownAdapter(requireContext(), courses)
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

    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(requireActivity())[ListViewModel::class.java]
    }

    private fun checkGetCardNumber(): Boolean {
        getCardNumber = edit_card_number.text.toString()
        return if (getCardNumber.length == 19) {
            text_card_number.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            text_card_number_warning.visibility = View.INVISIBLE
            true
        } else {
            text_card_number_warning.visibility = View.VISIBLE
            text_card_number.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
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
                    requireContext(),
                    R.color.light_white
                )
            )
            text_holder_name_warning.visibility = View.INVISIBLE
            true
        } else {
            text_holder_name_warning.visibility = View.VISIBLE
            text_holder_name.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
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
                    requireContext(),
                    R.color.light_white
                )
            )
            text_valid_till_warning.visibility = View.INVISIBLE
            true
        } else {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
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
                    requireContext(),
                    R.color.light_white
                )
            )
            text_valid_till_warning.visibility = View.INVISIBLE
            true
        } else {
            text_valid_till.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red
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
        getCardNotes = card_notes.text.toString()
        return if (getCardNotes.isEmpty()) {
            card_notes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_red_fade
                )
            )
            false
        } else {
            card_notes.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_white
                )
            )
            true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}