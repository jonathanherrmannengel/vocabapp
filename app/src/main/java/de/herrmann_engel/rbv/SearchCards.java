package de.herrmann_engel.rbv;

import java.util.List;
import java.util.regex.Pattern;

public class SearchCards {

    private boolean compare(String source, String query) {
        if (source == null) {
            return false;
        }
        return Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(source)
                .find();
    }

    public List<DB_Card> searchCards(List<DB_Card> input, String query) {
        input.removeIf(l -> !compare(l.front, query) && !compare(l.back, query) && !compare(l.notes, query));
        return input;
    }
}
