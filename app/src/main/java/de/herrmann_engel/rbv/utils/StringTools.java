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
    private Pattern formatCardUnderlinePattern;
    private Pattern unformatCardPattern;
    private Pattern shortenDefaultPattern;
    private Pattern shortenTestPattern;

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

    private Pattern generateTypefacePattern(int i) {
        String regex = String.format("%s%d%s%d%s", "(^|\\s|_)([*]{", i, "}[^\\s*][^\\n]*?(?<![\\s*])[*]{", i, "})");
        return Pattern.compile(regex);
    }

    private Pattern getTypefacePattern(int i) {
        if (i == 3) {
            if (formatCardBoldItalicPattern == null) {
                formatCardBoldItalicPattern = generateTypefacePattern(i);
            }
            return formatCardBoldItalicPattern;
        } else if (i == 2) {
            if (formatCardBoldPattern == null) {
                formatCardBoldPattern = generateTypefacePattern(i);
            }
            return formatCardBoldPattern;
        } else if (i == 1) {
            if (formatCardItalicPattern == null) {
                formatCardItalicPattern = generateTypefacePattern(i);
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
            Pattern pattern = getTypefacePattern(i);
            if (pattern != null) {
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
        if (formatCardUnderlinePattern == null) {
            String regex = "(^|\\s|[*])([_][^\\s_][^\\n]*?(?<![\\s_])[_])";
            formatCardUnderlinePattern = Pattern.compile(regex);
        }
        Matcher matcher = formatCardUnderlinePattern.matcher(modifiedInput);
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

    public String unformat(String input) {
        if (unformatCardPattern == null) {
            String regex = "[*_#{}]+";
            unformatCardPattern = Pattern.compile(regex);
        }
        return unformatCardPattern.matcher(input).replaceAll("");
    }

    private Pattern generateShortenPattern(int maxLength) {
        return Pattern.compile(String.format("%s%d%s", "^((\\P{M}\\p{M}*+){", maxLength - 1, "}).*$"));
    }

    private boolean shortenTest(String input, int maxLength) {
        if (maxLength <= 0) {
            return false;
        }
        if (shortenTestPattern == null) {
            shortenTestPattern = Pattern.compile("(\\P{M}\\p{M}*+)");
        }
        Matcher matcher = shortenTestPattern.matcher(input);
        int count = 0;
        while (matcher.find() && count <= maxLength) {
            count++;
        }
        return count > maxLength;
    }

    public String shorten(String input, int maxLength) {
        if (shortenTest(input, maxLength)) {
            return generateShortenPattern(maxLength).matcher(input).replaceAll("$1…");
        }
        return input;
    }

    public String shorten(String input) {
        int maxLength = 50;
        if (shortenTest(input, maxLength)) {
            if (shortenDefaultPattern == null) {
                shortenDefaultPattern = generateShortenPattern(maxLength);
            }
            return shortenDefaultPattern.matcher(input).replaceAll("$1…");
        }
        return input;
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
