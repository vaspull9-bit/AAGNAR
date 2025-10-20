package com.example.aagnar.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.aagnar.presentation.ui.chats.ChatsFragment
import com.example.aagnar.presentation.ui.contacts.ContactsFragment
import com.example.aagnar.presentation.ui.calls.CallsFragment

class MainPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatsFragment()
            1 -> ContactsFragment()
            2 -> CallsFragment()
            else -> ChatsFragment()
        }
    }
}