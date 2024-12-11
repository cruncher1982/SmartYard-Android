package com.sesameware.smartyard_oem.ui.main.address.helpers

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragToSortCallback(
    private val onItemsSwap: (Int, Int) -> Unit,
    private val onItemDragged: (RecyclerView.ViewHolder?) -> Unit,
    private val onItemReleased: (RecyclerView.ViewHolder?) -> Unit
) : ItemTouchHelper.Callback() {

    private var lastDraggedItem: RecyclerView.ViewHolder? = null

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            onItemDragged(viewHolder)
            lastDraggedItem = viewHolder
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) onItemReleased(lastDraggedItem)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    private var alreadySwapped = false
    private var lastTargetPosition = -1
    private var _targetView: View? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        if (actionState != ItemTouchHelper.ACTION_STATE_DRAG) return

        if (dY == 0.0f) return

        val position = viewHolder.bindingAdapterPosition
        val adapterItemCount = recyclerView.adapter?.itemCount ?: return

        val isDraggingDown = dY > 0.0f
        if ((isDraggingDown && position == adapterItemCount - 1) ||
            (!isDraggingDown && position == 0)) return

        val draggedViewEdgeY = if (isDraggingDown) {
            viewHolder.itemView.bottom + dY
        } else {
            viewHolder.itemView.top + dY
        }
        val targetPosition = if (isDraggingDown) { position + 1 } else { position - 1 }
        if (lastTargetPosition != targetPosition) {
            _targetView = recyclerView
                .findViewHolderForAdapterPosition(targetPosition)?.itemView ?: return
            lastTargetPosition = targetPosition
            alreadySwapped = false
        }
        val targetView = requireNotNull(_targetView)
        val targetViewHalfHeightY = targetView.top + targetView.height / 2
        if ((isDraggingDown && targetViewHalfHeightY < draggedViewEdgeY) ||
            (!isDraggingDown && targetViewHalfHeightY > draggedViewEdgeY)) {
            if (alreadySwapped) return
            onItemsSwap(position, targetPosition)
            alreadySwapped = true
        }
    }
}