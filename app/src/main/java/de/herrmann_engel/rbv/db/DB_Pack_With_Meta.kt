package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import java.util.Objects

/**
 * A pack together with the name of its collection and the number of cards in it.
 */
class DB_Pack_With_Meta {
    @Embedded
    lateinit var pack: DB_Pack

    // Number of cards in the pack, -1 if not counted
    var counter: Int = 0
    var collectionName: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DB_Pack_With_Meta) return false
        return counter == other.counter &&
            pack == other.pack &&
            collectionName == other.collectionName
    }

    override fun hashCode(): Int {
        return Objects.hash(pack, counter, collectionName)
    }
}
