
// AAGNAR v4.0.3
//  WebRTC P2P + Custom Signaling

package com.example.aagnar

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.example.aagnar.presentation.ui.call.CallFragment
import com.example.aagnar.presentation.ui.contacts.ContactsFragment
import com.example.aagnar.presentation.ui.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.aagnar.R
import com.example.aagnar.presentation.ui.p2p.P2PSimpleFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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


        setContentView(R.layout.activity_main)

        showFragment(ContactsFragment())

        // Находим элементы
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Кнопка меню справа открывает боковое меню
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Навигация в боковом меню
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_p2p -> showFragment(P2PSimpleFragment())
                R.id.nav_calls -> showFragment(CallFragment())
                R.id.nav_contacts -> showFragment(ContactsFragment())
                R.id.nav_settings -> showFragment(SettingsFragment())
                R.id.nav_about -> showAboutDialog()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Показываем главный экран при запуске
        showFragment(P2PSimpleFragment())
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("AAGNAR v4.0.3")
            .setMessage("P2P клиент, DeeR Tuund (C) 2025")
            .setPositiveButton("OK", null)
            .show()
    }
}