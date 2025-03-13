package com.dgc.photoediting.hairsegment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.image_segment_view.*
import java.io.IOException
import android.graphics.drawable.BitmapDrawable
import com.dgc.photoediting.R
import com.dore.hairsegment.HairSegmentManager



class ImageSegmentViewhair : AppCompatActivity() {

    private val bEngine: HairSegmentManager = HairSegmentManager()
    private val GALLERY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_segment_view_hair)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()

        val lickeycode = getString(R.string.hair)
        bEngine.init_data(this,lickeycode)


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun init_button_event() {

        btnRun.isEnabled = false


        btnBrowse.setOnClickListener {

            choosePhotoFromGallary()
        }

        btnRun.setOnClickListener {

            run_segment()
        }

    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    browse_img_preview.setImageBitmap(bitmap)
                    btnRun.isEnabled = true

                }
                catch (e: IOException) {
                    e.printStackTrace()

                }

            }

        }

    }

    private fun run_segment(){

        val bm = (browse_img_preview.getDrawable() as BitmapDrawable).bitmap
        val dimage =  DoreImage.fromBitmap(bm)
        var result = bEngine.run(dimage)

        runOnUiThread {
            browse_img_preview.setImageBitmap(result?.transparentImage)
            btnRun.isEnabled = false
        }


    }



}