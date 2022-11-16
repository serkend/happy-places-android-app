package com.example.a6_happy_places.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.a6_happy_places.R
import com.example.a6_happy_places.activities.AddHappyPlaceActivity
import com.example.a6_happy_places.activities.MainActivity
import com.example.a6_happy_places.database.DatabaseHandler
import com.example.a6_happy_places.databinding.ItemHappyPlaceBinding
import com.example.a6_happy_places.model.HappyPlaceModel

class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: OnPlaceListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place, parent,
                false
            )
        )
    }

    fun setOnPlaceListener(onPlaceListener: OnPlaceListener) {
        this.listener = onPlaceListener
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.viewBinding.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.viewBinding.tvName.text = model.title
            holder.viewBinding.tvDescription.text = model.description

            holder.viewBinding.happyPlaceCV.setOnClickListener{
                if (listener != null) {
                    listener!!.onClick(position, model)
                }
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode:Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun notifyDeleteItem(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])
       if(isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    interface OnPlaceListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(val view: View) :
        RecyclerView.ViewHolder(view) {
        val viewBinding = ItemHappyPlaceBinding.bind(view)
    }
}