package com.dgc.photoediting.stylesegment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dore.coreai.vision.DoreImage
import kotlinx.android.synthetic.main.image_segment_view.*
import java.io.IOException
import android.util.Size
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgc.photoediting.R
import com.dore.deepstyle.DeepStyleListener
import com.dore.deepstyle.DeepStyleManager
import kotlinx.android.synthetic.main.image_segment_view.browse_img_preview
import kotlinx.android.synthetic.main.image_segment_view.btnBrowse
import kotlinx.android.synthetic.main.image_segment_view_style.*
import kotlinx.android.synthetic.main.reamtime_deepstyle_view_style.*
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList





private class picadapter(internal var context: Context, internal var mData: List<String>) :
    RecyclerView.Adapter<picadapter.myViewHolder>() {

    private var rView : ImageSegmentViewStyle = ImageSegmentViewStyle()

    public fun setRview(sView : ImageSegmentViewStyle){
        this.rView = sView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): picadapter.myViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.style_listitem_style, parent, false)
        //return myViewHolder(view)
        return myViewHolder(view).listen { pos, type ->
            val cur_txt =  mData[pos]
            this.rView.load_style(cur_txt)
            // bEngine.load_style(view.lbstyleItem.text.toString())
        }
    }

    override fun onBindViewHolder(holder: picadapter.myViewHolder, position: Int) {
        holder.lbTxt.text = mData[position]
    }

    override fun getItemCount(): Int {
        return mData.size
    }



    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var lbTxt: TextView

        init {
            lbTxt = itemView.findViewById(R.id.lbstyleItem)
        }
    }
}

class ImageSegmentViewStyle : AppCompatActivity() {

    private val bEngine: DeepStyleManager = DeepStyleManager()
    private val GALLERY = 1
    private  var isStyleLoaded : Boolean = false

    private var list_col : ArrayList<String> = ArrayList()
    private var recyclerAdapter: picadapter? = null
    var conf: Bitmap.Config = Bitmap.Config.ARGB_4444
    public  var cur_pic: Bitmap = Bitmap.createBitmap(1, 1, conf);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_segment_view_style)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        init_button_event()
        init_view()
        progress_Bar.visibility = View.INVISIBLE

        val lickeycode = getString(R.string.deepstyle)
        bEngine.init_data(this,lickeycode)

        bEngine.setDeepStyleListener( object: DeepStyleListener {
            override fun onSuccess(info: String) {
                progress_Bar.visibility = View.INVISIBLE
                isStyleLoaded = true
                //show_message()
                run_segment()

            }

            override fun onFailure(error: String) {
                progress_Bar.visibility = View.INVISIBLE

            }

            override fun onProgressUpdate(progress: String) {
                runOnUiThread {
                    progress_Bar.visibility = View.VISIBLE
                    progress_Bar.setProgress( Integer.parseInt(progress))
                }
            }

        })





    }

    fun init_view(){
        for (x in 0..188) {
            list_col.add("s" + (x+1).toString())
        }
        var strArray  = Array<String>(list_col.size,  { "$it" })
        list_col.toArray(strArray)

        val listView: RecyclerView = findViewById(R.id.lststyle_viewpic)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        listView.layoutManager = layoutManager

        recyclerAdapter = picadapter(this, list_col)
        recyclerAdapter?.setRview(this)

        // val myListAdapter = StyleListAdapter(this, strArray )
        listView.adapter = recyclerAdapter


    }


    public fun load_style(styleID : String){

        bEngine.load_style(styleID)
    }

    public fun show_message() {

        Toast.makeText(this, "Style filter loaded.", Toast.LENGTH_LONG).show()
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




        btnBrowse.setOnClickListener {

            choosePhotoFromGallary()
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
                    cur_pic = bitmap
                    browse_img_preview.setImageBitmap(bitmap)


                }
                catch (e: IOException) {
                    e.printStackTrace()

                }

            }

        }

    }

    public fun run_segment(){

        if(isStyleLoaded) {

            //val bm =  (browse_img_preview.getDrawable() as BitmapDrawable).bitmap
            val dimage = DoreImage.fromBitmap(cur_pic)
            var result = bEngine.run(dimage)
            System.out.println("resultvv"+result)
            runOnUiThread {
                val outimg = result?.toBitmap(Size(cur_pic.width, cur_pic.height))
                browse_img_preview.setImageBitmap(outimg)
                if (outimg != null) {
                    val rnds = (0..188).random()
                    saveImage(outimg,rnds)
                }
            }

        }


    }
    private fun saveImage(finalBitmap: Bitmap, image_name: Int) {
        System.out.println("resultvv"+finalBitmap+" "+image_name)
        //String root = Environment.getExternalStorageDirectory().toString();
        val root = Environment.getExternalStorageDirectory()
        val myDir = File(root.toString())
        myDir.mkdirs()
        val fname = "Image-$image_name.jpg"
        //   File file = new File(myDir, fname);
//        File file = new File(root.getAbsolutePath()+"/DCIM/Screenshots/img.jpg");
        val file = File("/sdcard/cute_selfie_sample/$fname")
        if (file.exists()) file.delete()
        Log.i("LOAD", root.toString() + fname)
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}