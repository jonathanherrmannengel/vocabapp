package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import java.util.List;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Card_With_Meta;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta;
import de.herrmann_engel.rbv.db.DB_Tag;

public class DB_Helper_Get {

    private final DB_Helper dbHelper;

    public DB_Helper_Get(Context context) {
        dbHelper = new DB_Helper(context);
    }

    // Check if has content
    public boolean hasCollections() {
        return dbHelper.collection_dao.hasCollections();
    }

    public boolean hasCards() {
        return dbHelper.card_dao.hasCards();
    }

    public boolean hasMedia() {
        return dbHelper.media_dao.hasMedia();
    }

    public boolean mediaHasLink(int file) {
        return dbHelper.media_link_card_dao.mediaHasLink(file);
    }

    public boolean hasTags() {
        return dbHelper.tag_dao.hasTags();
    }

    // Count
    public int countCollections() {
        return dbHelper.collection_dao.countCollections();
    }

    public int countPacks() {
        return dbHelper.pack_dao.countPacks();
    }

    public int countPacksInCollection(int collection) {
        return dbHelper.pack_dao.countPacksInCollection(collection);
    }

    public int countCards() {
        return dbHelper.card_dao.countCards();
    }

    public int countCardsInCollection(int collection) {
        return dbHelper.card_dao.countCardsInCollection(collection);
    }

    public int countCardsInPack(int pack) {
        return dbHelper.card_dao.countCardsInPack(pack);
    }

    // Exists Entry?
    public boolean existsMedia(String file) {
        return dbHelper.media_dao.existsMedia(file);
    }

    public boolean existsTag(String tagName) {
        return dbHelper.tag_dao.existsTag(tagName);
    }

    public boolean existsMediaLinkCard(int file, int card) {
        return dbHelper.media_link_card_dao.existsMediaLinkCard(file, card);
    }

    public boolean existsTagLinkCard(int tag, int card) {
        return dbHelper.tag_link_card_dao.existsTagLinkCard(tag, card);
    }

    // Get One
    public DB_Collection getSingleCollection(int collection) {
        return dbHelper.collection_dao.getOne(collection);
    }

    public DB_Pack getSinglePack(int pack) {
        return dbHelper.pack_dao.getOne(pack);
    }

    public DB_Card getSingleCard(int card) {
        return dbHelper.card_dao.getOne(card);
    }

    public DB_Card_With_Meta getSingleCardWithMeta(int cardId) {
        return dbHelper.card_dao.getOneWithMeta(cardId);
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

    public DB_Tag getSingleTag(String tagName) {
        return dbHelper.tag_dao.getSingleTag(tagName);
    }

    public DB_Tag getSingleTag(int id) {
        return dbHelper.tag_dao.getSingleTag(id);
    }

    // Get All: Collections
    public List<DB_Collection> getAllCollections() {
        return dbHelper.collection_dao.getAll();
    }

    public List<DB_Collection> getAllCollectionsByName(String name) {
        return dbHelper.collection_dao.getAllByName(name);
    }

    public List<DB_Collection_With_Meta> getAllCollectionsWithMeta() {
        return dbHelper.collection_dao.getAllWithMeta();
    }

    public List<DB_Collection_With_Meta> getAllCollectionsWithMetaNoCounter() {
        return dbHelper.collection_dao.getAllWithMetaNoCounter();
    }

    // Get All: Packs
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

    public List<DB_Pack_With_Meta> getAllPacksWithMetaNoCounter() {
        return dbHelper.pack_dao.getAllWithMetaNoCounter();
    }

    public List<DB_Pack_With_Meta> getAllPacksWithMetaByCollection(int collection) {
        return dbHelper.pack_dao.getAllByCollectionWithMeta(collection);
    }

    public List<DB_Pack_With_Meta> getAllPacksWithMetaNoCounterByCollection(int collection) {
        return dbHelper.pack_dao.getAllByCollectionWithMetaNoCounter(collection);
    }

    // Get All: Cards
    public List<DB_Card_With_Meta> getAllCardsWithMeta() {
        return dbHelper.card_dao.getAllWithMeta();
    }

    public List<DB_Card_With_Meta> getAllCardsByPackWithMeta(int pack) {
        return dbHelper.card_dao.getAllByPackWithMeta(pack);
    }

    public List<DB_Card_With_Meta> getAllCardsByCollectionWithMeta(int collection) {
        return dbHelper.card_dao.getAllByCollectionWithMeta(collection);
    }

    public List<DB_Card_With_Meta> getAllCardsWithMetaFiltered(List<Integer> packs, List<Integer> tags, boolean progressGreater, int progressNumber, boolean repetitionOlder, int repetitionNumber) {
        long date = System.currentTimeMillis() / 1000L - repetitionNumber * 86400L;
        if (tags == null && packs == null) {
            if (repetitionNumber >= 0 && progressNumber >= 0) {
                if (progressGreater && repetitionOlder) {
                    return (dbHelper.card_dao.getAllGreaterEqualOlderRepetitionWithMeta(progressNumber, date));
                } else if (progressGreater) {
                    return (dbHelper.card_dao.getAllGreaterEqualNewerEqualRepetitionWithMeta(progressNumber, date));
                } else if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllLessEqualOlderRepetitionWithMeta(progressNumber, date));
                } else {
                    return (dbHelper.card_dao.getAllLessEqualNewerEqualRepetitionWithMeta(progressNumber, date));
                }
            } else if (progressNumber >= 0) {
                if (progressGreater) {
                    return (dbHelper.card_dao.getAllGreaterEqualWithMeta(progressNumber));
                } else {
                    return (dbHelper.card_dao.getAllLessEqualWithMeta(progressNumber));
                }
            } else if (repetitionNumber >= 0) {
                if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllOlderRepetitionWithMeta(date));
                } else {
                    return (dbHelper.card_dao.getAllNewerEqualRepetitionWithMeta(date));
                }
            } else {
                return (dbHelper.card_dao.getAllWithMeta());
            }
        } else if (tags == null) {
            if (repetitionNumber >= 0 && progressNumber >= 0) {
                if (progressGreater && repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPacksGreaterEqualOlderRepetitionWithMeta(packs, progressNumber, date));
                } else if (progressGreater) {
                    return (dbHelper.card_dao.getAllByPacksGreaterEqualNewerEqualRepetitionWithMeta(packs, progressNumber, date));
                } else if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPacksLessEqualOlderRepetitionWithMeta(packs, progressNumber, date));
                } else {
                    return (dbHelper.card_dao.getAllByPacksLessEqualNewerEqualRepetitionWithMeta(packs, progressNumber, date));
                }
            } else if (progressNumber >= 0) {
                if (progressGreater) {
                    return (dbHelper.card_dao.getAllByPacksGreaterEqualWithMeta(packs, progressNumber));
                } else {
                    return (dbHelper.card_dao.getAllByPacksLessEqualWithMeta(packs, progressNumber));
                }
            } else if (repetitionNumber >= 0) {
                if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPacksOlderRepetitionWithMeta(packs, date));
                } else {
                    return (dbHelper.card_dao.getAllByPacksNewerEqualRepetitionWithMeta(packs, date));
                }
            } else {
                return (dbHelper.card_dao.getAllByPacksWithMeta(packs));
            }
        } else if (packs == null) {
            if (repetitionNumber >= 0 && progressNumber >= 0) {
                if (progressGreater && repetitionOlder) {
                    return (dbHelper.card_dao.getAllByTagsGreaterEqualOlderRepetitionWithMeta(tags, progressNumber, date));
                } else if (progressGreater) {
                    return (dbHelper.card_dao.getAllByTagsGreaterEqualNewerEqualRepetitionWithMeta(tags, progressNumber, date));
                } else if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByTagsLessEqualOlderRepetitionWithMeta(tags, progressNumber, date));
                } else {
                    return (dbHelper.card_dao.getAllByTagsLessEqualNewerEqualRepetitionWithMeta(tags, progressNumber, date));
                }
            } else if (progressNumber >= 0) {
                if (progressGreater) {
                    return (dbHelper.card_dao.getAllByTagsGreaterEqualWithMeta(tags, progressNumber));
                } else {
                    return (dbHelper.card_dao.getAllByTagsLessEqualWithMeta(tags, progressNumber));
                }
            } else if (repetitionNumber >= 0) {
                if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByTagsOlderRepetitionWithMeta(tags, date));
                } else {
                    return (dbHelper.card_dao.getAllByTagsNewerEqualRepetitionWithMeta(tags, date));
                }
            } else {
                return (dbHelper.card_dao.getAllByTagsWithMeta(tags));
            }
        } else {
            if (repetitionNumber >= 0 && progressNumber >= 0) {
                if (progressGreater && repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPackAndTagsGreaterEqualOlderRepetitionWithMeta(packs, tags, progressNumber, date));
                } else if (progressGreater) {
                    return (dbHelper.card_dao.getAllByPackAndTagsGreaterEqualNewerEqualRepetitionWithMeta(packs, tags, progressNumber, date));
                } else if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPackAndTagsLessEqualOlderRepetitionWithMeta(packs, tags, progressNumber, date));
                } else {
                    return (dbHelper.card_dao.getAllByPackAndTagsLessEqualNewerEqualRepetitionWithMeta(packs, tags, progressNumber, date));
                }
            } else if (progressNumber >= 0) {
                if (progressGreater) {
                    return (dbHelper.card_dao.getAllByPackAndTagsGreaterEqualWithMeta(packs, tags, progressNumber));
                } else {
                    return (dbHelper.card_dao.getAllByPackAndTagsLessEqualWithMeta(packs, tags, progressNumber));
                }
            } else if (repetitionNumber >= 0) {
                if (repetitionOlder) {
                    return (dbHelper.card_dao.getAllByPackAndTagsOlderRepetitionWithMeta(packs, tags, date));
                } else {
                    return (dbHelper.card_dao.getAllByPackAndTagsNewerEqualRepetitionWithMeta(packs, tags, date));
                }
            } else {
                return (dbHelper.card_dao.getAllByPacksAndTagsWithMeta(packs, tags));
            }
        }
    }

    public List<DB_Card_With_Meta> getAllCardsByMediaWithMeta(int mediaId) {
        return dbHelper.card_dao.getAllByMedia(mediaId);
    }

    // Get All: Media
    public List<DB_Media> getAllMedia() {
        return dbHelper.media_dao.getAll();
    }

    // Get All: Media Links
    private boolean isPhoto(String mime) {
        return mime.equals("image/png") || mime.equals("image/jpeg") || mime.equals("image/webp");
    }

    public List<DB_Media_Link_Card> getAllMediaLinksByCard(int card) {
        return dbHelper.media_link_card_dao.getAllByCard(card);
    }

    public List<Integer> getAllMediaLinkFileIdsByCard(int card) {
        return dbHelper.media_link_card_dao.getAllMediaIdsByCard(card);
    }

    public List<DB_Media_Link_Card> getImageMediaLinksByCard(int card) {
        List<DB_Media_Link_Card> list = dbHelper.media_link_card_dao.getAllByCard(card);
        list.removeIf(l -> {
            DB_Media file = getSingleMedia(l.file);
            return file == null || !isPhoto(file.mime);
        });
        return list;
    }

    // Get All: Tags
    public List<DB_Tag> getAllTags() {
        return dbHelper.tag_dao.getAll();
    }

    public List<DB_Tag> getCardTags(int cardId) {
        return dbHelper.tag_dao.getAllByCard(cardId);
    }
}
