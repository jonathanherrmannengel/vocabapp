package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import java.util.Objects

/**
 * A collection together with the number of packs in it.
 */
class DB_Collection_With_Meta {
    @Embedded
    var collection: DB_Collection? = null

    // Number of packs in the collection, -1 if not counted
    var counter: Int = 0

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Collection_With_Meta
        return counter == that.counter && collection == that.collection
    }

    override fun hashCode(): Int {
        return Objects.hash(collection, counter)
    }
}
