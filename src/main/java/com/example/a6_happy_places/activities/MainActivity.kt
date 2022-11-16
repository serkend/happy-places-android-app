package com.example.a6_happy_places.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6_happy_places.adapters.HappyPlacesAdapter
import com.example.a6_happy_places.database.DatabaseHandler
import com.example.a6_happy_places.databinding.ActivityMainBinding
import com.example.a6_happy_places.model.HappyPlaceModel
import com.example.a6_happy_places.utils.SwipeToDeleteCallback
import com.example.a6_happy_places.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.fabAddHappyPlaces.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_HAPPY_PLACE_ACTIVITY_CODE)
        }

        getHappyPlacesListFromlocalDB()

        viewBinding.swipeToRefreshLayout.setOnRefreshListener {
            viewBinding.recyclerView.adapter!!.notifyDataSetChanged()
            viewBinding.swipeToRefreshLayout.isRefreshing = false

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_HAPPY_PLACE_ACTIVITY_CODE) {
                getHappyPlacesListFromlocalDB()
            }
        }
    }

    private fun setupHappyPlacesRecyclerView(
        happyPlacesList: ArrayList<HappyPlaceModel>
    ) {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.setHasFixedSize(true)

        val happyPlacesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        happyPlacesAdapter.setOnPlaceListener(object : HappyPlacesAdapter.OnPlaceListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        viewBinding.recyclerView.adapter = happyPlacesAdapter

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = viewBinding.recyclerView.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_HAPPY_PLACE_ACTIVITY_CODE
                )
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = viewBinding.recyclerView.adapter as HappyPlacesAdapter
                adapter.notifyDeleteItem(viewHolder.adapterPosition)
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

    }

    private fun getHappyPlacesListFromlocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.size > 0) {
            viewBinding.recyclerView.visibility = View.VISIBLE
            viewBinding.tvNoRecordsAvailable.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlaceList)
        } else {
            Log.e("HappyPlaceList", " is empty")
            viewBinding.recyclerView.visibility = View.GONE
            viewBinding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    companion object {
        const val ADD_HAPPY_PLACE_ACTIVITY_CODE = 1
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}