package de.herrmann_engel.rbv.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import java.text.BreakIterator
import java.util.Locale
import java.util.regex.Pattern

class StringTools {
    private var formatCardBoldItalicPattern: Pattern? = null
    private var formatCardBoldPattern: Pattern? = null
    private var formatCardItalicPattern: Pattern? = null
    private var formatCardUnderlinePattern: Pattern? = null
    private var unformatCardPattern: Pattern? = null
    private var shortenDefaultPattern: Pattern? = null
    private var shortenTestPattern: Pattern? = null
    private fun getTypeface(i: Int): Int {
        return when (i) {
            3 -> {
                Typeface.BOLD_ITALIC
            }

            2 -> {
                Typeface.BOLD
            }

            1 -> {
                Typeface.ITALIC
            }

            else -> {
                Typeface.NORMAL
            }
        }
    }

    private fun generateTypefacePattern(i: Int): Pattern {
        val regex = String.format(
            Locale.ROOT,
            "%s%d%s%d%s",
            "(^|\\s|[(]|_)([*]{",
            i,
            "})([^\\s*][^\\n]*?(?<![\\s*]))([*]{",
            i,
            "})"
        )
        return Pattern.compile(regex)
    }

    private fun getTypefacePattern(i: Int): Pattern? {
        return when (i) {
            3 -> {
                if (formatCardBoldItalicPattern == null) {
                    formatCardBoldItalicPattern = generateTypefacePattern(i)
                }
                formatCardBoldItalicPattern
            }

            2 -> {
                if (formatCardBoldPattern == null) {
                    formatCardBoldPattern = generateTypefacePattern(i)
                }
                formatCardBoldPattern
            }

            1 -> {
                if (formatCardItalicPattern == null) {
                    formatCardItalicPattern = generateTypefacePattern(i)
                }
                formatCardItalicPattern
            }

            else -> {
                null
            }
        }
    }

    private fun getUnderlinePattern(): Pattern {
        if (formatCardUnderlinePattern == null) {
            val regex = "(^|\\s|[(]|[*])(_)([^\\s_][^\\n]*?(?<![\\s_]))(_)"
            formatCardUnderlinePattern = Pattern.compile(regex)
        }
        return formatCardUnderlinePattern!!
    }


    fun format(input: String): SpannableString {
        val output = SpannableStringBuilder(input)
        val modifiedInput = StringBuffer(input)
        for (i in 3 downTo 1) {
            var offset = 0
            val pattern = getTypefacePattern(i)
            if (pattern != null) {
                val matcher = pattern.matcher(modifiedInput)
                while (matcher.find()) {
                    output.setSpan(
                        StyleSpan(getTypeface(i)),
                        matcher.start(3) - offset,
                        matcher.end(3) - offset,
                        0
                    )
                    output.delete(matcher.start(2) - offset, matcher.start(2) - offset + i)
                    modifiedInput.delete(matcher.start(2) - offset, matcher.start(2) - offset + i)
                    offset += i
                    output.delete(matcher.end(4) - offset - i, matcher.end(4) - offset)
                    modifiedInput.delete(matcher.end(4) - offset - i, matcher.end(4) - offset)
                    offset += i
                }
            }
        }
        var offset = 0
        val matcher = getUnderlinePattern().matcher(modifiedInput)
        while (matcher.find()) {
            output.setSpan(
                UnderlineSpan(),
                matcher.start(3) - offset,
                matcher.end(3) - offset,
                0
            )
            output.delete(matcher.start(2) - offset, matcher.start(2) - offset + 1)
            modifiedInput.delete(matcher.start(2) - offset, matcher.start(2) - offset + 1)
            offset++
            output.delete(matcher.end(4) - offset - 1, matcher.end(4) - offset)
            modifiedInput.delete(matcher.end(4) - offset - 1, matcher.end(4) - offset)
            offset++
        }
        return SpannableString.valueOf(output)
    }

    fun unformat(input: String, inaccurate: Boolean): String {
        if (inaccurate) {
            if (unformatCardPattern == null) {
                val regex = "[*_]+"
                unformatCardPattern = Pattern.compile(regex)
            }
            return unformatCardPattern!!.matcher(input).replaceAll("")
        } else {
            var output = input
            for (i in 3 downTo 1) {
                val pattern = getTypefacePattern(i)
                if (pattern != null) {
                    output = pattern.matcher(output).replaceAll("$1$3")
                }
            }
            output = getUnderlinePattern().matcher(output).replaceAll("$1$3")
            return output
        }
    }

    private fun generateShortenPattern(maxLength: Int): Pattern {
        return Pattern.compile(
            String.format(
                Locale.ROOT,
                "%s%d%s",
                "^((\\P{M}\\p{M}*+){",
                maxLength - 1,
                "}).*$"
            )
        )
    }

    private fun shortenTest(input: String, maxLength: Int): Boolean {
        if (maxLength <= 0) {
            return false
        }
        if (shortenTestPattern == null) {
            shortenTestPattern = Pattern.compile("(\\P{M}\\p{M}*+)")
        }
        val matcher = shortenTestPattern!!.matcher(input)
        var count = 0
        while (matcher.find() && count <= maxLength) {
            count++
        }
        return count > maxLength
    }

    fun shorten(input: String, maxLength: Int): String {
        return if (shortenTest(input, maxLength)) {
            generateShortenPattern(maxLength).matcher(input).replaceAll("$1…")
        } else input
    }

    fun shorten(input: String): String {
        val maxLength = 50
        return if (shortenTest(input, maxLength)) {
            if (shortenDefaultPattern == null) {
                shortenDefaultPattern = generateShortenPattern(maxLength)
            }
            shortenDefaultPattern!!.matcher(input).replaceAll("$1…")
        } else input
    }

    fun firstEmoji(input: String): String? {
        if (input.isEmpty()) {
            return null
        }
        val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
        iterator.setText(input)
        return input.substring(iterator.first(), iterator.next())
    }
}
