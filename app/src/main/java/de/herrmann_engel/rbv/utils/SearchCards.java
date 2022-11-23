package de.herrmann_engel.rbv.utils;

import static de.herrmann_engel.rbv.Globals.LIST_ACCURATE_SIZE;

import java.util.List;
import java.util.regex.Pattern;

import de.herrmann_engel.rbv.db.DB_Card;

public class SearchCards {

    private boolean compare(String source, String query) {
        if (source == null) {
            return false;
        }
        return Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(source)
                .find();
    }

    private boolean compareInaccurate(String source, String query) {
        if (source == null) {
            return false;
        }
        return source.toLowerCase().contains(query);
    }

    public List<DB_Card> searchCards(List<DB_Card> input, String query) {
        if (input.size() > LIST_ACCURATE_SIZE) {
            String queryLower = query.toLowerCase();
            input.removeIf(l -> !compareInaccurate(l.front, queryLower) && !compareInaccurate(l.back, queryLower) && !compareInaccurate(l.notes, queryLower));
        } else {
            input.removeIf(l -> !compare(l.front, query) && !compare(l.back, query) && !compare(l.notes, query));
        }
        return input;
    }
}
