// AAGNAR v4.0.6
//

package com.example.aagnar

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.aagnar.databinding.ActivityMainBinding
import com.example.aagnar.presentation.adapter.MainPagerAdapter
import com.example.aagnar.presentation.ui.chats.ChatsFragment
import com.example.aagnar.presentation.ui.contacts.ContactsFragment
import com.example.aagnar.presentation.ui.calls.CallsFragment
import com.example.aagnar.presentation.ui.settings.SettingsFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем зарегистрирован ли пользователь
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        if (!prefs.getBoolean("registered", false)) {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupViewPager()
        setupNavigation()
    }

    private fun initViews() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        // Настройка toolbar
        setSupportActionBar(binding.toolbar)
    }

    private fun setupViewPager() {
        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Чаты"
                1 -> "Контакты"
                2 -> "Звонки"
                else -> "Чаты"
            }
        }.attach()

        // Обработчики кнопок в toolbar
        binding.menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.searchButton.setOnClickListener {
            // TODO: Открыть поиск
            showMessage("Поиск")
        }

        binding.addContactButton.setOnClickListener {
            // TODO: Открыть добавление контакта
            showMessage("Добавить контакт")
        }
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showProfile() {
        // TODO: Открыть профиль пользователя
        showMessage("Профиль")
    }

    private fun showAbout() {
        android.app.AlertDialog.Builder(this)
            .setTitle("AAGNAR v4.0.6")
            .setMessage("P2P клиент, DeeR Tuund (C) 2025\n\nОсновано на JAMI")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun logout() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, RegistrationActivity::class.java))
        finish()
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}