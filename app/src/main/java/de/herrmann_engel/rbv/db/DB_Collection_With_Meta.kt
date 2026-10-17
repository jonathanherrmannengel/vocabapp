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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DB_Collection_With_Meta) return false
        return counter == other.counter &&
            collection == other.collection
    }

    override fun hashCode(): Int {
        return Objects.hash(collection, counter)
    }
}
