package com.kursor.chroniclesofww2.presentation.hudViews

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.kursor.chroniclesofww2.getFlagResId
import com.kursor.chroniclesofww2.objects.Tools
import com.kursor.chroniclesofww2.model.data.Player
import com.kursor.chroniclesofww2.model.game.DivisionResources
import com.kursor.chroniclesofww2.model.game.Reserve

class DivisionResourcesView(
    context: Context,
    attributeSet: AttributeSet? = null
) : LinearLayout(context, attributeSet) {

    init {
        orientation = HORIZONTAL
    }

    fun setup(player: Player) {
        divisionResources = player.divisionResources
        setBackgroundResource(player.nation.getFlagResId())
    }

    var divisionResources: DivisionResources? = null
        set(value) {
            if (value != null) init(value)
            field = value
        }

    lateinit var reserveViews: List<ReserveView>

    private var clickedReserveView: ReserveView? = null
        set(value) {
            if (field != null) field!!.isClicked = false
            if (value != null) value.isClicked = true
            field = value
        }

    private fun init(divisionResources: DivisionResources) {

        val tempReserveViews = mutableListOf<ReserveView>()
        val width = Tools.getScreenWidth() / reserveViews.size

        divisionResources.reserves.forEach { (_, reserve) ->
            val reserveView = ReserveView(context).apply { this.reserve = reserve }
            tempReserveViews.add(reserveView)

            reserveView.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
            addView(reserveView)
            reserveView.setOnClickListener {
                if (clickedReserveView === reserveView) {
                    reserveView.isClicked = false
                    clickedReserveView = null
                } else clickedReserveView = reserveView
                reserveView.onReserveViewClickListener.onClick(reserveView)
            }
        }
        reserveViews = tempReserveViews
    }

    fun setOnReserveClickListener(onReserveViewClickListener: ReserveView.OnReserveViewClickListener) {
        reserveViews.forEach {
            it.onReserveViewClickListener = onReserveViewClickListener
        }
    }
}

class ReserveView(
    context: Context,
    attributeSet: AttributeSet? = null
) : AppCompatButton(context, attributeSet) {

    var isClicked = false
        set(value) {
            setTextColor(
                if (value) Color.GRAY
                else Color.BLACK
            )
            field = value
        }

    var reserve: Reserve? = null
        set(value) {
            if (value != null) init(value)
            field = value
        }

    lateinit var onReserveViewClickListener: OnReserveViewClickListener

    private fun init(reserve: Reserve) {
        text = "${reserve.type}: ${reserve.size}"
        reserve.listener = object : Reserve.Listener {
            override fun onGetNewDivision() {
                updateText()
            }

            override fun onCancel() {
                updateText()
            }
        }
    }

    fun updateText() {
        text = "${reserve!!.type}: ${reserve!!.size}"
    }

    fun interface OnReserveViewClickListener {
        fun onClick(reserveView: ReserveView)
    }

}

