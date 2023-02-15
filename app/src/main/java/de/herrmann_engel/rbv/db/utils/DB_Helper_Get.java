package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Card_With_Meta;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta;

public class DB_Helper_Get {

    private final DB_Helper dbHelper;
    private final Context context;

    public DB_Helper_Get(Context context) {
        dbHelper = new DB_Helper(context);
        this.context = context;
    }

    //Check if has content
    public boolean hasCollections() {
        return dbHelper.collection_dao.hasCollections();
    }

    public boolean hasCards() {
        return dbHelper.card_dao.hasCards();
    }

    public boolean hasMedia() {
        return dbHelper.media_dao.hasMedia();
    }

    //Count
    public int countPacks() {
        return dbHelper.pack_dao.countPacks();
    }

    public int countCards() {
        return dbHelper.card_dao.countCards();
    }

    public int countCardsInCollection(int collection) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        int sum = 0;
        for (DB_Pack pack : packs) {
            sum += countCardsInPack(pack.uid);
        }
        return sum;
    }

    public int countCardsInPack(int pack) {
        return dbHelper.card_dao.countCardsInPack(pack);
    }

    //Exists Entry?
    public boolean existsMedia(String file) {
        return dbHelper.media_dao.existsMedia(file);
    }

    public boolean existsMediaLinkCard(int file, int card) {
        return dbHelper.media_link_card_dao.existsMediaLinkCard(file, card);
    }

    //Get One
    public DB_Collection getSingleCollection(int collection) throws Exception {
        List<DB_Collection> list = dbHelper.collection_dao.getOne(collection);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public DB_Pack getSinglePack(int pack) throws Exception {
        List<DB_Pack> list = dbHelper.pack_dao.getOne(pack);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public DB_Card getSingleCard(int card) throws Exception {
        List<DB_Card> list = dbHelper.card_dao.getOne(card);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public DB_Card_With_Meta getSingleCardWithMeta(int cardId) throws Exception {
        DB_Card_With_Meta card = dbHelper.card_dao.getOneWithMeta(cardId);
        if (card == null) {
            throw new Exception();
        }
        return card;
    }

    public int getSingleCardIdByPackAndFrontAndBackAndNotes(int pack, String front, String back, String notes) {
        return dbHelper.card_dao.getOneIdByPackAndFrontAndBackAndNotes(pack, front, back, notes);
    }

    public DB_Media getSingleMedia(String file) {
        return dbHelper.media_dao.getSingleMedia(file);
    }

    public DB_Media getSingleMedia(int id) {
        return dbHelper.media_dao.getSingleMedia(id);
    }

    //Get All: Collections
    public List<DB_Collection> getAllCollections() {
        return dbHelper.collection_dao.getAll();
    }

    public List<DB_Collection> getAllCollectionsByName(String name) {
        return dbHelper.collection_dao.getAllByName(name);
    }

    public List<DB_Collection_With_Meta> getAllCollectionsWithMeta() {
        return dbHelper.collection_dao.getAllWithMeta();
    }

    //Get All: Packs
    public List<DB_Pack> getAllPacks() {
        return dbHelper.pack_dao.getAll();
    }

    public List<DB_Pack> getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAll(collection);
    }

    public List<DB_Pack> getAllPacksByCollectionAndNameAndDesc(int collection, String name, String desc) {
        return dbHelper.pack_dao.getAllByCollectionAndNameAndDesc(collection, name, desc);
    }

    public List<DB_Pack_With_Meta> getAllPacksWithMeta() {
        return dbHelper.pack_dao.getAllWithMeta();
    }

    public List<DB_Pack_With_Meta> getAllPacksWithMetaByCollection(int collection) {
        return dbHelper.pack_dao.getAllByCollectionWithMeta(collection);
    }

    //Get All: Cards

    public List<DB_Card_With_Meta> getAllCardsWithMeta() {
        return dbHelper.card_dao.getAllWithMeta();
    }

    public List<DB_Card_With_Meta> getAllCardsByPackWithMeta(int pack) {
        return dbHelper.card_dao.getAllByPackWithMeta(pack);
    }

    public List<DB_Card_With_Meta> getAllCardsByCollectionWithMeta(int collection) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        List<DB_Card_With_Meta> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPackWithMeta(currentPack.uid)));
        return list;
    }

    public List<DB_Card_With_Meta> getAllCardsByProgressWithMeta(boolean progressGreater, int progressNumber) {
        List<DB_Card_With_Meta> cards = new ArrayList<>();
        if (progressNumber >= 0) {
            if (progressGreater) {
                cards.addAll(dbHelper.card_dao.getAllGreaterEqualWithMeta(progressNumber));
            } else {
                cards.addAll(dbHelper.card_dao.getAllLessEqualWithMeta(progressNumber));
            }
        } else {
            cards.addAll(dbHelper.card_dao.getAllWithMeta());
        }
        return cards;
    }

    public List<DB_Card_With_Meta> getAllCardsByPacksAndProgressWithMeta(List<Integer> packs, boolean progressGreater, int progressNumber) {
        List<DB_Card_With_Meta> cards = new ArrayList<>();
        packs.forEach(pack -> {
            if (progressNumber >= 0) {
                if (progressGreater) {
                    cards.addAll(dbHelper.card_dao.getAllGreaterEqualWithMeta(pack, progressNumber));
                } else {
                    cards.addAll(dbHelper.card_dao.getAllLessEqualWithMeta(pack, progressNumber));
                }
            } else {
                cards.addAll(dbHelper.card_dao.getAllByPackWithMeta(pack));
            }
        });
        return cards;
    }

    public List<DB_Card_With_Meta> getAllCardsByMediaWithMeta(int mediaId) {
        List<Integer> cardIds = dbHelper.media_link_card_dao.getAllCardIdsByMedia(mediaId);
        List<DB_Card_With_Meta> list = new ArrayList<>();
        cardIds.forEach(id -> {
            try {
                list.add(getSingleCardWithMeta(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return list;
    }

    //Get All: Media
    public List<DB_Media> getAllMedia() {
        return dbHelper.media_dao.getAll();
    }

    //Get All: Media Links
    private boolean isPhoto(String mime) {
        return mime.equals("image/png") || mime.equals("image/jpeg") || mime.equals("image/webp");
    }

    public List<DB_Media_Link_Card> getAllMediaLinksByCard(int card) {
        return dbHelper.media_link_card_dao.getAllByCard(card);
    }

    public List<Integer> getAllMediaLinkFileIdsByCard(int card) {
        return dbHelper.media_link_card_dao.getAllMediaIdsByCard(card);
    }

    public List<DB_Media_Link_Card> getAllMediaLinksByFile(int file) {
        return dbHelper.media_link_card_dao.getAllByMedia(file);
    }

    public List<DB_Media_Link_Card> getImageMediaLinksByCard(int card) {
        List<DB_Media_Link_Card> list = dbHelper.media_link_card_dao.getAllByCard(card);
        list.removeIf(l -> {
            DB_Media file = getSingleMedia(l.file);
            return file == null || !isPhoto(file.mime);
        });
        return list;
    }

}
