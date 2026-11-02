package com.tj.launcher

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView

/**
 * Foundation Home screen: a plain GridView of installed apps. No UI library on purpose — it
 * proves the HOME wiring and the build/CI pipeline while the real design is still an open
 * decision (BRIEF.md). Replace freely; keep the manifest's HOME intent filter.
 */
class HomeActivity : Activity() {
    private lateinit var grid: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grid = GridView(this).apply {
            numColumns = 4
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        setContentView(grid)
    }

    override fun onResume() {
        super.onResume()
        val apps = AppList.sorted(AppList.withoutSelf(installedApps(), packageName))
        grid.adapter = object : ArrayAdapter<AppEntry>(this, 0, apps) {
            override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
                val v = (convertView as? TextView) ?: TextView(context).apply {
                    gravity = android.view.Gravity.CENTER
                    setPadding(8, 24, 8, 24)
                    setTextColor(0xFFFFFFFF.toInt())
                    maxLines = 2
                }
                v.text = getItem(position)?.label
                return v
            }
        }
        grid.setOnItemClickListener { _, _, position, _ ->
            val a = apps[position]
            startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(a.packageName, a.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            })
        }
    }

    // Home is the root: Back does nothing (a launcher that exits to nowhere is a black screen).
    @Deprecated("Home is the root screen")
    override fun onBackPressed() {}

    private fun installedApps(): List<AppEntry> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(query, 0).map {
            AppEntry(it.loadLabel(packageManager).toString(), it.activityInfo.packageName, it.activityInfo.name)
        }
    }
}
