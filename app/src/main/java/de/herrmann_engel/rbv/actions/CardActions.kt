package de.herrmann_engel.rbv.actions

import android.app.Activity
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.htmlEncode
import androidx.core.text.toHtml
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard
import de.herrmann_engel.rbv.databinding.DiaPrintBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFiles
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.createDialog
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
    val activity: Activity,
    private val listener: Listener,
    private val mediaFiles: MediaFiles
) {
    interface Listener {
        fun deletedCards(cardIds: ArrayList<Int>)
        fun movedCards(cardIds: ArrayList<Int>)
    }

    // User data has to be escaped before it is inserted into the print HTML
    private fun escapeHtml(text: String?): String {
        return (text ?: "").htmlEncode()
    }

    /**
     * Asks for confirmation and deletes the cards. Deleting multiple cards requires a second
     * confirmation (forceDelete is set for the second dialog).
     */
    private fun delete(cards: ArrayList<DB_Card>, forceDelete: Boolean) {
        val text = if (cards.size > 1 && !forceDelete) {
            String.format(activity.getString(R.string.delete_multiple_cards), cards.size)
        } else null
        showConfirmDialog(activity, activity.getString(R.string.delete), text) {
            if (cards.size <= 1 || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(activity)
                val cardIds = arrayListOf<Int>()
                // All cards are deleted or none
                AppDatabase.getInstance(activity).runInTransaction {
                    for (card in cards) {
                        cardIds.add(card.uid)
                        dbHelperDelete.deleteCard(card)
                    }
                }
                listener.deletedCards(cardIds)
            } else {
                delete(cards, true)
            }
        }
    }

    fun delete(cards: ArrayList<DB_Card>) {
        delete(cards, false)
    }

    fun delete(card: DB_Card) {
        delete(arrayListOf(card))
    }

    /**
     * Lets the user choose a pack to move the cards to. Only packs of the given collection are
     * offered, or of all collections if collectionId is null.
     */
    fun move(cards: ArrayList<DB_Card>, collectionId: Int?) {
        val bindingMoveDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        val moveDialog = createDialog(
            activity,
            bindingMoveDialog.root,
            activity.getString(R.string.move_card)
        )
        val dbHelperGet = DB_Helper_Get(activity)
        // Includes the collection names, which are shown for packs of all collections
        val packs: List<DB_Pack_With_Meta> = if (collectionId == null) {
            dbHelperGet.allPacksWithMetaNoCounter
        } else {
            dbHelperGet.getAllPacksWithMetaNoCounterByCollection(collectionId)
        }
        val adapter = AdapterPacksMoveCard(packs, collectionId, cards, moveDialog, listener::movedCards)
        bindingMoveDialog.diaRec.adapter = adapter
        bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(activity)
        moveDialog.show()
    }

    fun move(card: DB_Card, collectionId: Int?) {
        move(arrayListOf(card), collectionId)
    }

    /**
     * Lets the user choose what to include and prints the cards. An HTML document is built and
     * rendered in a WebView, which is then passed to the print manager.
     */
    fun print(cards: ArrayList<DB_Card>) {
        val bindingPrintDialog = DiaPrintBinding.inflate(
            activity.layoutInflater
        )
        val printDialog = createDialog(
            activity,
            bindingPrintDialog.root,
            activity.getString(R.string.print)
        )
        val settings = AppSettings(activity)
        val formatCards = settings.formatCards
        val formatCardNotes = settings.formatCardNotes
        val dbHelperGet = DB_Helper_Get(activity)
        val stringTools = StringTools()
        val jobName: String
        // For a single card, hide the options for content the card does not have
        if (cards.size == 1) {
            jobName = "rbv_flashcard_" + cards[0].uid
            if (cards[0].notes.isNullOrBlank()) {
                bindingPrintDialog.diaPrintIncludeNotesLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeNotes.isChecked = false
            }
            val imageList =
                dbHelperGet.getCardImageMedia(cards[0].uid)
            if (imageList.isEmpty() || imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PRINT) {
                bindingPrintDialog.diaPrintIncludeImagesLayout.visibility = View.GONE
                bindingPrintDialog.diaPrintIncludeImages.isChecked = false
            }
            val mediaList =
                dbHelperGet.getCardMedia(cards[0].uid)
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
                }
            }
            var htmlDocument =
                "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + escapeHtml(activity.getString(R.string.print)) + "</title><style>@page{margin:20mm 25mm;}@media(max-width:250mm),@media(max-height:200mm){@page{margin:7% 10%;}}div{inline-size:100%;overflow-wrap:break-word;}.main-title{margin-bottom:30px;}.title{text-align:center;color:#000007;}.border-top::before{margin-bottom:20px;margin-top:30px;display:block;content:' ';width:100%;height:1px;outline:2px solid #555;outline-offset:-1px;}.main-title.border-top::before{margin-top:100px;}.image-div{display:grid;gap:10px;grid-template-columns: 1fr 1fr 1fr;justify-content:center;}.image{width:100%;height:100%;object-fit:contain;}.media-div{text-align:center}.tag-div{display:inline-block;width:auto;margin:10px;padding:10px;border:2px solid black;border-radius:1em;}.tag-emoji{padding-right:0.5em;}</style></head>"
            var firstCard = true
            // One section per card: title, front, back, and optionally notes, images, other media and tags
            for (card in cards) {
                val cardFront = if (formatCards) {
                    stringTools.format(card.front)
                } else {
                    card.front.toSpanned()
                }
                val maxTitleLength = 30
                var title = escapeHtml(stringTools.shorten(cardFront.toString(), maxTitleLength))
                if (bindingPrintDialog.diaPrintIncludeProgress.isChecked) {
                    title += " (" + card.known + ")"
                }
                val titleClass = if (firstCard) {
                    "main-title title"
                } else {
                    "main-title title border-top"
                }
                htmlDocument += "<h1 class=\"$titleClass\" dir=\"auto\">$title</h1>"
                htmlDocument += "<article>"
                if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                    htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.card_front)) + "</h2>"
                }
                htmlDocument += "<div>"
                htmlDocument += cardFront.toHtml()
                htmlDocument += "</div></article>"
                htmlDocument += "<article>"
                if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                    htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.card_back)) + "</h2>"
                    htmlDocument += "<div>"
                } else {
                    htmlDocument += "<div class=\"border-top\">"
                }
                htmlDocument += if (formatCards) {
                    stringTools.format(card.back)
                } else {
                    card.back.toSpanned()
                }.toHtml()
                htmlDocument += "</div></article>"
                val notes = card.notes
                if (!notes.isNullOrBlank() && bindingPrintDialog.diaPrintIncludeNotes.isChecked) {
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.card_notes)) + "</h2>"
                        htmlDocument += "<div dir=\"auto\">"
                    } else {
                        htmlDocument += "<div dir=\"auto\" class=\"border-top\">"
                    }
                    htmlDocument += if (formatCardNotes) {
                        // Notes are formatted as Markdown
                        val parser = Parser.builder().build()
                        val document = parser.parse(notes)
                        // Raw HTML is left out, as in the app (Markwon without HTML support)
                        val renderer = HtmlRenderer.builder()
                            .nodeRendererFactory { OmitRawHtml }
                            .build()
                        renderer.render(document)
                    } else {
                        notes.toSpanned().toHtml()
                    }
                    htmlDocument += "</div></article>"
                }

                val imageList =
                    dbHelperGet.getCardImageMedia(card.uid)
                if (imageList.isNotEmpty() && bindingPrintDialog.diaPrintIncludeImages.isChecked) {
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.image_media)) + "</h2>"
                        htmlDocument += "<div>"
                    } else {
                        htmlDocument += "<div class=\"border-top\">"
                    }
                    if (imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PRINT) {
                        htmlDocument += String.format(
                            Locale.ROOT,
                            activity.getString(R.string.image_media_print_number_and_limit),
                            imageList.size,
                            Globals.MAX_SIZE_CARD_IMAGE_PRINT
                        )
                    } else {
                        htmlDocument += when (imageList.size) {
                            1 -> {
                                "<div class=\"image-div\" style=\"grid-template-columns:0.33fr;\">"
                            }

                            2 -> {
                                "<div class=\"image-div\" style=\"grid-template-columns:0.33fr 0.33fr;\">"
                            }

                            else -> {
                                "<div class=\"image-div\">"
                            }
                        }
                        for (item in imageList) {
                            val uri =
                                mediaFiles.getImageUri(item.uid)
                            if (uri != null) {
                                htmlDocument += "<div><img class=\"image\" alt=\"" + escapeHtml(item.file) + "\" src=\"" + escapeHtml(uri.toString()) + "\"></div>"
                            }
                        }
                        htmlDocument += "</div>"
                    }
                    htmlDocument += "</div></article>"
                }
                val mediaList =
                    dbHelperGet.getCardMedia(card.uid)
                if (mediaList.isNotEmpty() && bindingPrintDialog.diaPrintIncludeMedia.isChecked) {
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.all_media)) + "</h2>"
                        htmlDocument += "<div>"
                    } else {
                        htmlDocument += "<div class=\"border-top\">"
                    }
                    for (item in mediaList) {
                        htmlDocument += "<div class=\"media-div\">" + escapeHtml(item.file) + "</div>"
                    }
                    htmlDocument += "</div></article>"
                }
                val tagList =
                    dbHelperGet.getCardTags(card.uid)
                if (tagList.isNotEmpty() && bindingPrintDialog.diaPrintIncludeTags.isChecked) {
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + escapeHtml(activity.getString(R.string.tags)) + "</h2>"
                        htmlDocument += "<div>"
                    } else {
                        htmlDocument += "<div class=\"border-top\">"
                    }
                    for (tag in tagList) {
                        htmlDocument += "<div class=\"tag-div\""
                        // Only accept hex colors (as stored by the tag editor) in the style attribute
                        if (tag.color?.matches(Regex("#[0-9a-fA-F]{3,8}")) == true) {
                            htmlDocument += " style=\"border-color:" + tag.color + ";\""
                        }
                        htmlDocument += ">"
                        if (!tag.emoji.isNullOrBlank()) {
                            htmlDocument += "<span class=\"tag-emoji\">" + escapeHtml(tag.emoji) + "</span>"
                        }
                        htmlDocument += "<span>" + escapeHtml(tag.name) + "</span></div>"
                    }
                    htmlDocument += "</div></article>"
                }
                firstCard = false
            }
            htmlDocument += "</html>"
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
        }
        printDialog.show()
    }

    fun print(card: DB_Card) {
        print(arrayListOf(card))
    }

}

// Renders raw HTML blocks and inline HTML of Markdown as nothing
private object OmitRawHtml : NodeRenderer {
    override fun getNodeTypes(): Set<Class<out Node>> =
        setOf(HtmlBlock::class.java, HtmlInline::class.java)

    override fun render(node: Node) {}
}
