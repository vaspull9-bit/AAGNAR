package com.example.aagnar.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class RegistrationClient {

    suspend fun register(username: String, password: String, nickname: String): String {
        return withContext(Dispatchers.IO) {
            try {
                println("🟢 [REG] Step 1: Creating socket...")
                val socket = Socket("192.168.88.240", 8888)
                println("🟢 [REG] Step 2: Socket created, setting up streams...")

                val out = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                // УБИРАЕМ ПЕРЕНОСЫ СТРОК - одна строка!
                val request = """{"type":"register","username":"$username","password":"$password","nickname":"$nickname"}"""

                println("🟢 [REG] Step 3: Sending request: $request")
                out.println(request)

                println("🟢 [REG] Step 4: Waiting for response...")
                val response = input.readLine()
                println("🟢 [REG] Step 5: Received response: $response")

                socket.close()
                println("🟢 [REG] Step 6: Socket closed")

                return@withContext response
            } catch (e: Exception) {
                println("🔴 [REG] ERROR: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                return@withContext "{\"type\":\"error\",\"message\":\"${e.message}\"}"
            }
        }
    }
}