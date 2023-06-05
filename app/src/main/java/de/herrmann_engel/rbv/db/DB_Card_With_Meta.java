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
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Card_With_Meta compareCard)) {
            return false;
        }
        if (packColor != compareCard.packColor || !Objects.equals(formattedFront, compareCard.formattedFront) || !Objects.equals(formattedNotes, compareCard.formattedNotes) || !Objects.equals(formattedBack, compareCard.formattedBack) || formattingIsInaccurate != compareCard.formattingIsInaccurate) {
            return false;
        }
        if (card == null || compareCard.card == null) {
            return card == compareCard.card;
        }
        return card.equals(compareCard.card);
    }
}
