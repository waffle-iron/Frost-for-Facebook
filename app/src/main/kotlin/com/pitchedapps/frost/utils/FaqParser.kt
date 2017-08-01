package com.pitchedapps.frost.utils

import android.content.Context
import android.content.res.XmlResourceParser
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.annotation.XmlRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.allanwang.kau.R
import ca.allanwang.kau.changelog.showChangelog
import ca.allanwang.kau.utils.bindOptionalView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.use
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser

/**
 * Created by Allan Wang on 2017-07-30.
 */
fun Context.getFaq(@XmlRes xmlRes: Int, @ColorInt textColor: Int? = null, customize: MaterialDialog.Builder.() -> Unit = {}) {
    doAsync {
        val items = parse(this@showChangelog, xmlRes)
        uiThread {
            materialDialog {
                title(R.string.kau_changelog)
                positiveText(R.string.kau_great)
                adapter(ChangelogAdapter(items, textColor), null)
                customize()
            }
        }
    }
}

internal fun parseFaq(context: Context, @XmlRes xmlRes: Int): List<Pair<String, String>> {
    val items = mutableListOf<Pair<String, String>>()
    context.resources.getXml(xmlRes).use {
        parser: XmlResourceParser ->
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG)
                ChangelogType.values.any { it.add(parser, items) }
            eventType = parser.next()
        }
    }
    return items
}

internal enum class ChangelogType(val tag: String, val attr: String, @LayoutRes val layout: Int) {
    TITLE("version", "title", R.layout.kau_changelog_title),
    ITEM("item", "text", R.layout.kau_changelog_content);

    companion object {
        @JvmStatic val values = values()
    }

    /**
     * Returns true if tag matches; false otherwise
     */
    fun add(parser: XmlResourceParser, list: MutableList<Pair<String, ChangelogType>>): Boolean {
        if (parser.name != tag) return false
        if (parser.getAttributeValue(null, attr).isNotBlank())
            list.add(Pair(parser.getAttributeValue(null, attr), this))
        return true
    }
}