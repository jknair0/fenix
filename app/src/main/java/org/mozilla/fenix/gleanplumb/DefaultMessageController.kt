/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gleanplumb

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import org.mozilla.fenix.BuildConfig
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageClicked
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageDismissed
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageDisplayed

/**
 * Handles default interactions with the ui of GleanPlumb messages.
 */
class DefaultMessageController(
    private val appStore: AppStore,
    private val messagingStorage: MessagingStorage,
    private val homeActivity: HomeActivity
) : MessageController {
// Report malformed message events

    override fun onMessagePressed(message: Message) {
        // Report telemetry event
        // This will be covered on https://github.com/mozilla-mobile/fenix/issues/24224
        val action = messagingStorage.getMessageAction(message)
        handleAction(action)
        appStore.dispatch(MessageDismissed(message))
    }

    override fun onMessageDismissed(message: Message) {
        // Report telemetry event
        // This will be covered on https://github.com/mozilla-mobile/fenix/issues/24224
        appStore.dispatch(MessageClicked(message))
    }

    override fun onMessageDisplayed(message: Message) {
        // Report telemetry event
        // This will be covered on https://github.com/mozilla-mobile/fenix/issues/24224
        appStore.dispatch(MessageDisplayed(message))
    }

    @VisibleForTesting
    internal fun handleAction(action: String): Intent {
        val partialAction = if (action.startsWith("http", ignoreCase = true)) {
            "://open?url=${action}" // TODO: URL encode
        } else {
            action
        }
        val intent = Intent(Intent.ACTION_VIEW, "${BuildConfig.DEEP_LINK_SCHEME}$partialAction".toUri())
        homeActivity.processIntent(intent)

        return intent
    }
}
