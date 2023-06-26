package de.herrmann_engel.rbv.activities

abstract class CardActionsActivity : FileTools() {
    abstract fun deletedCards(cardIds: ArrayList<Int>)
    abstract fun movedCards(cardIds: ArrayList<Int>)
}
