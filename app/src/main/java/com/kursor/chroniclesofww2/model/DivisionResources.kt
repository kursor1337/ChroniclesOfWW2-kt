package com.kursor.chroniclesofww2.model

import com.kursor.chroniclesofww2.model.board.Division

class DivisionResources(
    resMap: Map<Division.Type, Int>,
    val playerName: String
) {

    val resources =
        resMap.map { (type, quantity) -> type to Reserve(type, quantity, playerName) }.toMap()

    var lastReturned: Division.Type? = null


    fun getNewDivision(type: Division.Type): Division? {
        lastReturned = type
        return resources[type]?.getNewDivision()
    }

    fun cancel() {
        if (lastReturned == null) return
        resources[lastReturned]!!.cancel()
    }

    companion object {
        fun getDefaultInstance(playerName: String) = DivisionResources(
            mapOf(
                Division.Type.INFANTRY to 9,
                Division.Type.ARMORED to 3,
                Division.Type.ARTILLERY to 3
            ), playerName
        )
    }
}

class Reserve(
    val type: Division.Type,
    var size: Int,
    val playerName: String
) {

    val isEmpty: Boolean
        get() = size == 0

    var listener: Listener? = null

    fun getNewDivision(): Division? {
        if (!isEmpty) {
            size--
            listener?.onGetNewDivision()
            return Division.newInstance(type, playerName)
        }
        return null
    }

    fun cancel() {
        size++
        listener?.onCancel()
    }

    interface Listener {
        fun onGetNewDivision()
        fun onCancel()
    }
}



