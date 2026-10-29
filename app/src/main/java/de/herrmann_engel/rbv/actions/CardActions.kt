package de.herrmann_engel.rbv.actions

import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.htmlEncode
import androidx.core.text.toHtml
import androidx.core.text.toSpanned
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard
import de.herrmann_engel.rbv.databinding.DiaPrintBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.PackWithMeta
import de.herrmann_engel.rbv.db.utils.DbHelperDelete
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.db.utils.DbHelperUpdate
import de.herrmann_engel.rbv.media.MediaFiles
import de.herrmann_engel.rbv.media.isImage
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.runInBackground
import de.herrmann_engel.rbv.utils.showConfirmDialog
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlRenderer
import java.util.Locale

/**
 * Dialogs for actions on one or more cards: delete, move to another pack and print.
 * The listener is notified after cards have been deleted or moved; mediaFiles provides the
 * images for printing.
 */
class CardActions(
    val activity: ComponentActivity,
    private val listener: Listener,
    private val mediaFiles: MediaFiles
) {
    interface Listener {
        fun deletedCards(cardIds: ArrayList<Int>)
        fun movedCards(cardIds: ArrayList<Int>)
    }

    /**
     * Asks for confirmation and deletes the cards. Deleting multiple cards requires a second
     * confirmation (forceDelete is set for the second dialog).
     */
    private fun delete(cards: ArrayList<DbCard>, forceDelete: Boolean) {
        val text = if (cards.size > 1 && !forceDelete) {
            String.format(activity.getString(R.string.delete_multiple_cards), cards.size)
        } else null
        showConfirmDialog(activity, activity.getString(R.string.delete), text) {
            if (cards.size <= 1 || forceDelete) {
                val dbHelperDelete = DbHelperDelete(activity)
                val cardIds = ArrayList(cards.map { it.uid })
                runInBackground(activity, {
                    // All cards are deleted or none
                    AppDatabase.getInstance(activity).runInTransaction {
                        cards.forEach { dbHelperDelete.deleteCard(it) }
                    }
                }) { listener.deletedCards(cardIds) }
            } else {
                delete(cards, true)
            }
        }
    }

    fun delete(cards: ArrayList<DbCard>) {
        delete(cards, false)
    }

    fun delete(card: DbCard) {
        delete(arrayListOf(card))
    }

    /**
     * Lets the user choose a pack to move the cards to. Only packs of the given collection are
     * offered, or of all collections if collectionId is null.
     */
    fun move(cards: ArrayList<DbCard>, collectionId: Int?) {
        val bindingMoveDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        val moveDialog = createDialog(
            activity,
            bindingMoveDialog.root,
            activity.getString(R.string.move_card)
        )
        val dbHelperGet = DbHelperGet(activity)
        // Includes the collection names, which are shown for packs of all collections
        val packs: List<PackWithMeta> = if (collectionId == null) {
            dbHelperGet.getAllPacksWithMeta()
        } else {
            dbHelperGet.getAllPacksWithMetaByCollection(collectionId)
        }
        val adapter = AdapterPacksMoveCard(packs, collectionId) { packId ->
            moveDialog.dismiss()
            moveToPack(cards, packId)
        }
        bindingMoveDialog.diaRec.adapter = adapter
        bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(activity)
        moveDialog.show()
    }

    fun move(card: DbCard, collectionId: Int?) {
        move(arrayListOf(card), collectionId)
    }

    // Only cards not already in the pack are moved and reported
    private fun moveToPack(cards: List<DbCard>, packId: Int) {
        val dbHelperUpdate = DbHelperUpdate(activity)
        val movedCards = cards.filter { it.packId != packId }
        movedCards.forEach { it.packId = packId }
        runInBackground(activity, {
            // All cards are moved or none
            AppDatabase.getInstance(activity).runInTransaction {
                movedCards.forEach { dbHelperUpdate.updateCard(it) }
            }
        }) { listener.movedCards(ArrayList(movedCards.map { it.uid })) }
    }

    /**
     * Lets the user choose what to include and prints the cards. An HTML document is built and
     * rendered in a WebView, which is then passed to the print manager.
     */
    fun print(cards: ArrayList<DbCard>) {
        val bindingPrintDialog = DiaPrintBinding.inflate(
            activity.layoutInflater
        )
        val printDialog = createDialog(
            activity,
            bindingPrintDialog.root,
            activity.getString(R.string.print)
        )
        val dbHelperGet = DbHelperGet(activity)
        val jobName: String
        // For a single card, hide the options for content the card does not have
        if (cards.size == 1) {
            jobName = "rbv_flashcard_" + cards[0].uid
            if (cards[0].notes.isBlank()) {
                bindingPrintDialog.diaPrintIncludeNotesLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeNotes.isChecked = false
            }
            val mediaList = dbHelperGet.getCardMedia(cards[0].uid)
            val imageList = mediaList.filter { it.isImage }
            if (imageList.isEmpty() || imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PRINT) {
                bindingPrintDialog.diaPrintIncludeImagesLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeImages.isChecked = false
            }
            if (mediaList.isEmpty()) {
                bindingPrintDialog.diaPrintIncludeMediaLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeMedia.isChecked = false
            }
            val tagList =
                dbHelperGet.getCardTags(cards[0].uid)
            if (tagList.isEmpty()) {
                bindingPrintDialog.diaPrintIncludeTagsLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeTags.isChecked = false
            }
        } else {
            jobName = "rbv_flashcards"
        }
        bindingPrintDialog.diaPrintStart.setOnClickListener {
            Toast.makeText(activity, R.string.wait, Toast.LENGTH_LONG).show()
            printDialog.dismiss()
            val printManager =
                activity.getSystemService(AppCompatActivity.PRINT_SERVICE) as PrintManager
            val builder = PrintAttributes.Builder()
            builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            val attributes = builder.build()
            val webView = WebView(activity)
            // The WebView is not shown, so nothing would keep it until its page has been handed to
            // the print manager (which keeps it from then on): this observer holds it (through
            // onDestroy) for as long as it is registered with the activity, and destroys it if the
            // activity is closed before.
            val keepWebView = object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    webView.destroy()
                }
            }
            activity.lifecycle.addObserver(keepWebView)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    return false
                }

                // Start printing once the HTML has been rendered
                override fun onPageFinished(view: WebView, url: String) {
                    val adapter = webView.createPrintDocumentAdapter(jobName)
                    printManager.print(jobName, adapter, attributes)
                    activity.lifecycle.removeObserver(keepWebView)
                }
            }
            val options = PrintOptions(
                headings = bindingPrintDialog.diaPrintIncludeHeadings.isChecked,
                progress = bindingPrintDialog.diaPrintIncludeProgress.isChecked,
                notes = bindingPrintDialog.diaPrintIncludeNotes.isChecked,
                images = bindingPrintDialog.diaPrintIncludeImages.isChecked,
                media = bindingPrintDialog.diaPrintIncludeMedia.isChecked,
                tags = bindingPrintDialog.diaPrintIncludeTags.isChecked
            )
            val htmlDocument = printHtml(cards, options)
            webView.loadDataWithBaseURL(null, htmlDocument, "text/html", "UTF-8", null)
        }
        printDialog.show()
    }

    fun print(card: DbCard) {
        print(arrayListOf(card))
    }

    // What the user chose to include in the print dialog
    private data class PrintOptions(
        val headings: Boolean,
        val progress: Boolean,
        val notes: Boolean,
        val images: Boolean,
        val media: Boolean,
        val tags: Boolean
    )

    // User data has to be escaped before it is inserted into the print HTML
    private fun escapeHtml(text: String): String {
        return text.htmlEncode()
    }

    /**
     * The HTML document to print: one part per card with title, front, back, and optionally
     * notes, images, other media and tags. User data is escaped (see escapeHtml).
     */
    private fun printHtml(cards: List<DbCard>, options: PrintOptions): String {
        val settings = AppSettings(activity)
        val formatCards = settings.formatCards
        val formatCardNotes = settings.formatCardNotes
        val dbHelperGet = DbHelperGet(activity)

        /**
         * A section of a card: with a heading, or (if headings are left out) separated by a line
         * only, except for the first section. content appends the inner HTML.
         */
        fun StringBuilder.appendSection(
            @StringRes heading: Int,
            divAttributes: String = "",
            separated: Boolean = true,
            content: StringBuilder.() -> Unit
        ) {
            append("<article>")
            val headingClass = if (separated) "title border-top" else "title"
            if (options.headings) {
                val headingText = escapeHtml(activity.getString(heading))
                append("""<h2 class="$headingClass" dir="auto">$headingText</h2>""")
                append("""<div$divAttributes>""")
            } else if (separated) {
                append("""<div$divAttributes class="border-top">""")
            } else {
                append("""<div$divAttributes>""")
            }
            content()
            append("</div></article>")
        }

        return buildString {
            val documentTitle = escapeHtml(activity.getString(R.string.print))
            append("""<!doctype html><html><head><meta charset="utf-8">""")
            append("""<title>$documentTitle</title><style>$PRINT_CSS</style></head>""")
            cards.forEachIndexed { index, card ->
                val cardFront = if (formatCards) {
                    StringTools.format(card.front)
                } else {
                    card.front.toSpanned()
                }
                val maxTitleLength = 30
                val shortFront = StringTools.shorten(cardFront.toString(), maxTitleLength)
                var title = escapeHtml(shortFront)
                if (options.progress) {
                    title += " (${card.known})"
                }
                val titleClass = if (index == 0) {
                    "main-title title"
                } else {
                    "main-title title border-top"
                }
                append("""<h1 class="$titleClass" dir="auto">$title</h1>""")
                appendSection(R.string.card_front, separated = false) {
                    append(cardFront.toHtml())
                }
                appendSection(R.string.card_back) {
                    val cardBack = if (formatCards) {
                        StringTools.format(card.back)
                    } else {
                        card.back.toSpanned()
                    }
                    append(cardBack.toHtml())
                }
                val notes = card.notes
                if (notes.isNotBlank() && options.notes) {
                    appendSection(R.string.card_notes, """ dir="auto"""") {
                        if (formatCardNotes) {
                            // Notes are formatted as Markdown; raw HTML is left out, as in the app
                            // (Markwon without HTML support)
                            append(markdownRenderer.render(markdownParser.parse(notes)))
                        } else {
                            append(notes.toSpanned().toHtml())
                        }
                    }
                }
                val mediaList = dbHelperGet.getCardMedia(card.uid)
                val imageList = mediaList.filter { it.isImage }
                if (imageList.isNotEmpty() && options.images) {
                    appendSection(R.string.image_media) {
                        if (imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PRINT) {
                            append(
                                String.format(
                                    Locale.ROOT,
                                    activity.getString(R.string.image_media_print_number_and_limit),
                                    imageList.size,
                                    Globals.MAX_SIZE_CARD_IMAGE_PRINT
                                )
                            )
                        } else {
                            // Fewer than 3 images keep the width of a third each
                            val style = when (imageList.size) {
                                1 -> """ style="grid-template-columns:0.33fr;""""
                                2 -> """ style="grid-template-columns:0.33fr 0.33fr;""""
                                else -> ""
                            }
                            append("""<div class="image-div"$style>""")
                            for (item in imageList) {
                                val uri = mediaFiles.getImageUri(item.uid) ?: continue
                                val alt = escapeHtml(item.file)
                                val src = escapeHtml(uri.toString())
                                append("""<div><img class="image" alt="$alt" src="$src"></div>""")
                            }
                            append("</div>")
                        }
                    }
                }
                if (mediaList.isNotEmpty() && options.media) {
                    appendSection(R.string.all_media) {
                        for (item in mediaList) {
                            append("""<div class="media-div">${escapeHtml(item.file)}</div>""")
                        }
                    }
                }
                val tagList = dbHelperGet.getCardTags(card.uid)
                if (tagList.isNotEmpty() && options.tags) {
                    appendSection(R.string.tags) {
                        for (tag in tagList) {
                            // Only accept hex colors in the style attribute
                            val style = if (tag.color.matches(HEX_COLOR)) {
                                """ style="border-color:${tag.color};""""
                            } else {
                                ""
                            }
                            val emoji = if (tag.emoji.isBlank()) {
                                ""
                            } else {
                                """<span class="tag-emoji">${escapeHtml(tag.emoji)}</span>"""
                            }
                            val name = escapeHtml(tag.name)
                            append("""<div class="tag-div"$style>$emoji<span>$name</span></div>""")
                        }
                    }
                }
            }
            append("</html>")
        }
    }

}

// Style of the print HTML
private const val PRINT_CSS =
    "@page{margin:20mm 25mm;}" +
    "@media(max-width:250mm),@media(max-height:200mm){@page{margin:7% 10%;}}" +
    "div{inline-size:100%;overflow-wrap:break-word;}" +
    ".main-title{margin-bottom:30px;}" +
    ".title{text-align:center;color:#000007;}" +
    ".border-top::before{margin-bottom:20px;margin-top:30px;display:block;content:' ';" +
    "width:100%;height:1px;outline:2px solid #555;outline-offset:-1px;}" +
    ".main-title.border-top::before{margin-top:100px;}" +
    ".image-div{display:grid;gap:10px;grid-template-columns: 1fr 1fr 1fr;justify-content:center;}" +
    ".image{width:100%;height:100%;object-fit:contain;}" +
    ".media-div{text-align:center}" +
    ".tag-div{display:inline-block;width:auto;margin:10px;padding:10px;" +
    "border:2px solid black;border-radius:1em;}" +
    ".tag-emoji{padding-right:0.5em;}"

// Tag colors that may be used in the print HTML (as stored by the tag editor)
private val HEX_COLOR = Regex("#[0-9a-fA-F]{3,8}")

// Markdown of card notes for the print HTML; both can be reused for all notes
private val markdownParser: Parser by lazy { Parser.builder().build() }
private val markdownRenderer: HtmlRenderer by lazy {
    HtmlRenderer.builder().nodeRendererFactory { OmitRawHtml }.build()
}

// Renders raw HTML blocks and inline HTML of Markdown as nothing
private object OmitRawHtml : NodeRenderer {
    override fun getNodeTypes(): Set<Class<out Node>> =
        setOf(HtmlBlock::class.java, HtmlInline::class.java)

    override fun render(node: Node) {}
}
