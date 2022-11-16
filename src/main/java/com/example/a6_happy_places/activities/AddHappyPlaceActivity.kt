package com.example.a6_happy_places.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.a6_happy_places.R
import com.example.a6_happy_places.database.DatabaseHandler
import com.example.a6_happy_places.databinding.ActivityAddHappyPlaceBinding
import com.example.a6_happy_places.model.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    lateinit var viewBinding: ActivityAddHappyPlaceBinding
    private var mHappyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewBinding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {

            mHappyPlaceDetails =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"
            viewBinding.etTitle.setText(mHappyPlaceDetails!!.title)
            viewBinding.etDescription.setText(mHappyPlaceDetails!!.description)
            viewBinding.etDate.setText(mHappyPlaceDetails!!.date)
            viewBinding.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude!!.toDouble()
            mLongitude = mHappyPlaceDetails!!.longitude!!.toDouble()

            viewBinding.ivPlaceImage.setImageURI(Uri.parse(mHappyPlaceDetails!!.image))
            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            viewBinding.btnSave.text = "UPDATE"

        }

        viewBinding.etDate.setOnClickListener(this)
        viewBinding.tvAddImg.setOnClickListener(this)
        viewBinding.ivPlaceImage.setOnClickListener(this)
        viewBinding.etDate.setOnClickListener(this)
        viewBinding.etLocation.setOnClickListener(this)

        viewBinding.btnSave.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tvAddImg -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from Gallery",
                    "Capture photo from camera"
                )
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> capturePhotoWithCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {
                if (viewBinding.etTitle.text.isNullOrEmpty() ||
                    viewBinding.etDescription.text.isNullOrEmpty() ||
                    viewBinding.etLocation.text.isNullOrEmpty() ||
                    viewBinding.ivPlaceImage == null
                ) {
                    Toast.makeText(
                        this,
                        "Please fill all fields!", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val happyPlaceModel = HappyPlaceModel(
                        mHappyPlaceDetails?.id ?: 0,
                        viewBinding.etTitle.text.toString(),
                        saveImageToInternalStorage.toString(),
                        viewBinding.etDescription.text.toString(),
                        viewBinding.etDate.text.toString(),
                        viewBinding.etLocation.text.toString(),
                        mLatitude.toString(),
                        mLongitude.toString()
                    )
                    val dbHandler = DatabaseHandler(this)

                    if (mHappyPlaceDetails == null) {
                        var addHappyPlaceResult = dbHandler.addHappyPlace(happyPlaceModel)
                        if (addHappyPlaceResult > 0) {
                            setResult(RESULT_OK)
                            finish()
                        }
                    } else {
                        var updateHappyPlaceResult = dbHandler.updateHappyPlace(happyPlaceModel)
                        if (updateHappyPlaceResult > 0) {
                            setResult(RESULT_OK)
                            finish()
                        }
                    }

                }
                //TODO save model to db
            }

            R.id.et_location -> {
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME,
                    Place.Field.LAT_LNG, Place.Field.ADDRESS
                )

                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this@AddHappyPlaceActivity)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
//                    Toast.makeText(
//                        this@AddHappyPlaceActivity,
//                        "All permissions are granted.",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()
    }

    private fun capturePhotoWithCamera() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA

        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
//                    Toast.makeText(
//                        this@AddHappyPlaceActivity,
//                        "All permissions are granted.",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    val cameraIntent =
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage(
            "Looks like you have turned off permission ." +
                    "You can turned it on back under settings ."
        )
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, "")
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        viewBinding.etDate.setText(sdf.format(cal.time).toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        viewBinding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                        saveImageToInternalStorage = contentURI
                    } catch (exception: Exception) {
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved image: ", "Path: $saveImageToInternalStorage")
                viewBinding.ivPlaceImage.setImageBitmap(thumbnail)

            } else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)
                viewBinding.etLocation.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}