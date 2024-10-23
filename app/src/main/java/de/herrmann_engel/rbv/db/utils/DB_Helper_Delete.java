package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import java.util.List;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;

public class DB_Helper_Delete {

    private final DB_Helper dbHelper;

    public DB_Helper_Delete(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public boolean deleteCollection(DB_Collection collection, boolean force) {
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
    }

    public boolean deletePack(DB_Pack pack, boolean force) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        boolean containsCards = dbHelperGet.countCardsInPack(pack.uid) > 0;
        if (containsCards && force) {
            dbHelper.card_dao.deleteAllByPack(pack.uid);
        } else if (containsCards) {
            return false;
        }
        dbHelper.pack_dao.delete(pack);
        return true;
    }

    public boolean deleteCard(DB_Card card) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        List<Integer> fileIds = dbHelperGet.getAllMediaLinkFileIdsByCard(card.uid);
        dbHelper.media_link_card_dao.deleteMediaLinksByCard(card.uid);
        fileIds.forEach(fileId -> {
            if (!dbHelperGet.mediaHasLink(fileId)) {
                deleteMedia(fileId);
            }
        });
        dbHelper.card_dao.delete(card);
        return true;
    }

    public void deleteMedia(int file) {
        dbHelper.media_link_card_dao.deleteMediaLinksByMedia(file);
        dbHelper.media_dao.deleteMedia(file);
    }

    public void deleteMediaLink(int file, int card) {
        dbHelper.media_link_card_dao.deleteMediaLinkCard(file, card);
    }

    public void deleteTagLink(int tag, int card) {
        dbHelper.tag_link_card_dao.deleteTagLinkCard(tag, card);
        deleteDeadTags();
    }

    public void deleteDeadMediaLinks() {
        dbHelper.media_link_card_dao.deleteDeadMediaLinks();
    }

    public void deleteDeadTags() {
        dbHelper.tag_dao.deleteDeadTags();
        dbHelper.tag_link_card_dao.deleteDeadTagLinks();
    }
}
