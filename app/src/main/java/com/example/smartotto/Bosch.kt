package com.example.smartotto

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.buffer
import okio.source
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

class Bosch {

//    private val apiUrl = "https://192.168.0.10:8444/smarthome/devices/hdm:ZigBee:30e8e40000d289fa/services/TemperatureLevel/state"
    private val host = "192.168.0.10"
    private val apiUrl = "https://$host:8444/smarthome"
    private val apiVersion = "3.13"
    private val systemPasswordBase64 = Base64.getEncoder().encodeToString("testtest".toByteArray())


    // Define your certificate and key as strings
    private val certificateString = """
        -----BEGIN CERTIFICATE-----
        MIIDbTCCAlWgAwIBAgIUW91Wr5xm9idKPgnFc/IBL8B7C4swDQYJKoZIhvcNAQEL
        BQAwRTELMAkGA1UEBhMCRGUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM
        GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAgFw0yNDEwMTYwODM2NTBaGA8yMDUy
        MDMwMjA4MzY1MFowRTELMAkGA1UEBhMCRGUxEzARBgNVBAgMClNvbWUtU3RhdGUx
        ITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcN
        AQEBBQADggEPADCCAQoCggEBAK5pb1Yjp79hi+dSA/ODkOk8MmSucXOEUtumxo71
        8A3/D/AOqYSoevDAit7FbNd+1BTB8t81orXhs3TwoBrVKBRvyuvCsS2hKjLicpcA
        oVl3haKmjRzPIcnpKTAu6i7XN3MmSZBCxq97wX414W5P5uVnoXH4YB6T5M8WJewi
        wONFFHfmAqluP6QvrUPv0YpbgdIN1lqzEY/qz/fgqvIh80qfDrN9rLWc1jf0ysnq
        eIkTK4KQagR8g5nRjA7KGA/AIXhq0oXqP+atpwKkqlglfZmYLppC0oSm5JZUze6P
        LM9a23/vAw6VL5soIThUT7YTaNJ7rPolbwiG2pt5k7b55+cCAwEAAaNTMFEwHQYD
        VR0OBBYEFHDE5uWM4w9JwUY8aBJdJlO432UsMB8GA1UdIwQYMBaAFHDE5uWM4w9J
        wUY8aBJdJlO432UsMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEB
        ADblWqPzarHKVACog3OBtc2/FGM9cKfvdK5OklocyFjN3dphLhbxhxom/ad/FJt3
        gISzxFWhA+27vfgbJ0ig+Iw7QZU7J8xAZ2jyDcvtupdtrih/u6jcjSxnmFQNkVRH
        lMZtBdEiRG4xgJzNlDn7RRgHAeg8tsKJXMw/hZGXh9TeE22WZQ0cErhv3utwNph9
        qvmUltlWAqJB5ZzkOWPd5IGp00Zd4+HiPsJO+je41AGeIFbHpqX+owapooiiSiZZ
        zToSojfoB60CxwJndDE2A7uq6prC78cjtlkr9DGlLCfGY1mmNYNhZH19rfRICSlM
        x3tIp3So0hFg23nV1GZQe3A=
        -----END CERTIFICATE-----
""".trimIndent()

    private val privateKeyString = """
        -----BEGIN PRIVATE KEY-----
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCuaW9WI6e/YYvn
        UgPzg5DpPDJkrnFzhFLbpsaO9fAN/w/wDqmEqHrwwIrexWzXftQUwfLfNaK14bN0
        8KAa1SgUb8rrwrEtoSoy4nKXAKFZd4Wipo0czyHJ6SkwLuou1zdzJkmQQsave8F+
        NeFuT+blZ6Fx+GAek+TPFiXsIsDjRRR35gKpbj+kL61D79GKW4HSDdZasxGP6s/3
        4KryIfNKnw6zfay1nNY39MrJ6niJEyuCkGoEfIOZ0YwOyhgPwCF4atKF6j/mracC
        pKpYJX2ZmC6aQtKEpuSWVM3ujyzPWtt/7wMOlS+bKCE4VE+2E2jSe6z6JW8Ihtqb
        eZO2+efnAgMBAAECggEAAPCSBZ+Zk+9UxLcwmxjTGjsuPWPrmCO2c63ZiRJUghq3
        RpBI7Qdpm7c6HJFZsDgAyvVZa78irJLq60dE6ab0tg1v8azIFj2ttCvE7vaOGaJW
        DfEqA5SsNRD15m8PqmLsaQUlKz4cyHQbGGkAqjMOCIW4t0iKDpK7hn8OnWTSgJqQ
        ZDpvvKS03L2JSCK/1xDnch+KcN7oWia2srXWvra/8cpaHWX4Suk1aZhz33vrg1gG
        wbO7iV9hv0qF6PLEDuiHghi290daqNsY4R0IIV4MTuhFD6IePuoJqRePdjRvb+Wc
        kUuN3cQkxu9+9U7W22sqhtjB+Epm8hxDvOBcQYbh7QKBgQDdhxlzky/wjyrzLGO0
        Uz57eDpsvqCOkTwAkG4uySLm86mJg7oy/K3plYiIImu770reJyTWfviXoiavsh45
        PHpQqCCX6XttrwFvK7tlrSVjXoQ3FpZvq8uGRRzixV7leojrhPgkYco74OkHHQkE
        WpanmulTN8VpZc8Ou1QpTHR08wKBgQDJjWbIjdjnTN0lxmpgJqlHLhPlmAv5JXz9
        6OsheDcoD0CDeyybfadtVcEEbt/omKqWIEpMVJvL2MGB8ALbKbJVwa41fx7FKdwg
        MQV4KbxJF04TvZYeJIfFbBFK9k6vJceAsldFhfguJkcrKPx6J1afMVvFB2pGLA99
        GvvENAhOPQKBgBE7cyVka7RAzsx/xXm/h/q+zXysD31Hmu1ITungx5BDoAqMbq2M
        rlKfSTGdy9egs6g4T0tIMTpKaMv+BQ/avoELsw1eIjXUCT6Gnls4btIeBhbq8OTa
        7kgRlD+nrswXDhOUEmFyIfMqebYN8ieejF2ZVVEd44NhYgv5UoCr69s1AoGBAMCq
        4u4kaovw9hX+LgHr8zWP3oVDa26nX2x5EwvTlyY8LjeBr4qT+1CRAQnz0ybNiYQp
        Kdz9pFCgy5oijRhkK42+r4GEGy3ubfZ35fqQKuTT2lcUjEQUN5WJUJ6QBXd4bdWg
        pbxX7H5xKOchLvi2Uh+rV0ZpaDXcOJInETTlaGshAoGACEew0hlKsK/Y6iPtTaF2
        n4n8jE+fNHS5aZ5TcrzeNVAtxR1iP+aGXTItf7a2pcAwQ1rD5g2j73nqKiZUvCU5
        cOFEqDvnflFXR/mVi20Y5e7zKa6Sa+IFaNHiTmPcIkAtM7i6ihnpRI81NZxyOmaM
        EhDGKfq7Icln3Yp5YyDQsHg=
        -----END PRIVATE KEY-----
    """.trimIndent()

    private val client: OkHttpClient
    private val headers: Headers

    init {
        client = get_client()
        headers = get_headers()
    }

    private fun get_client(): OkHttpClient {
        val sslContext = createSSLContext(certificateString, privateKeyString)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager())
            .hostnameVerifier(HostnameVerifier { _, _ -> true }) // Disable hostname verification (be cautious with this in production)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun get_headers(): Headers {
        return Headers.Builder()
            .add("Content-Type", "application/json")
//            .add("User-Agent", "PostmanRuntime/7.42.0")
            .add("Accept", "*/*")
            .add("Accept-Encoding", "gzip, deflate, br")
            .add("Connection", "keep-alive")
//            .add("Accept", "application/json")
            .add("Systempassword", systemPasswordBase64)
            .add("api-version", apiVersion)
//            .add("Host", "192.168.0.10:8444")
            .build()

    }

    private fun get_request(apiUrl: String, headers: Headers): Request {
        return Request.Builder()
            .url(apiUrl)
            .headers(headers)
            .get()
            .build()
    }

    private fun put_request(url: String, headers: Headers, jsonBody: String): Request {
        return Request.Builder()
            .url(url)
            .headers(headers)
            .put(RequestBody.create("application/json".toMediaType(), jsonBody)) // Use the updated method
            .build()
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
                    val contentEncoding = response.header("Content-Encoding")
                    val responseBodyText = if (contentEncoding != null && contentEncoding.equals("gzip", ignoreCase = true)) {
                        decompressGzip(response.body) // Decompress if gzip encoded
                    } else {
                        response.body?.string() ?: ""
                    }

                    callback(responseBodyText)
                }
            }

        })
    }

    // Function to make a PUT request
    private fun makePutRequest(url: String, jsonBody: String, callback: (String?) -> Unit) {
        val putRequest = put_request(url, headers, jsonBody)

        // Handle the response inside the callback
        exeRequest(client, putRequest) { response ->
            if (response != null) {
                Log.d("Request", response)
                callback(response) // Pass the successful response to the caller
            } else {
                Log.e("Request", "Failed at makePutRequest")
                callback(null) // Pass null in case of failure
            }
        }
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

    fun get_public_info(callback: (String?) -> Unit) {
        val url = apiUrl + "/public/information"
        makeGetRequest(url, callback)
    }

    fun set_desired_temperature(deviceId: String, temperature: Double) {
        val url = "https://192.168.0.10:8444/smarthome/devices/$deviceId/services/RoomClimateControl/state"
        val jsonBody = """
                        {
                          "@type": "climateControlState",
                          "setpointTemperature": $temperature
                        }
                        """.trimIndent()

        makePutRequest(url, jsonBody) { response ->
            Log.d("asd", "$response")
        }
    }

    fun get_temperature(device: String, callback: (String?) -> Unit) {
        val url = "https://192.168.0.10:8444/smarthome/devices/$device/services/TemperatureLevel/state"
        makeGetRequest(url) { response ->
            val temperature = parseTemperature(response, "temperature")
            callback(temperature)
        }
    }

    fun get_desired_temperature(device: String, callback: (String?) -> Unit) {
        val url = "https://192.168.0.10:8444/smarthome/devices/$device/services/RoomClimateControl/state"
        makeGetRequest(url) { response ->
            Log.e("asd", "$device ====> $response")
            val temperature = parseTemperature(response, "setpointTemperature")
            callback(temperature)
        }
    }


    private fun parseTemperature(responseBody: String?, key: String): String {
        // Simple JSON parsing, assuming response is {"temperature": value}
        return responseBody?.let {
            val jsonObject = JSONObject(it)
            jsonObject.getString(key) // Adjust based on actual JSON structure
        } ?: "-?"
    }

    private fun decompressGzip(responseBody: ResponseBody?): String {
        val source = responseBody?.byteStream()?.source()?.buffer()
        val gzipStream = GZIPInputStream(source?.inputStream())
        return gzipStream.reader().use { it.readText() }
    }




    private fun createSSLContext(certString: String, keyString: String): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        // Load the client certificate from the string
        return try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificate = certificateFactory.generateCertificate(ByteArrayInputStream(certString.toByteArray())) as X509Certificate

            // Load the private key from the string
            val privateKey = loadPrivateKey(keyString)

            // Add both the certificate and the private key to the KeyStore
            keyStore.setKeyEntry("client", privateKey, null, arrayOf(certificate))

            keyManagerFactory.init(keyStore, null)
            sslContext.init(keyManagerFactory.keyManagers, arrayOf(trustManager()), null)
            sslContext
        } catch (e: Exception) {
            e.printStackTrace() // Print the stack trace for debugging
            throw e // Re-throw the exception after logging
        }
    }


    private fun loadPrivateKey(keyString: String): PrivateKey {
        val keyBytes = keyString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedKey = Base64.getDecoder().decode(keyBytes) // Decode the Base64 encoded string
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }



    private fun trustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun trustAllCerts(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun createTrustAllSslContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustAllCerts()), java.security.SecureRandom())
        return sslContext
    }
}