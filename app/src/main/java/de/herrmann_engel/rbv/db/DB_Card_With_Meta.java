package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

public class DB_Card_With_Meta {
    @Embedded
    public DB_Card card;

    public int packColor;

    public String formattedFront;

    public String formattedBack;

    public String formattedNotes;
}
