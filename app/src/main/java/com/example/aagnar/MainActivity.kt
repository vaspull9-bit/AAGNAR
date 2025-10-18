// AAGNAR v4.0.0
// Рекомендую: WebRTC P2P + Custom Signaling
//Давайте создадим простую P2P систему на основе WebRTC:
package com.example.aagnar  // ДОЛЖНО БЫТЬ ТАК

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
import com.example.aagnar.presentation.ui.p2p.P2PFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим элементы
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Устанавливаем название AAGNAR
        toolbarTitle.text = "AAGNAR"

        // Кнопка меню открывает боковое меню
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Кнопка "О программе"
        findViewById<ImageButton>(R.id.action_about).setOnClickListener {
            showAboutDialog()
        }

        // Навигация в боковом меню
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_p2p -> showFragment(P2PFragment(), "P2P Связь")
                R.id.nav_calls -> showFragment(CallFragment(), "Звонки")
                R.id.nav_contacts -> showFragment(ContactsFragment(), "Контакты")
                R.id.nav_settings -> showFragment(SettingsFragment(), "Настройки")
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Показываем главный экран при запуске
        showFragment(P2PFragment(), "P2P Связь")
    }

    private fun showFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        toolbarTitle.text = title
    }

    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("AAGNAR v4.0.0")
            .setMessage("Matrix клиент")
            .setPositiveButton("OK", null)
            .show()
    }
}