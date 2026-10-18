package de.herrmann_engel.rbv.cardlist

import android.app.Activity
import android.widget.TextView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaListStatsBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.utils.createDialog
import kotlin.math.roundToInt

/**
 * Statistics of the displayed cards: total and progress (sum, max, min, average) and a table of
 * the number and share of cards per progress value (0 to 4, 5 or more).
 */
class ListStatsDialog(private val activity: Activity) {

    // One row of the progress table; rows without label view have a fixed label in the layout
    private class ProgressRow(val label: TextView?, val number: TextView, val percent: TextView)

    fun show(cards: List<DB_Card_With_Meta>) {
        val binding = DiaListStatsBinding.inflate(activity.layoutInflater)
        val listStatsDialog = createDialog(
            activity,
            binding.root,
            activity.getString(R.string.list_stats)
        )

        val progress = cards.map { it.card.known }
        binding.listStatCardsTotal.text =
            String.format(activity.getString(R.string.list_stat_cards_total), cards.size)
        binding.listStatProgressTotal.text =
            String.format(activity.getString(R.string.list_stat_progress_total), progress.sum())
        binding.listStatProgressMax.text =
            String.format(
                activity.getString(R.string.list_stat_progress_max),
                progress.maxOrNull() ?: 0
            )
        binding.listStatProgressMin.text =
            String.format(
                activity.getString(R.string.list_stat_progress_min),
                progress.minOrNull() ?: 0
            )
        val average = if (progress.isEmpty()) 0.0 else progress.average()
        binding.listStatProgressAvg.text =
            String.format(
                activity.getString(R.string.list_stat_progress_avg),
                (average * 100.0).roundToInt() / 100.0
            )

        val rows = listOf(
            ProgressRow(
                null,
                binding.listStatProgressCounterNumber0,
                binding.listStatProgressCounterPercent0
            ),
            ProgressRow(
                binding.listStatProgressCounterKnown1,
                binding.listStatProgressCounterNumber1,
                binding.listStatProgressCounterPercent1
            ),
            ProgressRow(
                binding.listStatProgressCounterKnown2,
                binding.listStatProgressCounterNumber2,
                binding.listStatProgressCounterPercent2
            ),
            ProgressRow(
                binding.listStatProgressCounterKnown3,
                binding.listStatProgressCounterNumber3,
                binding.listStatProgressCounterPercent3
            ),
            ProgressRow(
                binding.listStatProgressCounterKnown4,
                binding.listStatProgressCounterNumber4,
                binding.listStatProgressCounterPercent4
            ),
            ProgressRow(
                null,
                binding.listStatProgressCounterNumber5,
                binding.listStatProgressCounterPercent5
            )
        )
        rows.forEachIndexed { value, row ->
            // The last row counts all cards with this progress or more
            val count = if (value == rows.lastIndex) {
                progress.count { it >= value }
            } else {
                progress.count { it == value }
            }
            row.label?.text =
                String.format(activity.getString(R.string.list_stat_progress_counter_known), value)
            row.number.text = activity.resources.getQuantityString(
                R.plurals.list_stat_progress_counter_number,
                count,
                count
            )
            val percent = if (cards.isEmpty()) 0 else (count / cards.size.toFloat() * 100).roundToInt()
            row.percent.text =
                String.format(activity.getString(R.string.list_stat_progress_counter_percent), percent)
        }
        listStatsDialog.show()
    }
}
