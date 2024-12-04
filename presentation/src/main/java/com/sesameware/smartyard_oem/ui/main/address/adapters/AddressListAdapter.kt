package com.sesameware.smartyard_oem.ui.main.address.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sesameware.smartyard_oem.databinding.ItemAddressBinding
import com.sesameware.smartyard_oem.databinding.ItemEventLogBinding
import com.sesameware.smartyard_oem.databinding.ItemIssueBinding
import com.sesameware.smartyard_oem.databinding.ItemVideoCameraBinding
import com.sesameware.smartyard_oem.databinding.ItemYardBinding
import com.sesameware.smartyard_oem.ui.main.address.models.AddressAction
import com.sesameware.smartyard_oem.ui.main.address.models.AddressUiModel
import com.sesameware.smartyard_oem.ui.main.address.models.EntranceState
import com.sesameware.smartyard_oem.ui.main.address.models.HouseUiModel
import com.sesameware.smartyard_oem.ui.main.address.models.IssueAction
import com.sesameware.smartyard_oem.ui.main.address.models.IssueModel
import com.sesameware.smartyard_oem.ui.main.address.models.OnCameraClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnEventLogClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnExpandClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnIssueClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnItemFullyExpanded
import com.sesameware.smartyard_oem.ui.main.address.models.OnOpenEntranceClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnQrCodeClick
import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.VideoCameraModelP
import net.cachapa.expandablelayout.ExpandableLayout.OnExpansionUpdateListener

typealias AddressCallback = (AddressAction) -> Unit
typealias IssueCallback = (IssueAction) -> Unit

class AddressListAdapter(
    private val addressCallback: AddressCallback,
    private val issueCallback: IssueCallback
) : ListAdapter<AddressUiModel, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HouseUiModel -> ADDRESS_STATE
            is IssueModel -> ISSUE_MODEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ADDRESS_STATE -> AddressViewHolder.getInstance(parent)
            ISSUE_MODEL -> IssueViewHolder.getInstance(parent)
            else -> throw IllegalArgumentException("Invalid type of view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddressViewHolder -> {
                val state = getItem(position) as HouseUiModel
                holder.bind(state, addressCallback)
            }
            is IssueViewHolder -> {
                val state = getItem(position) as IssueModel
                holder.bind(state, issueCallback)
            }
        }
    }

    // To skip rebinding, when item expands itself
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<AddressUiModel>() {

        private const val ADDRESS_STATE = 0
        private const val ISSUE_MODEL = 1

        override fun areItemsTheSame(oldItem: AddressUiModel, newItem: AddressUiModel) =
            when {
                oldItem is HouseUiModel && newItem is HouseUiModel -> {
                    oldItem.houseId == newItem.houseId
                }
                oldItem is IssueModel && newItem is IssueModel -> {
                    oldItem.key == newItem.key
                }
                else -> false
            }

        override fun areContentsTheSame(oldItem: AddressUiModel, newItem: AddressUiModel) =
            when {
                oldItem is HouseUiModel && newItem is HouseUiModel -> {
                    oldItem == newItem
                }
                oldItem is IssueModel && newItem is IssueModel -> {
                    oldItem == newItem
                }
                else -> false
            }

        override fun getChangePayload(oldItem: AddressUiModel, newItem: AddressUiModel): Any? =
            if (oldItem is HouseUiModel && newItem is HouseUiModel &&
                oldItem.isExpanded != newItem.isExpanded) true else null

    }
}

private class AddressViewHolder private constructor(
    private val binding: ItemAddressBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(state: HouseUiModel, callback: AddressCallback) {
        with (binding) {
            addressTitle.text = state.address
            expandAddress.isSelected = state.isExpanded
            expandableLayout.setExpanded(state.isExpanded, false)
            expandableLayout.setOnExpansionUpdateListener(object : OnExpansionUpdateListener {
                private var lastFraction = -1f

                override fun onExpansionUpdate(expansionFraction: Float, state: Int) {
                    if (lastFraction != expansionFraction && expansionFraction == 1f) {
                        callback(OnItemFullyExpanded(bindingAdapterPosition))
                    }
                    lastFraction = expansionFraction
                }
            })
            val onHeaderClickListener = View.OnClickListener {
                expandAddress.isSelected = !expandAddress.isSelected
                callback(OnExpandClick(bindingAdapterPosition, expandAddress.isSelected))
                expandableLayout.toggle()
            }
            addressTitle.setOnClickListener(onHeaderClickListener)
            expandAddress.setOnClickListener(onHeaderClickListener)

            addressContent.removeAllViews()
            addEntrances(addressContent, state.entranceList, callback)
            val model = VideoCameraModelP(state.houseId, state.address)
            addCameras(addressContent, model, state.cameraCount, callback)
            addEventLog(addressContent, state.hasEventLog,
                state.address, state.houseId, callback)
        }
    }

    private fun addEntrances(
        layout: LinearLayout,
        list: List<EntranceState>,
        callback: AddressCallback
    ) {
        list.forEach { state ->
            val binding = ItemYardBinding.inflate(LayoutInflater.from(layout.context),
                layout, true)
            with (binding){
                ivImage.setImageResource(state.iconRes)
                tvName.text = state.name
                tbOpen.isChecked = false
                tbOpen.setOnClickListener {
                    callback(OnOpenEntranceClick(state.entranceId))
                    tbOpen.isClickable = false
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(
                        {
                            tbOpen.isChecked = false
                            tbOpen.isClickable = true
                        },
                        3000
                    )
                }
            }
        }
    }

    private fun addCameras(
        layout: LinearLayout,
        model: VideoCameraModelP,
        count: Int,
        callback: AddressCallback
    ) {
        if (count == 0) return

        val binding = ItemVideoCameraBinding.inflate(LayoutInflater.from(layout.context),
            layout,true)
        with (binding) {
            tvCount.text = count.toString()
            root.setOnClickListener {
                callback(OnCameraClick(model))
            }
        }
    }

    private fun addEventLog(
        layout: LinearLayout,
        hasEventLog: Boolean,
        title: String,
        houseId: Int,
        callback: AddressCallback
    ) {
        if (!hasEventLog) return

        val binding = ItemEventLogBinding.inflate(LayoutInflater.from(layout.context),
            layout, true)
        with (binding) {
            root.setOnClickListener {
                callback(OnEventLogClick(title, houseId))
            }
        }
    }

    companion object {
        fun getInstance(parent: ViewGroup) : AddressViewHolder {
            val binding = ItemAddressBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return AddressViewHolder(binding)
        }
    }
}

private class IssueViewHolder private constructor(
    private val binding: ItemIssueBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(state: IssueModel, callback: IssueCallback) {
        with (binding) {
            tvAddress.text = state.address
            ivQrCode.setOnClickListener {
                callback(OnQrCodeClick)
            }
            root.setOnClickListener {
                callback(OnIssueClick(state))
            }
        }
    }

    companion object {
        fun getInstance(parent: ViewGroup) : IssueViewHolder {
            val binding = ItemIssueBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return IssueViewHolder(binding)
        }
    }
}