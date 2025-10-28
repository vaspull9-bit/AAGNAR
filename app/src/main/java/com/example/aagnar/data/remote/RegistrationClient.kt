package com.example.aagnar.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class RegistrationClient {

    suspend fun register(username: String, password: String, nickname: String): String {
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            try {
                println("🟢 [REG] Step 1: Creating socket...")
                socket = Socket()
                socket.soTimeout = 10000

                println("🟢 [REG] Step 2: Connecting...")
                socket.connect(InetSocketAddress("192.168.88.240", 8887), 10000)

                println("🟢 [REG] Step 3: Setting up streams...")
                val out = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                // ↓↓↓↓ ПРОПУСТИТЬ ПРИВЕТСТВЕННОЕ СООБЩЕНИЕ ↓↓↓↓
                println("🟢 [REG] Step 3.5: Reading welcome message...")
                val welcomeMessage = input.readLine()
                println("🟢 [REG] Welcome message: $welcomeMessage")
                // ↑↑↑↑ ПРОПУСТИТЬ ПРИВЕТСТВЕННОЕ СООБЩЕНИЕ ↑↑↑↑

                val request = """{"type":"register","username":"$username","password":"$password","nickname":"$nickname"}"""

                println("🟢 [REG] Step 4: Sending request: $request")
                out.println(request)
                out.flush()

                println("🟢 [REG] Step 5: Waiting for REAL response...")
                val response = input.readLine()  // ← это будет ответ на регистрацию
                println("🟢 [REG] Step 6: Received REAL response: $response")

                socket.close()
                return@withContext response ?: "{\"type\":\"error\",\"message\":\"No response\"}"

            } catch (e: Exception) {
                println("🔴 [REG] ERROR: ${e.javaClass.simpleName}: ${e.message}")
                socket?.close()
                return@withContext "{\"type\":\"error\",\"message\":\"${e.message}\"}"
            }
        }
    }
}