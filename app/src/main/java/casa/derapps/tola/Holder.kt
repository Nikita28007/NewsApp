package casa.derapps.tola

import android.graphics.drawable.DrawableWrapper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView
    val description: TextView
    var image : ImageView

    //  val url: TextView
    val arr = ArrayList<Int>()
    init {
        name = itemView.findViewById(R.id.name_recycler)
        description = itemView.findViewById(R.id.description_recycler)
        // url = itemView.findViewById(R.id.url_recycler)
        image = itemView.findViewById(R.id.imageRecycler)
        addArr()
    }

    private fun addArr(){
        arr.add(R.drawable.sport1)
        arr.add(R.drawable.sport2)
        arr.add(R.drawable.sport3)
        arr.add(R.drawable.sport4)
        arr.add(R.drawable.sport5)
        arr.add(R.drawable.sport6)
        arr.add(R.drawable.sport7)
        arr.add(R.drawable.sport8)
        arr.add(R.drawable.sport9)
        arr.add(R.drawable.sport10)
        arr.add(R.drawable.sport11)
    }
    fun bind(data: NewsData, position : Int) {

        name.text = data.name
        description.text = data.description
        // url.text = data.url
        image.setImageResource(arr[position])

    }
}