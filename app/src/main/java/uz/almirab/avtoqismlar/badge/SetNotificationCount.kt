package uz.almirab.avtoqismlar.badge

import android.content.Context
import android.graphics.drawable.LayerDrawable

 import uz.almirab.avtoqismlar.R

/**
 * Created by NguyenLuanPC on 19-07-2017.
 */

object SetNotificationCount {
    fun setBadgeCount(context: Context, icon: LayerDrawable, count: Int) {

        val badge: BadgeDrawable

        // Reuse drawable if possible
        val reuse = icon.findDrawableByLayerId(R.id.ic_badge)
        if (reuse != null && reuse is BadgeDrawable) {
            badge = reuse
        } else {
            badge = BadgeDrawable(context)
        }

        badge.setCount(count)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.ic_badge, badge)
    }
}
