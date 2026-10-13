package de.herrmann_engel.rbv.activities

/**
 * Activity that can delete or move packs via [de.herrmann_engel.rbv.actions.PackActions]
 * and gets notified afterwards.
 */
abstract class PackActionsActivity : RBVActivity() {
    abstract fun deletedPacks(packIds: ArrayList<Int>)
    abstract fun movedPacks(packIds: ArrayList<Int>)
}
