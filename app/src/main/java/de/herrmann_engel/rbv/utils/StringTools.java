package de.herrmann_engel.rbv.utils;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {

   private Pattern formatCardBoldItalicPattern;
   private Pattern formatCardBoldPattern;
   private Pattern formatCardItalicPattern;
   private Pattern formatCardIUnderlinePattern;

    private int getTypeface(int i) {
        if (i == 3) {
            return Typeface.BOLD_ITALIC;
        } else if (i == 2) {
            return Typeface.BOLD;
        } else if (i == 1) {
            return Typeface.ITALIC;
        } else {
            return Typeface.NORMAL;
        }
    }

    private Pattern generatePattern(int i) {
        String regex =  String.format("%s%d%s%d%s", "(^|\\s|_)([*]{", i, "}[^\\s*][^\\n]*?(?<![\\s*])[*]{", i, "})");
        return Pattern.compile(regex);
    }

    private Pattern getPattern(int i) {
        if (i == 3) {
            if(formatCardBoldItalicPattern == null){
                formatCardBoldItalicPattern = generatePattern(i);
            }
            return formatCardBoldItalicPattern;
        } else if (i == 2) {
            if(formatCardBoldPattern == null){
                formatCardBoldPattern = generatePattern(i);
            }
            return formatCardBoldPattern;
        } else if (i == 1) {
            if(formatCardItalicPattern == null){
                formatCardItalicPattern = generatePattern(i);
            }
            return formatCardItalicPattern;
        } else {
            return null;
        }
    }

    public SpannableString format(String input) {
        SpannableStringBuilder output = new SpannableStringBuilder(input);
        StringBuffer modifiedInput = new StringBuffer(input);
        for (int i = 3; i > 0; i--) {
            int offset = 0;
            Pattern pattern = getPattern(i);
            if(pattern != null) {
                Matcher matcher = pattern.matcher(modifiedInput);
                while (matcher.find()) {
                    output.setSpan(new StyleSpan(getTypeface(i)), matcher.start(2) - offset, matcher.end(2) - offset, 0);
                    output.delete(matcher.start(2) - offset, matcher.start(2) - offset + i);
                    modifiedInput.delete(matcher.start(2) - offset, matcher.start(2) - offset + i);
                    offset += i;
                    output.delete(matcher.end(2) - offset - i, matcher.end(2) - offset);
                    modifiedInput.delete(matcher.end(2) - offset - i, matcher.end(2) - offset);
                    offset += i;
                }
            }
        }
        int offset = 0;
        if(formatCardIUnderlinePattern == null) {
            String regex = "(^|\\s|[*])([_][^\\s_][^\\n]*?(?<![\\s_])[_])";
            formatCardIUnderlinePattern = Pattern.compile(regex);
        }
        Matcher matcher = formatCardIUnderlinePattern.matcher(modifiedInput);
        while (matcher.find()) {
            output.setSpan(new UnderlineSpan(), matcher.start(2) - offset, matcher.end(2) - offset, 0);
            output.delete(matcher.start(2) - offset, matcher.start(2) - offset + 1);
            modifiedInput.delete(matcher.start(2) - offset, matcher.start(2) - offset + 1);
            offset++;
            output.delete(matcher.end(2) - offset - 1, matcher.end(2) - offset);
            modifiedInput.delete(matcher.end(2) - offset - 1, matcher.end(2) - offset);
            offset++;
        }
        return SpannableString.valueOf(output);
    }

    public String shorten(String input, int maxLength) {
        Pattern pattern = Pattern.compile("(\\P{M}\\p{M}*+)");
        Matcher matcher = pattern.matcher(input);
        int count = 0;
        while (matcher.find() && count <= maxLength) {
            count++;
        }
        if (count > maxLength && maxLength >= 0) {
            return input.replaceAll(String.format("%s%d%s", "^((\\P{M}\\p{M}*+){", maxLength - 1, "}).*$"), "$1…");
        }
        return input;
    }

    public String shorten(String input) {
        return shorten(input, 50);
    }

    public String firstEmoji(String input) {
        if (input.isEmpty()) {
            return null;
        }
        BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
        iterator.setText(input);
        return input.substring(iterator.first(), iterator.next());
    }

}
