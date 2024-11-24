package com.example.smartotto

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.smartotto.Room
import com.example.smartotto.loadRooms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Roller-Shutter muss status gelesen werden zyklisch, damit man die Position kennt

class Roller_Fragement : Fragment() {
    private lateinit var rollerShutterView: RollerShutterView
    private lateinit var rooms: Map<String, Room>
    private var updateJob: Job? = null
    private val shelly = Shelly()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.roller_page, container, false) // Create a layout file for this fragment
        rollerShutterView = view.findViewById(R.id.rollerShutterView)



        val roller_layouts = rollerShutterView.get_roller_layouts()
        rooms = loadRooms(requireContext())

        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchData_and_updateUI(roller_layouts)
                delay(3 * 1000) // 30-second delay between updates
            }
        }

        updateUI(roller_layouts)

        return view
    }

    private fun networkRequest(roller_layouts: MutableMap<String,LinearLayout>) {
        Log.i("temp_page", "start networkRequest")
        rooms.forEach { (roomKey, room) ->
            Log.i("shutter", "$roomKey: ${room.name}, ID: ${room.thermostat?.device_id ?: "No ID"}")

//            val layout = roller_layouts[roomKey]

            val leftShutter = room.roller_shutter?.get("left")
            val rightShutter = room.roller_shutter?.get("left")

            Log.w("shutter", "ip left ${room.name}: {$leftShutter.ip}")

            // Left Shutter
            if (leftShutter != null) {
                val device_ip = leftShutter.ip
                if (!device_ip.isNullOrEmpty()) {
                    shelly.get_roller_status(device_ip) { repsonse ->
                        Log.e("shutter", repsonse)
                    }
                }

            }

//            // Right Shutter
//            if (rightShutter!= null) {
//                shelly.get_roller_status(rightShutter?.ip) { repsonse ->
//                    Log.e("shutter", repsonse)
//                }
//            }
        }
    }

    fun fetchData_and_updateUI(roller_layouts: MutableMap<String,LinearLayout>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Perform network call on IO dispatcher
                networkRequest(roller_layouts) // set Data into class-var-"rooms"
                Log.i("temp_page", "after networkRequest")


                withContext(Dispatchers.Main) {
                    updateUI(roller_layouts) // use Data from class-var-"rooms"
                }
                Log.i("temp_page", "after updateUI")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
//                    showError(e.message) // Handle errors on the main thread
                }
            }
        }
    }

    private fun updateUI(layouts: MutableMap<String, LinearLayout>) {
        rooms.forEach { (roomKey, room) ->
            val layout = layouts[roomKey]
            val roomTextView = layout?.findViewWithTag<TextView>("room_text")
            roomTextView?.text = room.name

            // Left Roller


        }
    }
}
