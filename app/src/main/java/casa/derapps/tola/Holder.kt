package casa.derapps.tola

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView
    val description: TextView
    val url: TextView

    init {
        name = itemView.findViewById(R.id.name_recycler)
        description = itemView.findViewById(R.id.description_recycler)
        url = itemView.findViewById(R.id.url_recycler)
    }

    fun bind(data: Source) {

        name.text = data.name
        description.text = data.description
        url.text = data.url


    }
}