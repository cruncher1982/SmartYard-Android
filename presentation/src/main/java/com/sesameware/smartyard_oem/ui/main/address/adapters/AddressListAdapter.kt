package com.sesameware.smartyard_oem.ui.main.address.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sesameware.smartyard_oem.databinding.ItemEventLogBinding
import com.sesameware.smartyard_oem.databinding.ItemHouseBinding
import com.sesameware.smartyard_oem.databinding.ItemIssueBinding
import com.sesameware.smartyard_oem.databinding.ItemVideoCameraBinding
import com.sesameware.smartyard_oem.databinding.ItemYardBinding
import com.sesameware.smartyard_oem.ui.main.address.models.AddressUiModel
import com.sesameware.smartyard_oem.ui.main.address.models.EntranceState
import com.sesameware.smartyard_oem.ui.main.address.models.HouseAction
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

typealias HouseCallback = (HouseAction) -> Unit
typealias IssueCallback = (IssueAction) -> Unit

class AddressListAdapter(
    private val houseCallback: HouseCallback,
    private val issueCallback: IssueCallback
) : ListAdapter<AddressUiModel, RecyclerView.ViewHolder>(DiffCallback) {

    private var isViewDragged = false

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HouseUiModel -> HOUSE_UI_MODEL
            is IssueModel -> ISSUE_MODEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            HOUSE_UI_MODEL -> HouseViewHolder.getInstance(parent)
            ISSUE_MODEL -> IssueViewHolder.getInstance(parent)
            else -> throw IllegalArgumentException("Invalid type of view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HouseViewHolder -> {
                val state = getItem(position) as HouseUiModel
                holder.bind(state, houseCallback)
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

    fun onViewDragged() {
        isViewDragged = true
    }

    fun onViewReleased() {
        isViewDragged = false
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (isViewDragged) {
            if (holder is IssueViewHolder) return
            (holder as HouseViewHolder).onAnyItemDragged(false)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is IssueViewHolder) return
        (holder as HouseViewHolder).onViewRecycled()
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<AddressUiModel>() {

        private const val HOUSE_UI_MODEL = 0
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

class HouseViewHolder private constructor(
    private val binding: ItemHouseBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var stashedIsExpanded = false

    fun onThisItemDragged() {
        binding.root.apply {
            val elevationPx = context.resources.displayMetrics.density * DRAGGED_ELEVATION
            translationZ = elevationPx
            scaleX = DRAGGED_SCALE_X
            scaleY = DRAGGED_SCALE_Y
        }
    }

    fun onThisItemReleased() {
        binding.root.doOnPreDraw {
            it.translationZ = 0f
            it.scaleX = 1.0f
            it.scaleY = 1.0f
        }
    }

    fun onAnyItemDragged(animate: Boolean) {
        binding.apply {
            expandableLayout.setExpanded(false, animate)
            expandHouse.isSelected = false
        }
    }

    fun onViewRecycled() {
        binding.houseContent.removeAllViews()
    }

    fun bind(state: HouseUiModel, callback: HouseCallback) {
        with (binding) {
            houseAddress.text = state.address

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
                expandHouse.isSelected = !expandHouse.isSelected
                callback(OnExpandClick(bindingAdapterPosition, expandHouse.isSelected))
                expandableLayout.toggle()
            }
            houseAddress.setOnClickListener(onHeaderClickListener)
            expandHouse.setOnClickListener(onHeaderClickListener)
            expandHouse.isSelected = state.isExpanded

            addEntrances(houseContent, state.entranceList, callback)
            val model = VideoCameraModelP(state.houseId, state.address)
            addCameras(houseContent, model, state.cameraCount, callback)
            addEventLog(houseContent, state.hasEventLog,
                state.address, state.houseId, callback)
        }
    }

    private fun addEntrances(
        layout: LinearLayout,
        list: List<EntranceState>,
        callback: HouseCallback
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
        callback: HouseCallback
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
        callback: HouseCallback
    ) {
        if (!hasEventLog) return

        val binding = ItemEventLogBinding.inflate(
            LayoutInflater.from(layout.context),
            layout, true
        )
        with(binding) {
            root.setOnClickListener {
                callback(OnEventLogClick(title, houseId))
            }
        }
    }

    companion object {
        private const val DRAGGED_ELEVATION = 1.5f
        private const val DRAGGED_SCALE_X = 1.035f
        private const val DRAGGED_SCALE_Y = 1.08f

        fun getInstance(parent: ViewGroup) : HouseViewHolder {
            val binding = ItemHouseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return HouseViewHolder(binding)
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