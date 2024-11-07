package com.sesameware.smartyard_oem.ui.main.address.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
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
import com.sesameware.smartyard_oem.ui.main.address.models.AddressListItem
import com.sesameware.smartyard_oem.ui.main.address.models.AddressState
import com.sesameware.smartyard_oem.ui.main.address.models.EntranceState
import com.sesameware.smartyard_oem.ui.main.address.models.IssueAction
import com.sesameware.smartyard_oem.ui.main.address.models.IssueModel
import com.sesameware.smartyard_oem.ui.main.address.models.OnCameraClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnEventLogClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnExpandClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnIssueClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnOpenEntranceClick
import com.sesameware.smartyard_oem.ui.main.address.models.OnQrCodeClick
import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.VideoCameraModelP

typealias AddressCallback = (AddressAction) -> Unit
typealias IssueCallback = (IssueAction) -> Unit

class AddressListAdapter(
    private val addressCallback: AddressCallback,
    private val issueCallback: IssueCallback
) : ListAdapter<AddressListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AddressState -> ADDRESS_STATE
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
                val state = getItem(position) as AddressState
                holder.bind(state, addressCallback)
            }
            is IssueViewHolder -> {
                val state = getItem(position) as IssueModel
                holder.bind(state, issueCallback)
            }
        }
    }

    // To skip rebinding, when AddressState.isExpanded is set by button click
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<AddressListItem>() {

        private const val ADDRESS_STATE = 0
        private const val ISSUE_MODEL = 1

        override fun areItemsTheSame(oldItem: AddressListItem, newItem: AddressListItem) =
            when {
                oldItem is AddressState && newItem is AddressState -> {
                    oldItem.houseId == newItem.houseId
                }
                oldItem is IssueModel && newItem is IssueModel -> {
                    oldItem.key == newItem.key
                }
                else -> false
            }

        override fun areContentsTheSame(oldItem: AddressListItem, newItem: AddressListItem) =
            when {
                oldItem is AddressState && newItem is AddressState -> {
                    oldItem == newItem
                }
                oldItem is IssueModel && newItem is IssueModel -> {
                    oldItem == newItem
                }
                else -> false
            }

        override fun getChangePayload(oldItem: AddressListItem, newItem: AddressListItem): Any? =
            if (oldItem is AddressState && newItem is AddressState &&
                oldItem.isExpanded != newItem.isExpanded) true else null

    }
}

private class AddressViewHolder private constructor(
    private val binding: ItemAddressBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(state: AddressState, callback: AddressCallback) {
        with (binding) {
            addressTitle.text = state.title
            expandAddress.isSelected = state.isExpanded
            expandableLayout.setExpanded(state.isExpanded, false)
            expandAddress.setOnClickListener {
                expandAddress.isSelected = !expandAddress.isSelected
                callback(OnExpandClick(bindingAdapterPosition, expandAddress.isSelected))
                expandableLayout.toggle()
            }
            addEntrances(binding.addressContent, state.entranceList, callback)
            val model = VideoCameraModelP(state.houseId, state.title)
            addCameras(binding.addressContent, model, state.cameraCount, callback)
            addEventLog(binding.addressContent, state.hasEventLog, callback)
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
                    callback(OnOpenEntranceClick(state.id))
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
        val binding = ItemVideoCameraBinding.inflate(LayoutInflater.from(layout.context),
            layout,true)
        with (binding) {
            tvCount.text = count.toString()
            root.setOnClickListener {
                callback(OnCameraClick(model))
            }
        }
    }

    private fun addEventLog(layout: LinearLayout, hasEventLog: Boolean, callback: AddressCallback) {
        if (!hasEventLog) return

        val binding = ItemEventLogBinding.inflate(LayoutInflater.from(layout.context),
            layout, true)
        with (binding) {
            root.setOnClickListener {
                callback(OnEventLogClick)
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