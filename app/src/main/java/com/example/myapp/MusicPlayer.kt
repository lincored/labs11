
package com.example.myapp

import android.Manifest
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import android.net.Uri
import java.util.ArrayList

class MusicPlayer : AppCompatActivity() {
    private val storagePermissionRequestCode = 100
    private lateinit var listView: ListView

    private lateinit var mp3Files: ArrayList<String>
    private lateinit var fileNames: ArrayList<String>

    private lateinit var SongName: TextView
    private lateinit var TrackDuration: TextView
    private lateinit var CurTime: TextView
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var timerSeekBar: SeekBar
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var pauseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    private var currentTrackIndex: Int = 0
    private var isPlaying: Boolean = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mp3Files = ArrayList()
        fileNames = ArrayList()

        initializeUI()

        if (checkStoragePermission()) {
            loadMusicFiles()
        } else {
            requestStoragePermission()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                storagePermissionRequestCode
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                storagePermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == storagePermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusicFiles()
            }
        }
    }

    private fun loadMusicFiles() {
        mp3Files.clear()
        fileNames.clear()

        val musicDirs = arrayOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            File(Environment.getExternalStorageDirectory(), "Music")
        )

        musicDirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                findMp3Files(dir)
            }
        }

        if (mp3Files.isEmpty()) {
            Toast.makeText(this, "Музыка не найдена", Toast.LENGTH_SHORT).show()
        } else {
            setupMusic()
            VolumeSeekBar()
            setupButtonListeners()
            timerSeekBar()
        }
    }

    private fun findMp3Files(directory: File) {
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> findMp3Files(file)
                file.name.endsWith(".mp3", ignoreCase = true) -> {
                    fileNames.add(file.nameWithoutExtension)
                    mp3Files.add(file.absolutePath)
                }
            }
        }
    }



    private fun initializeUI() {
        SongName = findViewById(R.id.SongName)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        timerSeekBar = findViewById(R.id.timerSeekBar)
        pauseButton = findViewById(R.id.B_Pause)
        nextButton = findViewById(R.id.B_NEXT)
        prevButton = findViewById(R.id.B_PREV)
        TrackDuration = findViewById(R.id.TrackDuration)
        CurTime = findViewById(R.id.CurTime)
        //listView = findViewById(R.id.musicListView)

        findViewById<Button>(R.id.B_back).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupButtonListeners() {
        pauseButton.setOnClickListener { Pause() }
        nextButton.setOnClickListener { Next() }
        prevButton.setOnClickListener { Previous() }
    }


    private fun timerSeekBar(){
        var duration: Int = getDuration()
        timerSeekBar.max = duration
        timerSeekBar.progress = 0
        TrackDuration.text = formattedDuration(duration)

        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    private fun VolumeSeekBar(){
        volumeSeekBar.max = 100
        volumeSeekBar.progress = 50
        mediaPlayer.setVolume(0.5f, 0.5f)

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun setupMusic() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(mp3Files[currentTrackIndex])))

        timerSeekBar.max = mediaPlayer.duration

        SongName.text = fileNames[currentTrackIndex]
        getDuration()

        mediaPlayer.setOnCompletionListener {
            Next()
        }
        startUpdatingUI()
    }
    fun Pause() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
                handler.removeCallbacks(updateSeekBar)
            } else {
                mediaPlayer.start()
                isPlaying = true
                startUpdatingUI()
            }
        }
    }

    fun Next() {
        Pause()
        if(currentTrackIndex == mp3Files.size - 1){
            currentTrackIndex = 0
        }else{
            currentTrackIndex++
        }
        setupMusic()
        Pause()
    }

    fun Previous() {
        Pause()
        if (currentTrackIndex > 0) {
            mediaPlayer.release()
            currentTrackIndex--
        } else {
            mediaPlayer.release()
            currentTrackIndex = mp3Files.size - 1
        }
        setupMusic()
        Pause()
    }

    private val updateSeekBar: Runnable = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                timerSeekBar.progress = currentPosition
                CurTime.text = formattedDuration(currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun startUpdatingUI() {
        handler.post(updateSeekBar)
    }

    fun formattedDuration(duration: Int): String{
        val minutes = (duration / (1000 * 60)) % 60
        val seconds = (duration / 1000) % 60
        val formattedDuration = String.format("%02d:%02d", minutes, seconds)
        return formattedDuration
    }
    fun getDuration(): Int {

        if (mediaPlayer == null) {
            TrackDuration.text = "00:00"
            return 0
        }
        var duration: Int = mediaPlayer.duration

        TrackDuration.text = formattedDuration(duration)

        return duration

    }
}
