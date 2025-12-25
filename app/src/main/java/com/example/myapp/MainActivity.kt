package com.example.myapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.B_Calculator).setOnClickListener{
            val intent = Intent(this, CalculatorPage::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.B_Music).setOnClickListener{
            val intent = Intent(this, MusicPlayer::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.Location).setOnClickListener{
            val intent = Intent(this, Location::class.java)
            startActivity(intent)
        }
    }
}