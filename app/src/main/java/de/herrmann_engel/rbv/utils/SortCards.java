package de.herrmann_engel.rbv.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.db.DB_Card;

public class SortCards {

    private int compareCardsAlphabetical(String a, String b) {
        return a.compareToIgnoreCase(b);
    }

    public List<DB_Card> sortCards(List<DB_Card> list, int sort, Random random) {
        List<DB_Card> sortedList = new ArrayList<>(list);
        if (sort == Globals.SORT_ALPHABETICAL) {
            sortedList.sort((a, b) -> {
                int c0 = compareCardsAlphabetical(a.front, b.front);
                if (c0 == 0) {
                    return compareCardsAlphabetical(a.back, b.back);
                }
                return c0;
            });
            return sortedList;
        } else if (sort == Globals.SORT_RANDOM) {
            if (random == null) {
                Collections.shuffle(sortedList);
            } else {
                Collections.shuffle(sortedList, random);
            }
            return sortedList;
        }
        sortedList.sort((a, b) -> {
            int c0 = Integer.compare(a.known, b.known);
            if (c0 == 0) {
                return Long.compare(b.date, a.date);
            }
            return c0;
        });
        return sortedList;
    }

    public List<DB_Card> sortCards(List<DB_Card> list, int sort) {
        return sortCards(list, sort, null);
    }
}
