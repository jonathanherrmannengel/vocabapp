package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.SpannableString
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard
import de.herrmann_engel.rbv.databinding.ActivityViewCardBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaPrintBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.StringTools
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

class ViewCard : FileTools() {
    private lateinit var binding: ActivityViewCardBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var dbHelperUpdate: DB_Helper_Update
    private var card: DB_Card? = null
    private var known = 0
    private var collectionNo = 0
    private var packNo = 0
    private var cardNo = 0
    private var formatCardNotes = false
    private var imageList: ArrayList<DB_Media_Link_Card>? = null
    private var mediaList: ArrayList<DB_Media_Link_Card>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        dbHelperUpdate = DB_Helper_Update(this)

        intent.extras?.let {
            collectionNo = it.getInt("collection")
            packNo = it.getInt("pack")
            cardNo = it.getInt("card")
        } ?: run {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ViewCard, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("cardUpdated", cardNo)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val formatCards = settings.getBoolean("format_cards", false)
        val increaseFontSize = settings.getBoolean("ui_font_size", false)
        try {
            card = dbHelperGet.getSingleCard(cardNo)
            val cardFront: String
            if (formatCards) {
                val formatString = StringTools()
                val cardFrontSpannable = formatString.format(card!!.front)
                cardFront = cardFrontSpannable.toString()
                binding.cardFront.text = cardFrontSpannable
                binding.cardBack.text = formatString.format(card!!.back)
            } else {
                cardFront = card!!.front
                binding.cardFront.text = cardFront
                binding.cardBack.text = card!!.back
            }
            formatCardNotes = settings.getBoolean("format_card_notes", false)
            if (card!!.notes != null && card!!.notes.isNotEmpty()) {
                binding.cardNotes.visibility = View.VISIBLE
                if (formatCardNotes) {
                    val markwon = Markwon.builder(this)
                        .usePlugin(
                            LinkifyPlugin.create(
                                Linkify.WEB_URLS
                            )
                        )
                        .build()
                    binding.cardNotes.movementMethod = BetterLinkMovementMethod.getInstance()
                    binding.cardNotes.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    markwon.setMarkdown(binding.cardNotes, card!!.notes)
                } else {
                    binding.cardNotes.autoLinkMask = Linkify.WEB_URLS
                    binding.cardNotes.text = card!!.notes
                }
            } else {
                binding.cardNotes.visibility = View.GONE
            }
            if (increaseFontSize) {
                binding.cardFront.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.card_front_size_big)
                )
                binding.cardBack.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.card_back_size_big)
                )
                binding.cardNotes.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.card_notes_size_big)
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val instant = Instant.ofEpochSecond(card!!.date)
                val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.ROOT)
                    .withZone(ZoneId.systemDefault())
                binding.cardDate.text = dateTimeFormatter.format(instant)
            } else {
                binding.cardDate.text = Date(card!!.date * 1000).toString()
            }
            known = card!!.known
            updateCardKnown()
            binding.cardMinus.setOnClickListener {
                known = 0.coerceAtLeast(--known)
                card!!.known = known
                dbHelperUpdate.updateCard(card)
                updateCardKnown()
            }
            binding.cardPlus.setOnClickListener {
                known++
                card!!.known = known
                dbHelperUpdate.updateCard(card)
                updateCardKnown()
            }
            updateColors()
            title = cardFront
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
        setMediaButtons()
    }

    override fun notifyFolderSet() {
        setMediaButtons()
    }

    override fun notifyMissingAction(id: Int) {
        val intent = Intent(this, EditCardMedia::class.java)
        intent.putExtra("card", cardNo)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_card, menu)
        if (collectionNo == 0 || packNo == 0 || cardNo == 0) {
            menu.findItem(R.id.menu_view_card_edit).isVisible = false
            menu.findItem(R.id.menu_view_card_delete).isVisible = false
            menu.findItem(R.id.menu_view_card_edit_media).isVisible = false
            menu.findItem(R.id.menu_view_card_print).isVisible = false
            menu.findItem(R.id.menu_view_card_move).isVisible = false
        } else {
            menu.findItem(R.id.menu_view_card_edit).setOnMenuItemClickListener {
                val intent = Intent(this, EditCard::class.java)
                intent.putExtra("card", cardNo)
                startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menu_view_card_delete).setOnMenuItemClickListener {
                val confirmDeleteDialog = Dialog(this, R.style.dia_view)
                val bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(
                    layoutInflater
                )
                confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.root)
                confirmDeleteDialog.setTitle(resources.getString(R.string.delete))
                confirmDeleteDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
                    val dbHelperDelete = DB_Helper_Delete(this)
                    dbHelperDelete.deleteCard(card)
                    confirmDeleteDialog.dismiss()
                    val intent = Intent(this, ListCards::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("cardDeleted", cardNo)
                    this.startActivity(intent)
                }
                bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
                confirmDeleteDialog.show()
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menu_view_card_edit_media).setOnMenuItemClickListener {
                val intent = Intent(this, EditCardMedia::class.java)
                intent.putExtra("card", cardNo)
                startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menu_view_card_print).setOnMenuItemClickListener {
                val printDialog = Dialog(this, R.style.dia_view)
                val bindingPrintDialog = DiaPrintBinding.inflate(
                    layoutInflater
                )
                printDialog.setContentView(bindingPrintDialog.root)
                printDialog.setTitle(resources.getString(R.string.print))
                printDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                if (card!!.notes == null || card!!.notes.isEmpty()) {
                    bindingPrintDialog.diaPrintIncludeNotesLayout.visibility = View.GONE
                    bindingPrintDialog.diaPrintIncludeNotes.isChecked = false
                }
                if (imageList!!.isEmpty() || imageList!!.size > Globals.IMAGE_PREVIEW_MAX) {
                    bindingPrintDialog.diaPrintIncludeImagesLayout.visibility = View.GONE
                    bindingPrintDialog.diaPrintIncludeImages.isChecked = false
                }
                if (mediaList!!.isEmpty()) {
                    bindingPrintDialog.diaPrintIncludeMediaLayout.visibility = View.GONE
                    bindingPrintDialog.diaPrintIncludeMedia.isChecked = false
                }
                bindingPrintDialog.diaPrintStart.setOnClickListener {
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()
                    printDialog.dismiss()
                    val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                    val jobName = "rbv_flashcard_$cardNo"
                    val builder = PrintAttributes.Builder()
                    builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    val attributes = builder.build()
                    val webView = WebView(this)
                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            val adapter = webView.createPrintDocumentAdapter(jobName)
                            printManager.print(jobName, adapter, attributes)
                        }
                    }
                    val stringTools = StringTools()
                    var htmlDocument =
                        "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + getString(
                            R.string.print
                        ) + "</title><style>div{inline-size:100%;overflow-wrap:break-word;}#main-title{margin-bottom: 30px;}.title{text-align:center;color: #000007;}.border-top::before{margin-bottom:20px;margin-top:30px;display:block;content:' ';width:100%;height:1px;outline:2px solid #555;outline-offset:-1px;}.image-div,.media-div{text-align:center}.image{padding:10px;max-width:30%;max-height:10%;object-fit:contain;}</style></head>"
                    var title = stringTools.shorten(binding.cardFront.text.toString(), 30)
                    if (bindingPrintDialog.diaPrintIncludeProgress.isChecked) {
                        title += " (" + binding.cardKnown.text + ")"
                    }
                    htmlDocument += "<h1 id=\"main-title\" class=\"title\" dir=\"auto\">$title</h1>"
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(
                            R.string.card_front
                        ) + "</h2>"
                    }
                    htmlDocument += "<div>"
                    htmlDocument += HtmlCompat.toHtml(
                        (binding.cardFront.text as SpannableString),
                        HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                    )
                    htmlDocument += "</div></article>"
                    htmlDocument += "<article>"
                    if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                        htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + getString(R.string.card_back) + "</h2>"
                        htmlDocument += "<div>"
                    } else {
                        htmlDocument += "<div class=\"border-top\">"
                    }
                    htmlDocument += HtmlCompat.toHtml(
                        (binding.cardBack.text as SpannableString),
                        HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                    )
                    htmlDocument += "</div></article>"
                    if (card!!.notes != null && card!!.notes.isNotEmpty() && bindingPrintDialog.diaPrintIncludeNotes.isChecked) {
                        htmlDocument += "<article>"
                        if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                            htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + getString(
                                R.string.card_notes
                            ) + "</h2>"
                            htmlDocument += "<div dir=\"auto\">"
                        } else {
                            htmlDocument += "<div dir=\"auto\" class=\"border-top\">"
                        }
                        htmlDocument += if (formatCardNotes) {
                            val parser = Parser.builder().build()
                            val document = parser.parse(card!!.notes)
                            val renderer = HtmlRenderer.builder().build()
                            renderer.render(document)
                        } else {
                            HtmlCompat.toHtml(
                                (binding.cardNotes.text as SpannableString),
                                HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                            )
                        }
                        htmlDocument += "</div></article>"
                    }
                    if (imageList!!.isNotEmpty() && bindingPrintDialog.diaPrintIncludeImages.isChecked) {
                        htmlDocument += "<article>"
                        if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                            htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + getString(
                                R.string.image_media
                            ) + "</h2>"
                            htmlDocument += "<div>"
                        } else {
                            htmlDocument += "<div class=\"border-top\">"
                        }
                        for (i in imageList!!) {
                            val currentMedia = dbHelperGet.getSingleMedia(i.file)
                            if (currentMedia != null) {
                                val uri = getImageUri(currentMedia.uid)
                                if (uri != null) {
                                    htmlDocument += "<div class=\"image-div\"><img class=\"image\" alt=\"" + currentMedia.file + "\" src=\"" + uri + "\"></div>"
                                }
                            }
                        }
                        htmlDocument += "</div></article>"
                    }
                    if (mediaList!!.isNotEmpty() && bindingPrintDialog.diaPrintIncludeMedia.isChecked) {
                        htmlDocument += "<article>"
                        if (bindingPrintDialog.diaPrintIncludeHeadings.isChecked) {
                            htmlDocument += "<h2 class=\"title border-top\" dir=\"auto\">" + getString(
                                R.string.all_media
                            ) + "</h2>"
                            htmlDocument += "<div>"
                        } else {
                            htmlDocument += "<div class=\"border-top\">"
                        }
                        for (i in mediaList!!) {
                            val currentMedia = dbHelperGet.getSingleMedia(i.file)
                            if (currentMedia != null) {
                                htmlDocument += "<div class=\"media-div\">" + currentMedia.file + "</div>"
                            }
                        }
                        htmlDocument += "</div></article>"
                    }
                    htmlDocument += "</html>"
                    webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
                }
                printDialog.show()
                return@setOnMenuItemClickListener true
            }
            if (packNo < 0) {
                menu.findItem(R.id.menu_view_card_move).isVisible = false
            } else {
                menu.findItem(R.id.menu_view_card_move).setOnMenuItemClickListener {
                    val moveDialog = Dialog(this, R.style.dia_view)
                    val bindingMoveDialog = DiaRecBinding.inflate(
                        layoutInflater
                    )
                    moveDialog.setContentView(bindingMoveDialog.root)
                    moveDialog.setTitle(resources.getString(R.string.move_card))
                    moveDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    val packs: List<DB_Pack> = if (collectionNo == -1) {
                        dbHelperGet.allPacks
                    } else {
                        dbHelperGet.getAllPacksByCollection(collectionNo)
                    }
                    val adapter = AdapterPacksMoveCard(packs, collectionNo, card!!, moveDialog)
                    bindingMoveDialog.diaRec.adapter = adapter
                    bindingMoveDialog.diaRec.layoutManager = LinearLayoutManager(this)
                    moveDialog.show()
                    return@setOnMenuItemClickListener true
                }
            }
        }
        return true
    }

    private fun updateColors() {
        try {
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
            val colorsBackgroundLight =
                resources.obtainTypedArray(R.array.pack_color_background_light)
            val packColors = dbHelperGet.getSinglePack(card!!.pack).colors
            if (packColors >= 0 && packColors < colorsStatusBar.length() && packColors < colorsBackground.length() && packColors < colorsBackgroundLight.length()) {
                val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                val colorBackground = colorsBackground.getColor(packColors, 0)
                val colorBackgroundLight = colorsBackgroundLight.getColor(packColors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
                binding.cardKnownProgress.setBackgroundColor(colorBackgroundLight)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
            colorsBackgroundLight.recycle()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCardKnown() {
        binding.cardKnown.text = known.toString()
        binding.cardMinus.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.cardMinus.setColorFilter(
            ContextCompat.getColor(this, if (known > 0) R.color.dark_red else R.color.dark_grey),
            PorterDuff.Mode.MULTIPLY
        )

    }

    fun movedCard() {
        try {
            card = dbHelperGet.getSingleCard(cardNo)
            packNo = card!!.pack
            updateColors()
            val intent = Intent(this, ListCards::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("collection", collectionNo)
            intent.putExtra("pack", packNo)
            this.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMediaButtons() {
        imageList = dbHelperGet.getImageMediaLinksByCard(cardNo) as ArrayList<DB_Media_Link_Card>
        if (imageList!!.isEmpty()) {
            binding.viewCardImages.visibility = View.GONE
        } else {
            binding.viewCardImages.visibility = View.VISIBLE
        }
        binding.viewCardImages.setOnClickListener {
            showImageListDialog(
                imageList!!
            )
        }
        mediaList = dbHelperGet.getAllMediaLinksByCard(cardNo) as ArrayList<DB_Media_Link_Card>
        if (mediaList!!.isEmpty()) {
            binding.viewCardMedia.visibility = View.GONE
        } else {
            binding.viewCardMedia.visibility = View.VISIBLE
        }
        binding.viewCardMedia.setOnClickListener {
            showMediaListDialog(
                mediaList!!
            )
        }
    }
}
