package com.zimneos.mycards.presentation


import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zimneos.mycards.R
import com.zimneos.mycards.common.MotionOnClickListener
import com.zimneos.mycards.model.Holding
import com.zimneos.mycards.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.layout_main_content_for_fragment.*
import kotlinx.android.synthetic.main.layout_main_screen_recylerview_list.view.*


class CardDataFragment : Fragment(), CardsListAdapter.OnItemListener {

    private lateinit var viewModel: ListViewModel
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "id.notifications"
    private val description = "my notification"
    private val cardsListAdapter = CardsListAdapter(clickListener = this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_card_data, container, false)
        setUpViewModel()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToLiveData()
        notificationManager =
            requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        add_new.setOnTouchListener(MotionOnClickListener(requireContext()) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, AddCardDetails())
                .addToBackStack(null)
                .commit()
        })
        recyclerViewLayoutManager()
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

    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(requireActivity())[ListViewModel::class.java]
    }

    private fun recyclerViewLayoutManager() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cardsListAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("message", "This is my message to be reloaded")
        super.onSaveInstanceState(outState)
    }

    private fun subscribeToLiveData() {
        viewModel.cardData.observe(viewLifecycleOwner) {
            cardsListAdapter.updateCountries(it)
            setViewVisibility(it.size)
        }
    }

    private fun notification(item: Holding) {
        notificationManager =
            requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(requireContext(), channelId)
                .setTimeoutAfter(200000)
                .setSmallIcon(R.drawable.app_logo_avd)
                .setContentTitle("Expiry: " + item.month + "/" + item.year)
                .setContentText("Cvv: " + item.cvv)

        }
        notificationManager.notify(1234, builder.build())
    }


    override fun copyButtonClicked(view: View, item: Holding) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        Snackbar.make(
            view,
            "CARD NUMBER - Copied to Clipboard\nEXPIRY and CVV in Notification",
            Snackbar.LENGTH_LONG
        ).setAction("DISMISS") {}.show()
        (requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
            setPrimaryClip(
                ClipData.newPlainText(
                    "label",
                    item.cardNumber?.replace(" ", "")
                )
            )
        }
        notification(item)
    }

    override fun deletedButtonClicked(
        view: View,
        cardNumber: String?,
        cardHolderName: String?,
        month: String?,
        year: String?,
        cvv: String?,
        cardType: String?,
        cardNote: String?,
        position: Int
    ) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialog)
            .setTitle("Delete Card")
            .setMessage("Are you sure you want to delete this card details?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteData(
                    cardNumber,
                    cardHolderName,
                    month,
                    year,
                    cvv,
                    cardType,
                    cardNote
                )
                cardsListAdapter.notifyItemRemoved(position)
                viewModel.refresh()
                view.delete_btn.visibility = View.GONE
            }
            .setNegativeButton("No") { _, _ ->
                view.delete_btn.visibility = View.GONE
            }.show()
    }

}
