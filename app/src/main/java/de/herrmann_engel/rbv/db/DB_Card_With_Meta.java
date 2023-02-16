package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;
import androidx.room.Ignore;

public class DB_Card_With_Meta {
    @Embedded
    public DB_Card card;

    public int packColor;

    @Ignore
    public String formattedFront;

    @Ignore
    public String formattedBack;

    @Ignore
    public String formattedNotes;

    @Ignore
    public boolean formattingIsInaccurate;
}
