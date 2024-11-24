package com.example.smartotto  // Ensure this matches your package structure

import android.os.Bundle
import android.os.Handler  // Import for Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity  // For AppCompatActivity
import okhttp3.*
import org.json.JSONObject  // Import for JSONObject
import java.io.File
import java.io.InputStream
import java.io.FileInputStream
import java.io.IOException  // Import for IOException
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.concurrent.TimeUnit  // Import for TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.net.ssl.KeyManagerFactory

import javax.net.ssl.SSLSession
import android.widget.Toast  // Import for Toast
import java.io.ByteArrayInputStream
import android.util.Log
import java.util.zip.GZIPInputStream
import okio.buffer
import okio.source
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


// Pages
// 1. Temperature
// 2. Rollshuter
// 3. Lights
// 4. PV Anlage // Power Consumption
// 5. Battery and Connection


class MainActivity : AppCompatActivity() {

    private lateinit var temperatureTextView: TextView
    private val handler = Handler()

    private lateinit var textPublicInfo: TextView // Declare TextView



    private inner class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2 // Number of pages

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> Temp_Fragement()   // Ensure these fragment classes are defined
                0 -> Roller_Fragement()
                //2 -> ThirdFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

//        temperatureTextView = findViewById(R.id.textView)
//        textPublicInfo = findViewById(R.id.text_public_info)

        viewPager = findViewById(R.id.viewPager) // Ensure you have this in your XML layout
        val adapter = MyPagerAdapter(this)
        viewPager.adapter = adapter



//        fetchSmartHomeInformation()
//        fetchTemperature()

//        Toast.makeText(this, "Hello Waaorld!", Toast.LENGTH_LONG).show()

        // Start updating temperature every 60 seconds
//        handler.post(updateTemperatureRunnable)
    }

//    private val updateTemperatureRunnable = object : Runnable {
//        override fun run() {
//            // Update the TextView to display "Hello World!"
////            runOnUiThread {
////                temperatureTextView.text = "Hello Wossssrld!"
////            }
//            fetchTemperature()
//            handler.postDelayed(this, 60000) // 60 seconds
//
//            val viewPager: ViewPager2 = findViewById(R.id.viewPager)
//            viewPager.adapter = MyPagerAdapter(this@MainActivity)
//        }
//    }










    override fun onDestroy() {
        super.onDestroy()
//        handler.removeCallbacks(updateTemperatureRunnable) // Stop the updates
    }
}
