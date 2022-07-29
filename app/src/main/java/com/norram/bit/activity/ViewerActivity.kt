package com.norram.bit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import com.norram.bit.databinding.ActivityViewerBinding
import com.squareup.picasso.Picasso

class ViewerActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_viewer)
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val screen = Screen.getInstance()

        Picasso.get()
            .load(imageUrl)
            .resize(screen.width, screen.width)
            .centerInside() // maintain aspect ratio
            .into(binding.chosenImageView)

        binding.openButton.setOnClickListener {
            val uri = Uri.parse(imageUrl)
            val exIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(exIntent)
        }
    }
}