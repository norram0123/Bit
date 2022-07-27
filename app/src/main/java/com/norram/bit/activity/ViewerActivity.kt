package com.norram.bit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        val chosenImageView: ImageView = findViewById(R.id.chosenImageView)
        val openButton: Button = findViewById(R.id.openButton)
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val screen = Screen.getInstance()

        Picasso.get()
            .load(imageUrl)
            .resize(screen.width, screen.width)
            .centerInside() // maintain aspect ratio
            .into(chosenImageView)

        openButton.setOnClickListener {
            val uri = Uri.parse(imageUrl)
            val exIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(exIntent)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}