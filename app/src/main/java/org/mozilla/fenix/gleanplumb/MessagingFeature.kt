/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gleanplumb

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.lib.state.helpers.AbstractBinding
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.appstate.AppState

class MessagingFeature(
    val store: AppStore,
    private val onMessageChange: (Message?) -> Unit
) : AbstractBinding<AppState>(store) {

    override suspend fun onState(flow: Flow<AppState>) {
        flow.map { it }.ifChanged {
            it.messagingState.messageToShow
        }.collect { state ->
            onMessageChange(state.messagingState.messageToShow)
        }
    }

    override fun start() {
        super.start()
        store.dispatch(AppAction.MessagingAction.Evaluate)
    }
}
