package de.herrmann_engel.rbv

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterCollections(private val collection: List<DB_Collection>, private val c: Context) :
        RecyclerView.Adapter<AdapterCollections.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(viewGroup.context).inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (position == 0 && collection.isEmpty()) {
            viewHolder.textView.text = c.resources.getString(R.string.welcome_collection)
        } else if (position == 0) {
            viewHolder.textView.text = c.resources.getString(R.string.all_packs)
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", -1)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
        } else {
            viewHolder.textView.text = collection[position - 1].name
            val extra = collection[position - 1].uid
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", extra)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return collection.size + 1
    }
}
