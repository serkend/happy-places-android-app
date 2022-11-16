package com.example.a6_happy_places.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.a6_happy_places.R
import com.example.a6_happy_places.databinding.ActivityHappyPlaceDetailsBinding
import com.example.a6_happy_places.model.HappyPlaceModel
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrInterface

class HappyPlaceDetailsActivity : AppCompatActivity() {
    var viewBinding: ActivityHappyPlaceDetailsBinding? = null
    var slidr:SlidrInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)

        var happyPlaceDetailModel: HappyPlaceModel? = null

        slidr = Slidr.attach(this)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailModel =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        if (happyPlaceDetailModel != null) {
            setSupportActionBar(viewBinding!!.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            viewBinding!!.toolbarHappyPlaceDetail.setNavigationOnClickListener {
                onBackPressed()
            }

            viewBinding!!.ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            viewBinding!!.tvDescription.text = happyPlaceDetailModel.description
            viewBinding!!.tvLocation.text = happyPlaceDetailModel.location

        }


    }
}