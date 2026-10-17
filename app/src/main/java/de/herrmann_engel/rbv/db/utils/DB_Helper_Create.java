package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.DB_Tag;
import de.herrmann_engel.rbv.db.DB_Tag_Link_Card;

/**
 * Inserts new entries into the database. All methods validate their input and throw an
 * InvalidEntryException if the entry cannot be created (e.g. empty name, missing parent,
 * duplicate). They return the uid of the new entry.
 */
public class DB_Helper_Create {

    // Thrown if an entry cannot be created; the message says why
    public static class InvalidEntryException extends Exception {
        public InvalidEntryException(String message) {
            super(message);
        }
    }

    private final DB_Helper dbHelper;

    public DB_Helper_Create(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public long createCollection(String name, String desc, int colors, String emoji, long date) throws InvalidEntryException {
        if (!name.isEmpty()) {
            DB_Collection collection = new DB_Collection();
            collection.name = name;
            collection.desc = desc;
            collection.date = date;
            collection.colors = colors;
            collection.emoji = emoji;
            return dbHelper.collection_dao.insert(collection);
        }
        throw new InvalidEntryException("Collection name is empty");
    }

    // Creates a collection without color and emoji, dated now
    public long createCollection(String name, String desc) throws InvalidEntryException {
        return createCollection(name, desc, 0, null, System.currentTimeMillis() / 1000L);
    }

    public long createPack(String name, String desc, int collection, int colors, String emoji, long date)
            throws InvalidEntryException {
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
        throw new InvalidEntryException("Pack name is empty or collection " + collection + " does not exist");
    }

    // Creates a pack without emoji, dated now, inheriting the color of its collection
    public long createPack(String name, String desc, int collection) throws InvalidEntryException {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        DB_Collection parent = dbHelperGet.getSingleCollection(collection);
        if (parent == null) {
            throw new InvalidEntryException("Collection " + collection + " does not exist");
        }
        return createPack(name, desc, collection, parent.colors, null, System.currentTimeMillis() / 1000L);
    }

    public long createCard(String front, String back, String notes, int pack, int known, long date, long lastRepetition, int countRepetitions) throws InvalidEntryException {
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
            card.countRepetitions = countRepetitions;
            return dbHelper.card_dao.insert(card);
        }
        throw new InvalidEntryException("Card front or back is empty or pack " + pack + " does not exist");
    }

    // Creates a card without progress or repetitions, dated now
    public long createCard(String front, String back, String notes, int pack) throws InvalidEntryException {
        return createCard(front, back, notes, pack, 0, System.currentTimeMillis() / 1000L, 0, 0);
    }

    public long createMedia(String file, String mime) throws InvalidEntryException {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsMedia(file)) {
            DB_Media media = new DB_Media();
            media.file = file;
            media.mime = mime;
            return dbHelper.media_dao.insert(media);
        }
        throw new InvalidEntryException("Media entry for file " + file + " already exists");
    }

    public long createMediaLink(int file, int card) throws InvalidEntryException {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsMediaLinkCard(file, card)) {
            DB_Media_Link_Card mediaLinkCard = new DB_Media_Link_Card();
            mediaLinkCard.file = file;
            mediaLinkCard.card = card;
            return dbHelper.media_link_card_dao.insert(mediaLinkCard);
        }
        throw new InvalidEntryException("Media file " + file + " is already linked to card " + card);
    }

    public long createTag(String tagName, String emoji, String color) throws InvalidEntryException {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsTag(tagName)) {
            DB_Tag newTag = new DB_Tag();
            newTag.name = tagName;
            newTag.emoji = emoji;
            newTag.color = color;
            return dbHelper.tag_dao.insert(newTag);
        }
        throw new InvalidEntryException("Tag " + tagName + " already exists");
    }

    public long createTag(String tagName) throws InvalidEntryException {
        return createTag(tagName, null, null);
    }

    public long createTagLink(int tag, int card) throws InvalidEntryException {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        if (!dbHelperGet.existsTagLinkCard(tag, card)) {
            DB_Tag_Link_Card tagLinkCard = new DB_Tag_Link_Card();
            tagLinkCard.tag = tag;
            tagLinkCard.card = card;
            return dbHelper.tag_link_card_dao.insert(tagLinkCard);
        }
        throw new InvalidEntryException("Tag " + tag + " is already linked to card " + card);
    }

    // Links a tag to a card by name, creating the tag first if it does not exist yet
    public long createTagLink(String tagName, int card) throws InvalidEntryException {
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
