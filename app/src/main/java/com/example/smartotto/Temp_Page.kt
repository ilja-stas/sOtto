package com.example.smartotto

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartotto.TemperatureControlView
import com.example.smartotto.Bosch
import com.example.smartotto.Shelly
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import kotlinx.coroutines.*
import com.example.smartotto.Room
import com.example.smartotto.loadRooms

class Temp_Fragement : Fragment() {


    private lateinit var tempControlView: TemperatureControlView
    private val bosch = Bosch()
    private val shelly = Shelly()
//    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var currentTemperature: Double = 20.0 // Initial temperature
    private var updateJob: Job? = null

    private lateinit var rooms: Map<String, Room>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.temp_page, container, false) // Create a layout file for this fragment
        tempControlView = view.findViewById(R.id.temperatureControlView)

        val temperature_layouts = tempControlView.get_temperature_layouts()
        rooms = loadRooms(requireContext())

        Log.d("asd", "aaaaaaaaaaaa")

        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchData_and_updateUI(temperature_layouts)
                delay(3 * 1000) // 30-second delay between updates
            }
        }
//        fetchData_and_updateUI(temperature_layouts)



        return view

    }


    // Example function for network call
    fun fetchData_and_updateUI(temperature_layouts: MutableMap<String,LinearLayout>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Perform network call on IO dispatcher
                networkRequest(temperature_layouts) // set Data into class-var-"rooms"
                Log.i("temp_page", "after networkRequest")


                withContext(Dispatchers.Main) {
                    updateUI(temperature_layouts) // use Data from class-var-"rooms"
                }
                Log.i("temp_page", "after updateUI")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
//                    showError(e.message) // Handle errors on the main thread
                }
            }
        }
    }

    private fun networkRequest(temperature_layouts: MutableMap<String,LinearLayout>) {
        Log.i("temp_page", "start networkRequest")
        rooms.forEach { (roomKey, room) ->
            Log.i("networkRequest", "$roomKey: ${room.name}, ID: ${room.thermostat?.device_id ?: "No ID"}")

            val layout = temperature_layouts[roomKey]

            bosch_network_request(layout, room)

            // Humidity from shelly ht
            if (room.climate_sensor?.device_id != null) {
                shelly.get_humidity(room.climate_sensor.device_id) { humidity ->
                    room.humidity = humidity ?: "99"
                }
            }

            // Temperature from shelly ht
            if (room.climate_sensor?.device_id != null) {
                shelly.get_temperature(room.climate_sensor.device_id) { temperature ->
                    room.temperature = temperature ?: "99"
                }
            }
            Log.d("temp_page", "Updating room: $roomKey with data: $room")
        }
    }

    private fun bosch_network_request(layout:LinearLayout?, room:Room) {
        var desired_temp_text = layout?.findViewWithTag<TemperatureTextView>("desired_temp_text")
        var update_in_app = desired_temp_text?.update_in_app ?: false
        val parentDeviceId = room.thermostat?.parentDeviceId ?: ""
        if (parentDeviceId == "") {
            return
        }

        if (update_in_app) {
            // Set Desired Temperature
            desired_temp_text?.update_in_app = false
            val des_temp = desired_temp_text?.temperature?.toDoubleOrNull() ?: 17.0 // if error then default 17°C
            Log.e("Temperature Check", "des_temp: $des_temp")  // Log to verify value
            room.desired_temperature = des_temp.toString()
            bosch.set_desired_temperature(parentDeviceId, des_temp)
        } else {
            // Read Desired Temperature
            bosch.get_desired_temperature(parentDeviceId) { temperature ->
                room.desired_temperature = temperature
            }

            // Actual Temperature from bosch thermostat
            if (room.thermostat?.device_id != null) {
                bosch.get_temperature(room.thermostat.device_id) { temperature ->
                    room.temperature = temperature ?: "?"
                }
            }
        }
    }

    private fun updateUI(temperature_layouts: MutableMap<String,LinearLayout>) {
        rooms.forEach { (roomKey, room) ->
            Log.i("temp_page", "assssssssssssss")
            val layout = temperature_layouts[roomKey]
            val roomTextView = layout?.findViewWithTag<TextView>("room_text")
            roomTextView?.text = room.name

            //--------------- HUMIDITY ---------------
            val humidityTextView = layout?.findViewWithTag<TextView>("humidity_text")
            val humidityImage = layout?.findViewWithTag<ImageView>("humidityImage")
            if (room.humidity != null) {
                Log.i("temp_page", "room.climate_sensor.humidity: ${room.humidity} ($roomKey: ${room.name})")
                humidityTextView?.text = room.humidity
                humidityTextView?.visibility = View.VISIBLE
                humidityImage?.visibility = View.VISIBLE
            }
            else {
                Log.i("temp_page", "hide humidity in room $roomKey: ${room.name}")
                humidityTextView?.visibility = View.INVISIBLE
                humidityImage?.visibility = View.INVISIBLE
            }

            //---------------TEMPERATURE ---------------
            val actualTemperatureView = layout?.findViewWithTag<TextView>("room_temperature")
            val thermoImage = layout?.findViewWithTag<ImageView>("thermoImage")
            if (room.temperature != null) {
                actualTemperatureView?.text = room.temperature
                actualTemperatureView?.visibility = View.VISIBLE
                thermoImage?.visibility = View.VISIBLE
            }
            else {
                Log.i("temp_page", "hide temp in room $roomKey: ${room.name}")
                actualTemperatureView?.visibility = View.INVISIBLE
                thermoImage?.visibility = View.INVISIBLE
            }

            //--------------- DESIRED TEMPERATURE ---------------
            val desired_temp_text = layout?.findViewWithTag<TemperatureTextView>("desired_temp_text")
            val increaseTempImage = layout?.findViewWithTag<ImageView>("increase_temp_button")
            val decreaseTempImage = layout?.findViewWithTag<ImageView>("decrease_temp_button")

            if (room.desired_temperature != null) {
                desired_temp_text?.text = room.desired_temperature
                desired_temp_text?.visibility = View.VISIBLE
                increaseTempImage?.visibility = View.VISIBLE
                decreaseTempImage?.visibility = View.VISIBLE
            }
            else {
                Log.i("temp_page", "hide hdesired_temperature")
                desired_temp_text?.visibility = View.INVISIBLE
                increaseTempImage?.visibility = View.INVISIBLE
                decreaseTempImage?.visibility = View.INVISIBLE
            }
        }
    }

//    private val updateJob = CoroutineScope(Dispatchers.IO).launch {
//        while (isActive) {
//            update_rooms(rooms, temperature_layouts, false)
//            delay(10000)  // 10-second delay between updates
//        }
//    }
    private fun update_rooms(
        rooms: Map<String, Room>,
        temperature_layouts: MutableMap<String,LinearLayout>,
        is_init: Boolean = false
    ) {
        rooms.forEach { (roomKey, room) ->
            Log.i("add", "$roomKey: ${room.name}, ID: ${room.thermostat?.device_id ?: "No ID"}")

            val layout = temperature_layouts[roomKey]
            val roomTextView = layout?.findViewWithTag<TextView>("room_text")
            roomTextView?.text = room.name

            // Humidity from shelly ht
            if (room.climate_sensor?.device_id != null) {
                shelly.get_humidity(room.climate_sensor.device_id) { humidity ->
                    room.humidity = humidity ?: "99"
                }
            }

//            // Actual Temperature from shelly ht
//            if (room.climate_sensor?.device_id != null) {
//                val deviceId = room.climate_sensor.device_id
//                val tempTextView = layout?.findViewWithTag<TextView>("room_temperature")
//                show_current_temperature(deviceId, shelly::get_temperature, tempTextView)
//            }

            // Actual Temperature from bosch thermostat
            if (room.thermostat?.device_id != null) {
                val deviceId = room.thermostat.device_id
                val tempTextView = layout?.findViewWithTag<TextView>("room_temperature")
                show_current_temperature(deviceId, bosch::get_temperature, tempTextView)
            }
            if (room.thermostat?.parentDeviceId != null) {
                // Desired Temperature
                val desired_temp_text = layout?.findViewWithTag<TextView>("desired_temp_text")
//                val update_state = tempControlView.get_update_temp_state()
//                Log.e("set_temp", "state: $update_state")

//                if (update_state) {
//                    set_desired_temperature(room, desired_temp_text)
//                    tempControlView.set_update_temp_state(false)
//                } else {
//                    show_desired_temperature(room, desired_temp_text)
//                }

            }
            Log.d("temp_page", "Updating room: $roomKey with data: $room")
        }


    }

    private fun show_humidity(
        device_id: String,
        get_humidity_fun: (String, (String?) -> Unit) -> Unit,
        textView: TextView?
    )  {
        get_humidity_fun(device_id) { response ->
            if (response != null) {
                Log.i("temp_page", "Temperature: $response")

                textView?.text = response ?: "tbd"
            } else {
                Log.i("temp_page", "Failed to get temperature")
                textView?.text = "-"
            }
        }

    }

    private fun set_desired_temperature(room: Room?, textView: TextView?) {
        room?.thermostat?.parentDeviceId?.let { parentDeviceId ->
            val temperature = textView?.text?.toString()?.toDoubleOrNull()
            if (temperature != null) {
                bosch.set_desired_temperature(parentDeviceId, temperature)
            } else {
                Log.e("temp_page", "Invalid temperature value in TextView")
            }
        }
    }

    private fun show_desired_temperature(room: Room?, textView: TextView?) {
        room?.thermostat?.parentDeviceId?.let { parentDeviceId ->
            bosch.get_desired_temperature(parentDeviceId) { response ->
                if (response != null) {
                    Log.i("temp_page", "Temperature: $response")

                    textView?.text = response ?: "-"
                } else {
                    Log.i("temp_page", "Failed to get temperature")
                    textView?.text = "-" ?: "-"
                }
            }
        }
    }

    private fun show_current_temperature(
        device_id: String,
        get_temperature: (String, (String?) -> Unit) -> Unit,
        textView: TextView?
    ) {
        get_temperature(device_id) { response ->
            if (response != null) {
                Log.i("temp_page", "Temperature: $response")

                textView?.text = response ?: "tbd"
            } else {
                Log.i("temp_page", "Failed to get temperature")
                textView?.text = "-"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove callbacks to avoid memory leaks
//        handler.removeCallbacks(runnable)
        updateJob?.cancel()
    }
}
