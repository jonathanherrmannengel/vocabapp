package de.herrmann_engel.rbv;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;

public class SearchCards {
    List<DB_Card> input;
    String query;
    Context context;

    public SearchCards(List<DB_Card> input, String query, Context context) {
        this.input = input;
        this.query = query;
        this.context = context;
    }

    private boolean compare (String source) {
        return Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(source).find();
    }

    public List<DB_Card> searchCards(){
        SharedPreferences settings = context.getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean formatCardsNotes = settings.getBoolean("format_card_notes", false);
        input.removeIf(l -> !compare(formatCards ? (new FormatString(l.back).formatString().toString()) : l.back) && !compare(formatCards ? (new FormatString(l.front).formatString().toString()) : l.front) && !compare(formatCardsNotes ? (Markwon.create(context)).toMarkdown(l.notes).toString() : l.notes));
        return input;
    }
}
