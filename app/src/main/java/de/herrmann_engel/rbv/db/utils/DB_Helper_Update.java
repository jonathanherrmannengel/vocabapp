package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.DB_Tag;

public class DB_Helper_Update {

    private final DB_Helper dbHelper;

    public DB_Helper_Update(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public boolean updateCollection(DB_Collection collection) {
        if (collection.name.isEmpty()) {
            return false;
        }
        dbHelper.collection_dao.update(collection);
        return true;
    }

    public boolean updatePack(DB_Pack pack) {
        if (pack.name.isEmpty()) {
            return false;
        }
        dbHelper.pack_dao.update(pack);
        return true;
    }

    public boolean updateCard(DB_Card card) {
        if (card.front.isEmpty() || card.back.isEmpty()) {
            return false;
        }
        dbHelper.card_dao.update(card);
        return true;
    }

    public boolean updateTag(DB_Tag tag) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        // Do not allow tag name already in use
        DB_Tag existingTag = dbHelperGet.getSingleTag(tag.name);
        if (existingTag != null && existingTag.uid != tag.uid) {
            return false;
        }
        if (tag.name.isBlank()) {
            return false;
        }
        dbHelper.tag_dao.update(tag);
        return true;
    }

}
