package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import java.util.Objects

/**
 * A pack together with the name of its collection and the number of cards in it.
 */
class DB_Pack_With_Meta {
    @JvmField
    @Embedded
    var pack: DB_Pack? = null

    // Number of cards in the pack, -1 if not counted
    @JvmField
    var counter: Int = 0
    @JvmField
    var collectionName: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Pack_With_Meta
        return counter == that.counter && pack == that.pack && collectionName == that.collectionName
    }

    override fun hashCode(): Int {
        return Objects.hash(pack, counter, collectionName)
    }
}
