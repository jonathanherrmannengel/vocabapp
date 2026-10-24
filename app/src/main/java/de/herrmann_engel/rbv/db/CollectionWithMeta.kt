package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import java.util.Objects

/**
 * A collection together with the number of packs in it.
 */
class CollectionWithMeta {
    @Embedded
    lateinit var collection: DbCollection

    // Number of packs in the collection
    var counter: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionWithMeta) return false
        return counter == other.counter &&
            collection == other.collection
    }

    override fun hashCode(): Int {
        return Objects.hash(collection, counter)
    }
}
