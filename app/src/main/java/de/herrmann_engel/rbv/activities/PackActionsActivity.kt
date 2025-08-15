package de.herrmann_engel.rbv.activities

abstract class PackActionsActivity : RBVActivity() {
    abstract fun deletedPacks(packIds: ArrayList<Int>)
    abstract fun movedPacks(packIds: ArrayList<Int>)
}
