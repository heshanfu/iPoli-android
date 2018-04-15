package io.ipoli.android.common.view

import android.support.annotation.ColorRes
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/11/2018.
 */
fun IconicsDrawable.largeIcon(icon: IIcon): IconicsDrawable =
    icon(icon)
        .colorRes(R.color.md_white)
        .paddingDp(8)
        .sizeDp(64)

fun IconicsDrawable.normalIcon(icon: IIcon, @ColorRes color: Int): IconicsDrawable =
    icon(icon)
        .colorRes(color)
        .sizeDp(24)