package eco.libros.android.common

import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R

object CustomFragmentManager {
    fun removeCurrentFragment(activity: FragmentActivity) {
        val manager = activity.supportFragmentManager
        if (manager.backStackEntryCount > 0) {
            manager.popBackStack()
        }
    }

    fun addFragment(
            activity: FragmentActivity,
            nextFrag: Fragment?,
            name: String?
    ) {
        val manager = activity.supportFragmentManager

        val ft = manager.beginTransaction()
        if (nextFrag != null) {
            ft.add(R.id.fragment_container,nextFrag,name)
        }
        ft.addToBackStack(name).commitAllowingStateLoss()
        activity.findViewById<TextView>(R.id.toolbar_text)?.text = name
        manager.executePendingTransactions()
    }

    fun addFragmentOnMainTitle(
            activity: FragmentActivity,
            nextFrag: Fragment?,
            name: String?
    ) {
        val manager = activity.supportFragmentManager
        for (i in manager.backStackEntryCount downTo 1) {
            manager.popBackStack()
        }
        val ft = manager.beginTransaction()
        if (nextFrag != null) {
            ft.replace(R.id.fragment_container,nextFrag,name)
        }
        ft.addToBackStack(name)
        ft.commit()

        manager.executePendingTransactions()
    }

    fun addFragmentOnMain(
            activity: FragmentActivity,
            nextFrag: Fragment?,
            name: String?
    ) {
        val manager = activity.supportFragmentManager
        for (i in manager.backStackEntryCount downTo 1) {
            manager.popBackStack()
        }
        val ft = manager.beginTransaction()
        if (nextFrag != null) {
            ft.add(R.id.fragment_container, nextFrag)
        }
        ft.addToBackStack(name)
        ft.commit()
        manager.executePendingTransactions()
    }

    fun addSettingFragmentOnMain(
        activity: FragmentActivity,
        nextFrag: Fragment?,
        name: String?
    ) {
        val manager = activity.supportFragmentManager
        val ft = manager.beginTransaction()
        if (nextFrag != null) {
            ft.add(R.id.fragment_container, nextFrag)
        }
        ft.addToBackStack(name).commitAllowingStateLoss()
        activity.findViewById<TextView>(R.id.toolbar_text)?.text = name
        manager.executePendingTransactions()
    }

    fun goToMain(activity: FragmentActivity) {
        val manager = activity.supportFragmentManager
        for (i in manager.backStackEntryCount downTo 1) {
            manager.popBackStack()
        }
    }

    fun getFragmentCount(activity: FragmentActivity): Int {
        val manager = activity.supportFragmentManager
        return manager.backStackEntryCount
    }

    fun getCurrentCustomFragment(activity: FragmentActivity): CustomFragment? {
        var returnFragment: CustomFragment? = null
        val manager = activity.supportFragmentManager
        if (manager.backStackEntryCount == 0) {
            return null
        }
        val backEntry =
                manager.getBackStackEntryAt(manager.backStackEntryCount - 1)
        val fragmentName = backEntry.name
        val fragment = manager.findFragmentByTag(fragmentName)
        if (fragment != null && fragment is CustomFragment) {
            returnFragment = fragment as CustomFragment?
        }
        return returnFragment
    }
}