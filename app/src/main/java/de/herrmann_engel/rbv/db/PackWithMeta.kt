package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import java.util.Objects

/**
 * A pack together with the name of its collection and the number of cards in it.
 */
class PackWithMeta {
    @Embedded
    lateinit var pack: DbPack

    // Number of cards in the pack
    var counter: Int = 0
    var collectionName: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PackWithMeta) return false
        return counter == other.counter &&
            pack == other.pack &&
            collectionName == other.collectionName
    }

    override fun hashCode(): Int {
        return Objects.hash(pack, counter, collectionName)
    }
}
