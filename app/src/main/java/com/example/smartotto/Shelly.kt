package com.example.smartotto

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


class Shelly() {

    private val baseUrl = "https://shelly-56-eu.shelly.cloud"
    private val authKey = "MTU0NDM0dWlk6FBF401F197824D278C27DBE39025D09D88C150D9C1CA07BCF4C0FF10F0DB4EA5737956ABCD83131"

    // OkHttpClient instance for making HTTP requests
    private val client = OkHttpClient()
    private val headers: Headers

    init {
        headers = get_headers()
    }

    private fun get_headers(): Headers {
        return Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", "*/*")
            .build()
    }

    // Separate function to get temperature
    fun get_temperature(deviceId: String, callback: (String?) -> Unit) {
        get_device_data(deviceId, "temperature:0") { temperature ->
            callback(temperature ?: "99.9")
        }
    }

    // Separate function to get humidity
    fun get_humidity(deviceId: String, callback: (String?) -> Unit) {
        get_device_data(deviceId, "humidity:0") { humidity ->
            callback(humidity ?: "99")
        }
    }

    // Separate function to get humidity
    fun get_roller_status(device_ip: String, callback: (String) -> Unit) {
        val url = "http://$device_ip/roller/0?status"
        try {
            makeGetRequest(url) { response ->
                val status = parse_json_by_key(response,"last_direction")
                callback(status)
            }
        } catch (e: Exception) {
            Log.e("shutter", "error at getting shutter status")
            callback("error")
        }
    }

    private fun parse_json_by_key(responseBody: String?, key: String): String {
        // Simple JSON parsing, assuming response is {"temperature": value}
        return responseBody?.let {
            val jsonObject = JSONObject(it)
            jsonObject.getString(key) // Adjust based on actual JSON structure
        } ?: "-?"
    }

    private fun get_device_data(deviceId: String, dataKey: String, callback: (String?) -> Unit) {
        get_device_status(deviceId) { deviceStatus ->
            if (deviceStatus != null) {
                // Parse the JSON string into a JSONObject
                val jsonObject = JSONObject(deviceStatus)

                // Navigate to the `data` and `device_status` objects
                val data = jsonObject.optJSONObject("data")
                val deviceStatusObj = data?.optJSONObject("device_status")
                val specificDataObj = deviceStatusObj?.optJSONObject(dataKey)

                // Retrieve the requested data based on the key
                val result = when (dataKey) {
                    "temperature:0" -> specificDataObj?.optDouble("tC")?.toString() ?: "99.9"
                    "humidity:0" -> specificDataObj?.optInt("rh")?.toString() ?: "99"
                    else -> null
                }

                // Pass the result to the callback
                callback(result)
            } else {
                Log.e("Shelly", "Failed to retrieve device data")
                callback(null)
            }
        }
    }

    private fun get_device_status(deviceId: String, callback: (String?) -> Unit) {
        val url = "$baseUrl/device/status"
        val formBody = get_formBody(deviceId)

        makePostRequest(url, formBody) { response ->
            callback(response)
        }
    }

    private fun exeRequest(client: OkHttpClient, request: Request, callback: (String?) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Ilja", "Test2")
                Log.e("FetchError", "Error !!!: ${e.message}")
                callback(null) // On failure, pass null to the callback
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null) // On failure, pass null to the callback
                        throw IOException("Unexpected code $response")
                    }

                    // Handle the response.body if gzipped
                    // Parse JSON response
                    val responseData = response.body?.string()
                    callback(responseData)
                }
            }

        })
    }

    private fun makeGetRequest(url: String, callback: (String?) -> Unit) {
        val getRequest = get_request(url, headers)

        // Handle the response inside the callback
        exeRequest(client, getRequest) { response ->
            if (response != null) {
                Log.d("Request", response)
                callback(response) // Pass the successful response to the caller
            } else {
                Log.e("Request", "Failed at makeGetRequest")
                callback(null) // Pass null in case of failure
            }
        }
    }


    private fun get_request(apiUrl: String, headers: Headers): Request {
        return Request.Builder()
            .url(apiUrl)
            .headers(headers)
            .get()
            .build()
    }
    // Function to make a POST request
    private fun makePostRequest(url: String, formBody: FormBody, callback: (String?) -> Unit) {
        val postRequest = get_post_request(url, formBody)

        // Handle the response inside the callback
        exeRequest(client, postRequest) { response ->
            if (response != null) {
                Log.d("Request", response)
                callback(response) // Pass the successful response to the caller
            } else {
                Log.e("Request", "Failed to fetch information")
                callback(null) // Pass null in case of failure
            }
        }
    }

    private fun get_post_request(url: String, formBody: FormBody): Request {
        return Request.Builder()
            .url(url)
            .post(formBody)
            .build()
    }

    private fun get_formBody(device_id: String? = null): FormBody {
        val formBody = FormBody.Builder()
            .add("auth_key", authKey)

        device_id?.let {
            formBody.add("id", it)
        }
        return formBody.build()
    }



}
