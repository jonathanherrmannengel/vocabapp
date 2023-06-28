package de.herrmann_engel.rbv.activities

import androidx.appcompat.app.AppCompatActivity

abstract class PackActionsActivity : AppCompatActivity() {
    abstract fun deletedPacks(packIds: ArrayList<Int>)
    abstract fun movedPacks(packIds: ArrayList<Int>)
}
