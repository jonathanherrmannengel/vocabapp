package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.DB_Tag;
import de.herrmann_engel.rbv.db.DB_Tag_Link_Card;

public class DB_Helper_Create {

    private final DB_Helper dbHelper;

    public DB_Helper_Create(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public long createCollection(String name, String desc, int colors, String emoji, long date) throws Exception {
        if (!name.isEmpty()) {
            DB_Collection collection = new DB_Collection();
            collection.name = name;
            collection.desc = desc;
            collection.date = date;
            collection.colors = colors;
            collection.emoji = emoji;
            return dbHelper.collection_dao.insert(collection);
        }
        throw new Exception();
    }

    public long createCollection(String name, String desc) throws Exception {
        return createCollection(name, desc, 0, null, System.currentTimeMillis() / 1000L);
    }

    public long createPack(String name, String desc, int collection, int colors, String emoji, long date)
            throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (dbHelperGet.getSingleCollection(collection) != null && !name.isEmpty()) {
            DB_Pack pack = new DB_Pack();
            pack.name = name;
            pack.desc = desc;
            pack.date = date;
            pack.colors = colors;
            pack.emoji = emoji;
            pack.collection = collection;
            return dbHelper.pack_dao.insert(pack);
        }
        throw new Exception();
    }

    public long createPack(String name, String desc, int collection) throws Exception {
        return createPack(name, desc, collection, 0, null, System.currentTimeMillis() / 1000L);
    }

    public long createCard(String front, String back, String notes, int pack, int known, long date, long lastRepetition) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (dbHelperGet.getSinglePack(pack) != null && !front.isEmpty() && !back.isEmpty()) {
            DB_Card card = new DB_Card();
            card.front = front;
            card.back = back;
            card.pack = pack;
            card.known = known;
            card.date = date;
            card.notes = notes;
            card.lastRepetition = lastRepetition;
            return dbHelper.card_dao.insert(card);
        }
        throw new Exception();
    }

    public long createCard(String front, String back, String notes, int pack) throws Exception {
        return createCard(front, back, notes, pack, 0, System.currentTimeMillis() / 1000L, 0);
    }

    public long createMedia(String file, String mime) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsMedia(file)) {
            DB_Media media = new DB_Media();
            media.file = file;
            media.mime = mime;
            return dbHelper.media_dao.insert(media);
        }
        throw new Exception();
    }

    public long createMediaLink(int file, int card) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsMediaLinkCard(file, card)) {
            DB_Media_Link_Card mediaLinkCard = new DB_Media_Link_Card();
            mediaLinkCard.file = file;
            mediaLinkCard.card = card;
            return dbHelper.media_link_card_dao.insert(mediaLinkCard);
        }
        throw new Exception();
    }

    public long createTag(String tagName, String emoji, String color) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsTag(tagName)) {
            DB_Tag newTag = new DB_Tag();
            newTag.name = tagName;
            newTag.emoji = emoji;
            newTag.color = color;
            return dbHelper.tag_dao.insert(newTag);
        }
        throw new Exception();
    }

    public long createTag(String tagName) throws Exception {
        return createTag(tagName, null, null);
    }

    public long createTagLink(int tag, int card) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsTagLinkCard(tag, card)) {
            DB_Tag_Link_Card tagLinkCard = new DB_Tag_Link_Card();
            tagLinkCard.tag = tag;
            tagLinkCard.card = card;
            return dbHelper.tag_link_card_dao.insert(tagLinkCard);
        }
        throw new Exception();
    }

    public long createTagLink(String tagName, int card) throws Exception {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        int tag;
        if (dbHelperGet.existsTag(tagName)) {
            tag = dbHelperGet.getSingleTag(tagName).uid;
        } else {
            tag = (int) createTag(tagName);
        }
        return createTagLink(tag, card);
    }
}
