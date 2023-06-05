package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;
import androidx.room.Ignore;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Card_With_Meta that = (DB_Card_With_Meta) o;
        return packColor == that.packColor && formattingIsInaccurate == that.formattingIsInaccurate && Objects.equals(card, that.card) && Objects.equals(formattedFront, that.formattedFront) && Objects.equals(formattedBack, that.formattedBack) && Objects.equals(formattedNotes, that.formattedNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card, packColor, formattedFront, formattedBack, formattedNotes, formattingIsInaccurate);
    }
}
