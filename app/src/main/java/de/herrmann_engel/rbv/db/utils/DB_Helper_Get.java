package de.herrmann_engel.rbv.db.utils;

import static android.content.Context.MODE_PRIVATE;
import static de.herrmann_engel.rbv.Globals.LIST_ACCURATE_SIZE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.utils.StringTools;
import io.noties.markwon.Markwon;

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

    public int countPacksInCollection(int collection) {
        return dbHelper.pack_dao.countPacksInCollection(collection);
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

    //Get All: Cards
    private List<DB_Card> formatCards(List<DB_Card> list) {
        SharedPreferences settings = context.getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean formatCardsNotes = settings.getBoolean("format_card_notes", false);
        if (formatCards || formatCardsNotes) {
            StringTools formatString = new StringTools();
            if (list.size() > LIST_ACCURATE_SIZE) {
                list.forEach(l -> {
                    if (formatCards) {
                        l.front = formatString.unformat(l.front);
                        l.back = formatString.unformat(l.back);
                    }
                    if (formatCardsNotes) {
                        l.notes = formatString.unformat(l.notes);
                    }
                });
            } else {
                Markwon markwon = Markwon.create(context);
                list.forEach(l -> {
                    if (formatCards) {
                        l.front = formatString.format(l.front).toString();
                        l.back = formatString.format(l.back).toString();
                    }
                    if (formatCardsNotes) {
                        l.notes = markwon.toMarkdown(l.notes).toString();
                    }
                });
            }
        }
        return list;
    }

    public List<DB_Card> getAllCards() {
        return formatCards(dbHelper.card_dao.getAll());
    }

    public List<DB_Card> getAllCardsByIds(ArrayList<Integer> ids) {
        List<DB_Card> list = new ArrayList<>();
        ids.forEach(id -> {
            try {
                list.add(getSingleCard(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return formatCards(list);
    }

    public List<DB_Card> getAllCardsByProgress(boolean progressGreater, int progressNumber) {
        List<DB_Card> cards = new ArrayList<>();
        if (progressNumber >= 0) {
            if (progressGreater) {
                cards.addAll(dbHelper.card_dao.getAllGreaterEqual(progressNumber));
            } else {
                cards.addAll(dbHelper.card_dao.getAllLessEqual(progressNumber));
            }
        } else {
            cards.addAll(dbHelper.card_dao.getAll());
        }
        return formatCards(cards);
    }

    public List<DB_Card> getAllCardsByCollection(int collection) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        List<DB_Card> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPack(currentPack.uid)));
        return formatCards(list);
    }

    public List<DB_Card> getAllCardsByPack(int pack) {
        return formatCards(dbHelper.card_dao.getAll(pack));
    }

    public List<DB_Card> getAllCardsByPacksAndProgress(List<Integer> packs, boolean progressGreater, int progressNumber) {
        List<DB_Card> cards = new ArrayList<>();
        packs.forEach(pack -> {
            if (progressNumber >= 0) {
                if (progressGreater) {
                    cards.addAll(dbHelper.card_dao.getAllGreaterEqual(pack, progressNumber));
                } else {
                    cards.addAll(dbHelper.card_dao.getAllLessEqual(pack, progressNumber));
                }
            } else {
                cards.addAll(dbHelper.card_dao.getAll(pack));
            }
        });
        return formatCards(cards);
    }

    public List<DB_Card> getAllCardsByPackAndFrontAndBackAndNotes(int pack, String front, String back, String notes) {
        return dbHelper.card_dao.getAllByPackAndFrontAndBackAndNotes(pack, front, back, notes);
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

    public List<Integer> getAllMediaLinkCardIdsByMedia(int file) {
        return dbHelper.media_link_card_dao.getAllCardIdsByMedia(file);
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
