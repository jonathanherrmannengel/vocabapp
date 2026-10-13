package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;
import androidx.room.Ignore;

import java.util.Objects;

/**
 * A card together with data needed to display it in lists: the color of its pack and the
 * names (and emojis) of its tags. The ignored fields are computed in memory and not stored.
 */
public class DB_Card_With_Meta {
    @Embedded
    public DB_Card card;
    public int packColor;
    public String tagNames;
    // Front and back with formatting markup (*, _) removed; used for searching and sorting
    @Ignore
    public String formattedFront;
    @Ignore
    public String formattedBack;
    // True if the faster, less accurate unformatting was used (very large lists)
    @Ignore
    public boolean formattingIsInaccurate;
    // Temporary weight for the "mixed" sort order
    @Ignore
    public double mixedWeight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Card_With_Meta that = (DB_Card_With_Meta) o;
        return packColor == that.packColor && formattingIsInaccurate == that.formattingIsInaccurate && Objects.equals(card, that.card) && Objects.equals(tagNames, that.tagNames) && Objects.equals(formattedFront, that.formattedFront) && Objects.equals(formattedBack, that.formattedBack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card, packColor, tagNames, formattedFront, formattedBack, formattingIsInaccurate);
    }
}
