/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.appstate

import mozilla.components.lib.crash.Crash.NativeCodeCrash
import mozilla.components.lib.state.Action
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.gleanplumb.Message

/**
 * [Action] implementation related to [AppStore].
 */
sealed class AppAction : Action {
    data class UpdateInactiveExpanded(val expanded: Boolean) : AppAction()
    data class AddNonFatalCrash(val crash: NativeCodeCrash) : AppAction()
    data class RemoveNonFatalCrash(val crash: NativeCodeCrash) : AppAction()
    object RemoveAllNonFatalCrashes : AppAction()

    sealed class MessagingAction : AppAction() {
        object Initialize : MessagingAction()
        object Evaluate : MessagingAction()
        data class UpdateMessageToShow(val message: Message?) : MessagingAction()
        data class UpdateMessages(val messages: List<Message>) : MessagingAction()
        data class MessageClicked(val message: Message) : MessagingAction()
        data class MessageDisplayed(val message: Message) : MessagingAction()
        data class MessageDismissed(val message: Message) : MessagingAction()
    }
}
