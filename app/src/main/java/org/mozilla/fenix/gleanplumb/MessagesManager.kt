/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gleanplumb

import android.content.Context
import org.mozilla.experiments.nimbus.internal.FeatureHolder
import org.mozilla.fenix.nimbus.Messaging

/**
 * Handles all interactions messages from nimbus.
 * The implementation will be covered on https://github.com/mozilla-mobile/fenix/issues/24223
 */
class MessagesManager(
    private val context: Context,
    private val storage: MessageStorage,
    private val messagingFeature: FeatureHolder<Messaging>
) {

    fun areMessagesAvailable(): Boolean = false

    fun getNextMessage(): Message? = null

    @Suppress("UNUSED_PARAMETER")
    fun onMessagePressed(message: Message) = Unit

    @Suppress("UNUSED_PARAMETER")
    fun onMessageDismissed(message: Message) = Unit

    @Suppress("UNUSED_PARAMETER")
    fun onMessageDisplayed(message: Message) = Unit

    fun initialize() = Unit
}
