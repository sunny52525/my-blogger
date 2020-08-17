package com.shaun.myblogger

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HtmlImageGetter(
    private val scope: LifecycleCoroutineScope,
    private val res: Resources,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)

        scope.launch(Dispatchers.IO) {
            runCatching {
                Log.d("TAG", "getDrawable: $url")

                val bitmap = Picasso.get().load(url)
                    .get()


                val drawable = BitmapDrawable(res, bitmap)


                val width = getScreenWidth() - 150
                val aspectRatio: Float =
                    (drawable.intrinsicWidth.toFloat()) / (drawable.intrinsicHeight.toFloat())

                val height = width / aspectRatio


                drawable.setBounds(10, 20, width, height.toInt())

                holder.setDrawable(drawable)
                holder.setBounds(10, 20, width, height.toInt())

                withContext(Dispatchers.Main) {
                    htmlTextView.text = htmlTextView.text
                }
            }
        }

        return holder
    }

    internal class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) : BitmapDrawable(res, bitmap) {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }
    }

    fun getScreenWidth() =
        Resources.getSystem().displayMetrics.widthPixels


}