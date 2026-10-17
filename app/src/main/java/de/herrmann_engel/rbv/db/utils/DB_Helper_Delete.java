package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;

/**
 * Deletes entries from the database.
 */
public class DB_Helper_Delete {

    // Stays below SQLite's limit of 999 bound parameters per statement
    private static final int MAX_PARAMETERS = 500;

    private final DB_Helper dbHelper;

    public DB_Helper_Delete(Context context) {
        dbHelper = new DB_Helper(context);
    }

    /**
     * Deletes a collection. If it still contains packs, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned.
     */
    public boolean deleteCollection(DB_Collection collection, boolean force) {
        return dbHelper.database.runInTransaction(() -> {
            DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
            List<DB_Pack> packs = dbHelperGet.getAllPacksByCollection(collection.uid);
            boolean containsPacks = !packs.isEmpty();
            if (containsPacks && force) {
                packs.forEach(pack -> deletePack(pack, true));
            } else if (containsPacks) {
                return false;
            }
            dbHelper.collection_dao.delete(collection);
            return true;
        });
    }

    /**
     * Deletes a pack. If it still contains cards, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned. As with deleteCard, media files no
     * longer linked to any card are deleted too, and so are tags no longer assigned to any card.
     */
    public boolean deletePack(DB_Pack pack, boolean force) {
        return dbHelper.database.runInTransaction(() -> {
            DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
            boolean containsCards = dbHelperGet.countCardsInPack(pack.uid) > 0;
            if (containsCards && !force) {
                return false;
            }
            List<Integer> fileIds = dbHelper.media_link_card_dao.getAllMediaIdsByPack(pack.uid);
            List<Integer> tagIds = dbHelper.tag_link_card_dao.getAllTagIdsByPack(pack.uid);
            // Deleting the pack also deletes its cards and their links (cascade)
            dbHelper.pack_dao.delete(pack);
            deleteUnusedTags(tagIds);
            deleteUnusedMedia(fileIds);
            return true;
        });
    }

    /**
     * Deletes a card and its media and tag links. Media files and tags no longer linked to any card
     * are deleted too.
     */
    public boolean deleteCard(DB_Card card) {
        return dbHelper.database.runInTransaction(() -> {
            List<Integer> fileIds = dbHelper.media_link_card_dao.getAllMediaIdsByCard(card.uid);
            List<Integer> tagIds = dbHelper.tag_link_card_dao.getAllTagIdsByCard(card.uid);
            // Deleting the card also deletes its media and tag links (cascade)
            dbHelper.card_dao.delete(card);
            deleteUnusedTags(tagIds);
            deleteUnusedMedia(fileIds);
            return true;
        });
    }

    // Deletes a media entry; its links are deleted with it (cascade)
    public void deleteMedia(int file) {
        dbHelper.media_dao.deleteMedia(file);
    }

    // Removes a media file from a card; returns true if the media entry was deleted because it is
    // no longer used
    public boolean deleteMediaLink(int file, int card) {
        return dbHelper.database.runInTransaction(() -> {
            dbHelper.media_link_card_dao.deleteMediaLinkCard(file, card);
            return deleteUnusedMedia(Collections.singletonList(file)) > 0;
        });
    }

    // Removes a tag from a card; returns true if the tag was deleted because it is no longer used
    public boolean deleteTagLink(int tag, int card) {
        return dbHelper.database.runInTransaction(() -> {
            dbHelper.tag_link_card_dao.deleteTagLinkCard(tag, card);
            return deleteUnusedTags(Collections.singletonList(tag)) > 0;
        });
    }

    /**
     * Deletes those of the given media entries that are no longer linked to any card and returns
     * their number. Deleting cards only removes their media links (cascade), so the entries are
     * checked here. The files themselves are kept.
     */
    public int deleteUnusedMedia(List<Integer> files) {
        return deleteInChunks(files, dbHelper.media_dao::deleteUnusedMedia);
    }

    /**
     * Deletes those of the given tags that are no longer assigned to any card and returns their
     * number. Deleting cards only removes their tag links (cascade), so the tags are checked here.
     */
    public int deleteUnusedTags(List<Integer> tags) {
        return deleteInChunks(tags, dbHelper.tag_dao::deleteUnusedTags);
    }

    // Runs a delete query for a list of ids in chunks and returns the number of deleted rows
    private int deleteInChunks(List<Integer> ids, ToIntFunction<List<Integer>> delete) {
        int deleted = 0;
        for (int i = 0; i < ids.size(); i += MAX_PARAMETERS) {
            deleted += delete.applyAsInt(ids.subList(i, Math.min(i + MAX_PARAMETERS, ids.size())));
        }
        return deleted;
    }

}
