package de.herrmann_engel.rbv.activities

/**
 * Activity that can delete or move cards via [de.herrmann_engel.rbv.actions.CardActions]
 * and gets notified afterwards.
 */
abstract class CardActionsActivity : FileTools() {
    abstract fun deletedCards(cardIds: ArrayList<Int>)
    abstract fun movedCards(cardIds: ArrayList<Int>)
}
