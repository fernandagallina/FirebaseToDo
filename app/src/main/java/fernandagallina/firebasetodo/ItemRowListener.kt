package fernandagallina.firebasetodo

interface ItemRowListener {

    fun modifyItemState(itemObjectId: String, isDone: Boolean)

    fun onItemDelete(itemObjectId: String, position: Int)
}
