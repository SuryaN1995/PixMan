package com.example.pixman.ui

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.pixman.R
import com.example.pixman.databinding.ActivityOperationBinding
import com.example.pixman.helper.AppConstants
import com.example.pixman.helper.flip
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*


class OperationActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityOperationBinding? = null
    private var bitmap: Bitmap? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_operation)
        binding?.clickHandler = this
        uri = Uri.parse(intent?.getStringExtra(AppConstants.IMAGE_URI))
        bitmap = uri?.let { getPath(it) }
        AppConstants.operations.clear()
        initView()
        saveImageEnable()
    }

    private fun initView() {
        binding?.image?.let {
            Glide.with(this)
                .load(bitmap)
                .apply(RequestOptions().override(100))
                .into(it)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tv_flip_hor -> {
                AppConstants.operations.add(AppConstants.Option.FLIP_HORIZONTAL)
                flipImage(true)
                saveImageEnable()
            }
            R.id.tv_flip_ver -> {
                AppConstants.operations.add(AppConstants.Option.FLIP_VERTICAL)
                flipImage(false)
                saveImageEnable()
            }
            R.id.save -> {
                if(AppConstants.operations.isNotEmpty()){
                    saveImage()
                }
            }
            R.id.tv_add_text -> {
                if (!AppConstants.operations.contains(AppConstants.Option.ADD_TEXT)) {
                    AppConstants.operations.add(AppConstants.Option.ADD_TEXT)
                    setLabel()
                    saveImageEnable()
                }
            }
            R.id.tv_opacity -> {
                if (!AppConstants.operations.contains(AppConstants.Option.OPACITY)) {
                    AppConstants.operations.add(AppConstants.Option.OPACITY)
                    setOpacity()
                    saveImageEnable()
                }
            }
            R.id.tv_prev -> {

            }
        }
    }

    private fun saveImageEnable() {
        binding?.save?.isEnabled = AppConstants.operations.isNotEmpty()
    }

    private fun saveImage() {
        val filename = Date().time.toString() + ".png"
        val folder = getExternalFilesDir(null)?.absolutePath?:""
        File(folder).mkdirs()
        val file = File(folder,filename)
        try {
            val fos = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 80, fos)
            fos.flush()
            fos.close()
        }catch (e: Exception){
            e.printStackTrace()
        }finally {
            finish()
        }
    }

    private fun setLabel() {
        val trans =
            Bitmap.createBitmap(bitmap?.width ?: 0, bitmap?.height ?: 0, Bitmap.Config.ARGB_8888)
        val canvas = trans?.let { Canvas(it) }
        val paint = Paint()
        paint.color = Color.GREEN
        paint.textSize = 300f
        bitmap?.let { canvas?.drawBitmap(it, 0f, 0f, null) }
        val bounds = Rect()
        val text = "GameGreedy"
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = (trans.width - paint.measureText(text)) / 2
        val y = trans.height / 2
        val bkg = Paint()
        bkg.color = Color.BLACK
        canvas?.drawRect(x,(trans.height/2 - 200).toFloat(), x + 1700,(trans.height/2 + 100).toFloat(),bkg)
        canvas?.drawText(text, x, y.toFloat(), paint)
        bitmap = trans
        initView()
    }

    private fun setOpacity() {
        val trans =
            Bitmap.createBitmap(bitmap?.width ?: 0, bitmap?.height ?: 0, Bitmap.Config.ARGB_8888)
        val canvas = trans?.let { Canvas(it) }
        canvas?.drawARGB(0, 0, 0, 0)
        val paint = Paint()
        paint.alpha = 50
        bitmap?.let { canvas?.drawBitmap(it, 0f, 0f, paint) }
        bitmap = trans
        initView()
    }

    private fun flipImage(horizontal: Boolean) {
        val cx = (bitmap?.width ?: 0) / 2f
        val cy = (bitmap?.height ?: 0) / 2f
        val x = if (horizontal) -1f else 1f
        val y = if (horizontal) 1f else -1f
        bitmap = bitmap?.flip(x, y, cx, cy)
        initView()
    }

    private fun getPath(uri: Uri): Bitmap? {
        var bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        return bitmap
    }


}
