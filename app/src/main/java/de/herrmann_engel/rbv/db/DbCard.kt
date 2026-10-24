package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single card with a front and a back side, belonging to a pack. Shown to users as
 * "flashcard".
 */
@Entity(
    tableName = "DB_Card",
    foreignKeys = [ForeignKey(
        entity = DbPack::class,
        parentColumns = ["uid"],
        childColumns = ["pack"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("pack")]
)
data class DbCard(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,

    @ColumnInfo(name = "front")
    var front: String = "",

    @ColumnInfo(name = "back")
    var back: String = "",

    // uid of the pack the card belongs to
    @ColumnInfo(name = "pack")
    var packId: Int = 0,

    // Learning progress: how often the card has been marked as known (decreases when it is not known)
    @ColumnInfo(name = "known")
    var known: Int = 0,

    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    var date: Long = 0,

    @ColumnInfo(name = "notes")
    var notes: String = "",

    // Date of the last repetition (Unix time in seconds, 0 if never repeated)
    @ColumnInfo(name = "last_repetition")
    var lastRepetition: Long = 0,

    // Total number of repetitions
    @ColumnInfo(name = "count_repetitions")
    var countRepetitions: Int = 0
)
