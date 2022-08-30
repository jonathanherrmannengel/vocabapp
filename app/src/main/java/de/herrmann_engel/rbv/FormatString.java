package de.herrmann_engel.rbv;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatString {

    private int getTypeface(int i) {
        if(i == 3) {
            return Typeface.BOLD_ITALIC;
        } else if (i == 2) {
            return Typeface.BOLD;
        } else if (i == 1) {
            return Typeface.ITALIC;
        } else {
            return Typeface.NORMAL;
        }
    }

    public SpannableString formatString(String input){
        SpannableStringBuilder output = new SpannableStringBuilder(input);
        StringBuffer modifiedInput = new StringBuffer(input);
        for(int i = 3; i > 0; i--){
            int offset = 0;
            String regex = String.format("%s%d%s%d%s", "(^|\\s|_)([*]{", i, "}[^\\s*][^\\n]*?(?<![\\s*])[*]{", i, "})");
            Pattern pattern = Pattern.compile(regex);
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
        int offset = 0;
        String regex = "(^|\\s|[*])([_][^\\s_][^\\n]*?(?<![\\s_])[_])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(modifiedInput);
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

}
