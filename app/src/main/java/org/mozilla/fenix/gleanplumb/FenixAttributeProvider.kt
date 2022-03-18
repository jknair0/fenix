package org.mozilla.fenix.gleanplumb

import android.content.Context
import org.json.JSONObject
import org.mozilla.fenix.utils.BrowsersCache
import java.text.SimpleDateFormat
import java.util.*

private val formatter = SimpleDateFormat("yyyy-MM-dd")

object FenixAttributeProvider {
    fun getCustomAttributes(context: Context): JSONObject? {
        val now = Calendar.getInstance()

        return JSONObject(
            mapOf(
                "is_default_browser" to BrowsersCache.all(context).isDefaultBrowser,
                "date_string" to formatter.format(now.time)
            )
        )
    }
}