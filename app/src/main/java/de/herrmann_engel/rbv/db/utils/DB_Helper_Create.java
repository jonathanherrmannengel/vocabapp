package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;

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
        dbHelperGet.getSingleCollection(collection);
        if (!name.isEmpty()) {
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
        dbHelperGet.getSinglePack(pack);
        if (!front.isEmpty() && !back.isEmpty()) {
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
}
