package com.egamerica.rollergrilltracker.ui.holdtime

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.egamerica.rollergrilltracker.data.entities.GrillConfig

class GrillHoldTimePagerAdapter(
    fragmentActivity: FragmentActivity,
    private val grillConfigs: List<GrillConfig>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = grillConfigs.size

    override fun createFragment(position: Int): Fragment {
        val grillConfig = grillConfigs[position]
        return GrillHoldTimePageFragment.newInstance(grillConfig.grillNumber, grillConfig.grillName)
    }

    fun getGrillName(position: Int): String {
        return if (position < grillConfigs.size) {
            grillConfigs[position].grillName
        } else {
            ""
        }
    }
}