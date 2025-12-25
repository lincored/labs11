package com.example.myapp

import android.Manifest
import android.os.Environment
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import android.widget.TextView
import android.location.Location
import android.os.CountDownTimer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.io.FileWriter
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Async.Execute
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import android.content.Context
import android.net.NetworkInfo
import android.telephony.TelephonyManager

class Location : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var previousData = ""
    private lateinit var Latitude: TextView
    private lateinit var Longitude: TextView
    private lateinit var Altitude: TextView
    private lateinit var Current_time: TextView

    private lateinit var _cellID: TextView
    private lateinit var _pci: TextView
    private lateinit var _rsrp: TextView
    private lateinit var _bandwith: TextView
    private lateinit var _rsrq: TextView



    private val gson = Gson()
    private var timer: CountDownTimer? = null
    val TAG = "TelephonyActivity"

    var jsonString: String? = null
    private val log_tag: String = "MY_LOG_TAG"
    private var clientRunning = false
    private var getting_data = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        UI()
        checkLocationPermission()
        startClientInBackground()
    }

    private fun start_getting_data(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    p0.lastLocation?.let{SaveDataAndUpdateUI(it, getNetworkInfo())}
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun getNetworkInfo(): NetworkData?{
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoList = telephonyManager.allCellInfo

        for(cell in cellInfoList){
            if(cell is android.telephony.CellInfoLte && cell.isRegistered){
                return NetworkData(
                    cellID = cell.cellIdentity.ci,
                    pci = cell.cellIdentity.pci,
                    rsrp = cell.cellSignalStrength.rsrp,
                    band = cell.cellIdentity.bandwidth,
                    rsrq = cell.cellSignalStrength.rsrq
                )
            }
        }
        return null
    }
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                start_getting_data()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
            }
            else -> {
                requestPermissions()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    start_getting_data()
                }
                return
            }
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    private fun UI(){
        Latitude = findViewById(R.id.Latitude)
        Longitude = findViewById(R.id.Longitude)
        Altitude = findViewById(R.id.Altitude)
        Current_time = findViewById(R.id.Current_time)
        _cellID = findViewById(R.id.CellID)
        _pci = findViewById(R.id.pci)
        _rsrp = findViewById(R.id.rsrp)
        _bandwith = findViewById(R.id.bandwith)
        _rsrq = findViewById(R.id.rsrq)
    }

    private fun SaveDataAndUpdateUI(location: Location, networkData: NetworkData?){
        Latitude.text = "Latitude: ${location.latitude}"
        Longitude.text = "Longitude: ${location.longitude}"
        Altitude.text = "Altitude: ${location.altitude} meters"
        _cellID.text = "Cell id : ${networkData?.cellID}"
        _pci.text = "pci : ${networkData?.pci}"
        _rsrp.text = "rsrp : ${networkData?.rsrp}"
        _bandwith.text = "bandwith : ${networkData?.band}"
        _rsrq.text = "rsrq : ${networkData?.rsrq}"


        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val currentTime = sdf.format(Date(location.time))

        Current_time.text = "Current Time: $currentTime"

        val locationData = LocationData(
            output_latitude = location.latitude,
            output_longitude = location.longitude,
            output_altitude = location.altitude,
            output_Current_time = location.time
        )
        saveLocationDataToJSON(locationData, networkData)
    }

    private fun startClientInBackground() {
        clientRunning = true;
        lifecycleScope.launch(Dispatchers.IO) {
            while(clientRunning)
                try{
                    startClient()
                    delay(5000)
                } catch(e: Exception) {
                    Log.e(log_tag, "client error")
                    delay(5000)
                }
        }
    }
    fun startClient() {
        val context = ZMQ.context(1)
        val socket = context.socket(ZMQ.REQ)

        try{
            socket.connect("tcp://192.168.56.1:2222")
            socket.setReceiveTimeOut(10000)

            val currentData  = readJsonFile()

            if(currentData.isNotEmpty()){
                socket.send(currentData.toByteArray(ZMQ.CHARSET))

                val reply = socket.recvStr()
                if(reply == null){
                    Log.d(log_tag, "client: server didn't response")
                } else {
                    Log.d(log_tag, "client: received $reply")
                }
            }else {
                Thread.sleep(5000)
            }
        } catch(e: Exception){
            Log.e(log_tag, "client error: ${e.message}")
        } finally {
            socket.close()
            context.close()
        }
    }

    private fun readJsonFile() : String {
        val documentsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(documentsDir, "all_data.json")

        if (file.exists()) {
            val lines = file.readLines()
            return if (lines.isNotEmpty()) {
                lines.last()
            } else {
                "Nothing to transmit"
            }
        } else {
            return "Nothing to transmit"
        }
    }


    private fun saveLocationDataToJSON(locationData: LocationData, networkData: NetworkData?) {
        val documentsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(documentsDir, "all_data.json")

        if (!file.exists()) {
            file.createNewFile()
        }
        val combinedData = CombinedData(locationData, networkData)
        FileWriter(file, true).use { writer ->
            writer.append("${gson.toJson(combinedData)}\n")
        }
    }

}
data class CombinedData(
    val location : LocationData,
    val network : NetworkData?
)
data class LocationData(
    val output_latitude: Double,
    val output_longitude: Double,
    val output_altitude: Double,
    val output_Current_time: Long
)

data class NetworkData(
    val cellID : Int,
    val pci : Int,
    val rsrp : Int,
    val band : Int,
    val rsrq : Int
)