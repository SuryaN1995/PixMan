package com.example.pixman.helper

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.pixman.R
import java.io.File
import java.io.FileOutputStream

/**
 * Created by SURYA N on 3/2/20.
 */
class PhotoEditorSDK(photoEditorSDKBuilder: PhotoEditorSDKBuilder) {

    private var context: Context? = null
    private var parentView: RelativeLayout? = null
    private var imageView: ImageView? = null
    private var deleteView: View? = null
    private var addedViews: MutableList<View>? = null
    private var addTextRootView: View? = null

    init {
        this.context = photoEditorSDKBuilder.context
        this.parentView = photoEditorSDKBuilder.parentView
        this.imageView = photoEditorSDKBuilder.imageView
        this.deleteView = photoEditorSDKBuilder.deleteView
        addedViews = ArrayList()
    }

    fun addImage(desiredImage: Bitmap) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val imageRootView = inflater.inflate(R.layout.image, null)
        val imageView = imageRootView.findViewById(R.id.photo_editor_sdk_image_iv) as ImageView
        imageView.setImageBitmap(desiredImage)
        imageView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView?.addView(imageRootView, params)
        addedViews?.add(imageRootView)
    }

    fun addText(text: String, colorCodeTextView: Int) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        addTextRootView = inflater.inflate(R.layout.text, null)
        val addTextView = addTextRootView?.findViewById(R.id.photo_editor_sdk_text_tv) as TextView
        addTextView.gravity = Gravity.CENTER
        addTextView.text = text
        if (colorCodeTextView != -1)
            addTextView.setTextColor(colorCodeTextView)

        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView?.addView(addTextRootView, params)
        addedViews?.add(addTextRootView!!)

    }




    fun viewUndo() {
        if ((addedViews?.size ?: 0) > 0) {
            parentView?.removeView(addedViews?.removeAt((addedViews?.size ?: 0) - 1))
        }
    }

    private fun viewUndo(removedView: View) {
        if ((addedViews?.size ?: 0) > 0) {
            if (addedViews?.contains(removedView) == true) {
                parentView?.removeView(removedView)
                addedViews?.remove(removedView)
            }
        }
    }

    fun clearAllViews() {
        addedViews?.indices?.forEach {
            parentView?.removeView(addedViews?.get(it))
        }
    }

    fun saveImage(folderName: String, imageName: String): String {
        var selectedOutputPath = ""
        if (isSDCARDMounted()) {
            val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                folderName
            )
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PhotoEditorSDK", "Failed to create directory")
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.path + File.separator + imageName
            Log.d("PhotoEditorSDK", "selected camera path $selectedOutputPath")
            val file = File(selectedOutputPath)
            try {
                val out = FileOutputStream(file)
                if (parentView != null) {
                    parentView?.isDrawingCacheEnabled = true
                    parentView?.drawingCache?.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return selectedOutputPath
    }

    private fun isSDCARDMounted(): Boolean {
        val status = Environment.getExternalStorageState()
        return status == Environment.MEDIA_MOUNTED
    }

    private fun convertEmoji(emoji: String): String {
        var returnedEmoji = ""
        try {
            val convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16)
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt)
        } catch (e: NumberFormatException) {
            returnedEmoji = ""
        }

        return returnedEmoji
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    fun onEditTextClickListener(text: String, colorCode: Int) {
        if (addTextRootView != null) {
            parentView?.removeView(addTextRootView)
            addedViews?.remove(addTextRootView!!)
        }
    }

    fun onRemoveViewListener(removedView: View) {
        viewUndo(removedView)
    }

    class PhotoEditorSDKBuilder(val context: Context) {
        var parentView: RelativeLayout? = null
        var imageView: ImageView? = null
        var deleteView: View? = null

        fun parentView(parentView: RelativeLayout): PhotoEditorSDKBuilder {
            this.parentView = parentView
            return this
        }

        fun childView(imageView: ImageView): PhotoEditorSDKBuilder {
            this.imageView = imageView
            return this
        }

        fun deleteView(deleteView: View): PhotoEditorSDKBuilder {
            this.deleteView = deleteView
            return this
        }

        fun buildPhotoEditorSDK(): PhotoEditorSDK {
            return PhotoEditorSDK(this)
        }
    }
}