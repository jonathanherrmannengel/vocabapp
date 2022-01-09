package de.herrmann_engel.rbv

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Intent
import android.view.View

class AdapterCollections(private val collection: List<DB_Collection>, private val c: Context) :
    RecyclerView.Adapter<AdapterCollections.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.rec_name)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rec_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (collection.isEmpty()) {
            viewHolder.textView.text = c.resources.getString(R.string.welcome_collection)
        } else {
            viewHolder.textView.text = collection[position].name
            val extra = collection[position].uid
            viewHolder.textView.setOnClickListener {
                val intent = Intent(c.applicationContext, ListPacks::class.java)
                intent.putExtra("collection", extra)
                c.startActivity(intent)
                (c as ListCollections).finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return collection.size.coerceAtLeast(1)
    }
}