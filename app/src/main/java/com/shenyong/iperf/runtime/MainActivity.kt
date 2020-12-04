package com.shenyong.iperf.runtime

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class MainActivity : AppCompatActivity() {

    private val cmdFragment = CmdFragment.newInstance()
    private val jniFragment = JniFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MainPagerAdapter(supportFragmentManager)
        pager.adapter = adapter
        tabs.setupWithViewPager(pager)
    }

    inner class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(
        fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> cmdFragment
                1 -> jniFragment
                else -> cmdFragment
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when(position) {
                0 -> "可执行文件调用"
                1 -> "JNI 调用"
                else -> "可执行文件调用"
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }
}
