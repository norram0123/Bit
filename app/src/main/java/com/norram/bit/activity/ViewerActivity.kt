package com.norram.bit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewTreeObserver
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import com.norram.bit.databinding.ActivityViewerBinding
import com.squareup.picasso.Picasso
import kotlin.math.*

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private lateinit var mPanGestureDetector: GestureDetectorCompat
    private var mScaleFactor = 1.0f
    private var mTranslationX = 0f
    private var mTranslationY = 0f
    private var mImageWidth = 0f
    private var mImageHeight = 0f
    private var mDefaultImageWidth = 0f
    private var mDefaultImageHeight = 0f
    private var mViewPortWidth = 0f
    private var mViewPortHeight = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_viewer)
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        mPanGestureDetector = GestureDetectorCompat(this, PanListener())

        val viewTreeObserver = binding.chosenImageView.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.chosenImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    Picasso.get()
                        .load(imageUrl)
                        .resize(binding.chosenImageView.width, binding.chosenImageView.width)
                        .centerInside() // maintain aspect ratio
                        .into(binding.chosenImageView)

                    val imageAspectRatio = binding.chosenImageView.height.toFloat() / binding.chosenImageView.width.toFloat()
                    val viewAspectRatio = binding.chosenImageView.height.toFloat() / binding.chosenImageView.width.toFloat()

                    mDefaultImageWidth = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        binding.chosenImageView.width.toFloat()
                    } else {
                        // Portrait image
                        binding.chosenImageView.height.toFloat() / imageAspectRatio
                    }

                    mDefaultImageHeight = if (imageAspectRatio < viewAspectRatio) {
                        // landscape image
                        binding.chosenImageView.width.toFloat() * imageAspectRatio
                    } else {
                        // Portrait image
                        binding.chosenImageView.height.toFloat()
                    }

                    mImageWidth = mDefaultImageWidth
                    mImageHeight = mDefaultImageHeight

                    mViewPortWidth = binding.chosenImageView.width.toFloat()
                    mViewPortHeight = binding.chosenImageView.height.toFloat()
                }
            })
        }

        binding.openButton.setOnClickListener {
            val uri = Uri.parse(imageUrl)
            val exIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(exIntent)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        mPanGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class PanListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val translationX = mTranslationX - distanceX
            val translationY = mTranslationY - distanceY

            adjustTranslation(translationX, translationY)

            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            mScaleFactor *= mScaleGestureDetector.scaleFactor
            mScaleFactor = max(1.0f, min(mScaleFactor, 5.0f))
            binding.chosenImageView.scaleX = mScaleFactor
            binding.chosenImageView.scaleY = mScaleFactor
            mImageWidth = mDefaultImageWidth * mScaleFactor
            mImageHeight = mDefaultImageHeight * mScaleFactor
            adjustTranslation(mTranslationX, mTranslationY)
            return true
        }
    }

    private fun adjustTranslation(translationX: Float, translationY: Float) {
        val translationXMargin = abs((mImageWidth - mViewPortWidth) / 2)
        val translationYMargin = abs((mImageHeight - mViewPortHeight) / 2)

        mTranslationX = if (translationX < 0) {
            max(translationX, -translationXMargin)
        } else {
            min(translationX, translationXMargin)
        }

        mTranslationY = if (translationY < 0) {
            max(translationY, -translationYMargin)
        } else {
            min(translationY, translationYMargin)
        }

        binding.chosenImageView.translationX = mTranslationX
        binding.chosenImageView.translationY = mTranslationY
    }
}