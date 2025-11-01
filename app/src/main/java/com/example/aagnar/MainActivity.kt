//AAGNAR v4.3.1 -
// Стабильная версия с тестовым чатом
// С версии 4.3.0 начинается тестирование WEBRTC 01/11/2025
package com.example.aagnar

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.presentation.adapter.MainPagerAdapter
import com.example.aagnar.presentation.ui.settings.SettingsFragment
import com.example.aagnar.util.PerformanceMonitor
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.net.Socket
import java.net.InetSocketAddress
import android.content.Context  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
import com.example.aagnar.presentation.ui.chat.ChatActivity
import com.example.aagnar.presentation.ui.contacts.AddContactDialog

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: androidx.viewpager2.widget.ViewPager2
    private lateinit var toolbar: Toolbar
    private lateinit var menuButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var addContactButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 ПРОВЕРКА СЕССИИ - ДОБАВИТЬ ЭТИ 6 СТРОК
        val sessionPrefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        if (!sessionPrefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, AccountSelectionActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        initViews()
        setupViewPager()
        setupNavigation()
        checkServerStatus() // ← ДОБАВИТЬ проверку статуса серверов
    }

    private fun checkServerStatus() {
        lifecycleScope.launch {
            try {
                val webSocketOk = checkWebSocketServer()
                val tcpOk = checkTcpServer()

                updateStatusIndicators(webSocketOk, tcpOk)

                // Автоматическая регистрация если серверы доступны
                if (webSocketOk && tcpOk) {
                    autoRegisterIfNeeded()
                }
            } catch (e: Exception) {
                updateStatusIndicators(false, false)
            }
        }
    }

    private suspend fun checkWebSocketServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("ws://192.168.88.240:8889")
                    .build()
                val webSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {})
                delay(1000)
                webSocket.close(1000, null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun checkTcpServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("192.168.88.240", 8887), 3000)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun updateStatusIndicators(webSocketOk: Boolean, tcpOk: Boolean) {
        runOnUiThread {
            // Временное решение - Toast, потом заменим на иконки в тулбаре
            val status = "WS: ${if (webSocketOk) "🟢" else "🔴"}, TCP: ${if (tcpOk) "🟢" else "🔴"}"
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }
    }

    private fun autoRegisterIfNeeded() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        if (!prefs.getBoolean("registered", false)) {
            lifecycleScope.launch {
                try {
                    val registrationClient = com.example.aagnar.data.remote.RegistrationClient()
                    val username = prefs.getString("username", "user_${System.currentTimeMillis()}") ?: "user_${System.currentTimeMillis()}"
                    val result = registrationClient.register(username, "auto_password", username)

                    if (result.contains("success")) {
                        prefs.edit().putBoolean("registered", true).apply()
                        Toast.makeText(this@MainActivity, "✅ Авторегистрация выполнена", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Авторегистрация не обязательна - приложение работает и так
                }
            }
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        toolbar = findViewById(R.id.toolbar)
        menuButton = findViewById(R.id.menuButton)
        searchButton = findViewById(R.id.searchButton)
        addContactButton = findViewById(R.id.addContactButton)

        setSupportActionBar(toolbar)
    }

    private fun setupViewPager() {
        val pagerAdapter = MainPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Чаты"
                1 -> "Контакты"
                2 -> "Звонки"
                else -> "Чаты"
            }
        }.attach()

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        searchButton.setOnClickListener {
            showMessage("Поиск")
        }

        addContactButton.setOnClickListener {

            showAddContactDialog()
        }
    }

    // ДОБАВЛЯЕМ МЕТОД ДИАЛОГА:
    private fun showAddContactDialog() {
        val dialog = AddContactDialog()
        dialog.show(supportFragmentManager, "AddContactDialog")
    }
    private fun setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> showFragment(SettingsFragment())
            R.id.nav_profile -> showProfile()
            R.id.nav_about -> showAbout()
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showFragment(fragment: Fragment) {
        // Скрываем ViewPager и показываем FragmentContainer
        viewPager.visibility = View.GONE

        val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragment_container)
        fragmentContainer.visibility = View.VISIBLE

        // Правильное изменение layout params
        val layoutParams = fragmentContainer.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        layoutParams.weight = 1f
        fragmentContainer.layoutParams = layoutParams

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (isFragmentVisible()) {
            // Если открыт фрагмент, возвращаемся к ViewPager
            returnToViewPager()
        } else {
            super.onBackPressed()
        }
    }

    private fun isFragmentVisible(): Boolean {
        val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragment_container)
        return fragmentContainer.visibility == View.VISIBLE
    }

    private fun returnToViewPager() {
        // Показываем ViewPager и скрываем FragmentContainer
        viewPager.visibility = View.VISIBLE

        val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragment_container)
        fragmentContainer.visibility = View.GONE

        // Правильное изменение layout params
        val layoutParams = fragmentContainer.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = 0
        layoutParams.weight = 0f
        fragmentContainer.layoutParams = layoutParams

        // Очищаем back stack
        supportFragmentManager.popBackStack()
    }

    private fun showProfile() {
        showMessage("Профиль")
    }

    private fun showAbout() {
        android.app.AlertDialog.Builder(this)
            .setTitle("AAGNAR v4.3.1")
            .setMessage("P2P клиент, DeeR Tuund (C) 2025\n\nWEBRTC GNU GPL-3.0-")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun logout() {
        val sessionPrefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        sessionPrefs.edit().putBoolean("logged_in", false).apply()

        // НЕ очищаем данные аккаунта!
        startActivity(Intent(this, AccountSelectionActivity::class.java))
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}