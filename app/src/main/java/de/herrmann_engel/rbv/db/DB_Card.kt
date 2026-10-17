package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Objects

/**
 * A single flashcard with a front and a back side, belonging to a pack.
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = DB_Pack::class,
        parentColumns = ["uid"],
        childColumns = ["pack"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("pack")]
)
class DB_Card {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "front")
    var front: String = ""

    @ColumnInfo(name = "back")
    var back: String = ""

    // uid of the pack the card belongs to
    @ColumnInfo(name = "pack")
    var pack: Int = 0

    // Learning progress: how often the card has been marked as known (decreases when it is not known)
    @ColumnInfo(name = "known")
    var known: Int = 0

    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    var date: Long = 0

    @ColumnInfo(name = "notes")
    var notes: String? = null

    // Date of the last repetition (Unix time in seconds, 0 if never repeated)
    @ColumnInfo(name = "last_repetition")
    var lastRepetition: Long = 0

    // Total number of repetitions
    @ColumnInfo(name = "count_repetitions")
    var countRepetitions: Int = 0

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Card
        return uid == that.uid && pack == that.pack && known == that.known && front == that.front && back == that.back && notes == that.notes
    }

    override fun hashCode(): Int {
        return Objects.hash(uid, front, back, pack, known, notes)
    }
}
