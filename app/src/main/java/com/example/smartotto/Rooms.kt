package com.example.smartotto

import android.content.Context
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import android.util.Log

data class Room(
    val name: String,
    val thermostat: Thermostat? = null,
    val climate_sensor: Climate_Sensor? = null,
    val roller_shutter: Map<String, Roller_Shutter>? = null,
    var temperature: String? = null,
    var humidity: String? = null,

    var desired_temperature: String? = null,
)

data class Roller_Shutter(
    val company: String,
    val device_id: String? = null,
    val ip: String? = null,
    var state: String = "close",
)

data class Thermostat(
    val company: String,
    val device_id: String? = null,
    val parentDeviceId: String? = null,
    var desired_temperature: String = "00.7",
    var actual_temperature: String = "00.7",
//    update_desired_temp: Boolean = false,
    var battery_state: String = "tbd"
)

data class Climate_Sensor (
    val company: String,
    val device_id: String? = null,
    var battery_state: String? = null,
//    var connection_quality
//    var temperature: String? = null,
//    var humidity: String? = null
)

fun loadRooms(context: Context): Map<String, Room> {
    val yaml = Yaml()

    // Load the YAML file as a map of maps
    val inputStream: InputStream = context.assets.open("rooms.yaml")

    // Parse the YAML into a map
    val roomsMap: Map<String, Map<String, Any>> = yaml.load(inputStream)

    // Convert the nested maps into a Map<String, Room>
    return roomsMap.mapValues { (_, roomData) ->
        val thermostatData = roomData["thermostat"] as? Map<String, Any>
        val climate_sensorData = roomData["climate_sensor"] as? Map<String, Any>
        val rollerShutterData = roomData["roller"] as? Map<String, Map<String, Any>>
        Log.e("rooms", "sdasd")

        Room(
            name = roomData["name"] as String,
            thermostat = thermostatData?.let {
                Thermostat(
                    company = it["company"] as String,
                    device_id = it["device_id"] as? String ?: it["id"] as? String,
                    parentDeviceId = it["parentDeviceId"] as? String
                )
            },
            climate_sensor = climate_sensorData?.let {
                Climate_Sensor(
                    company = it["company"] as String,
                    device_id = it["device_id"] as? String ?: it["id"] as? String
                )
            },
            roller_shutter = rollerShutterData?.mapValues { (_, shutterData) ->
                Roller_Shutter(
                    company = shutterData["company"] as String,
                    device_id = shutterData["device_id"] as? String,
                    ip = shutterData["ip"] as? String
                )
            }
        )

    }
}