/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gleanplumb.state

import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.Initialize
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageClicked
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageDismissed
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.UpdateMessages
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.MessageDisplayed
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.Evaluate
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.UpdateMessageToShow
import org.mozilla.fenix.components.appstate.AppState
import org.mozilla.fenix.gleanplumb.Message
import org.mozilla.fenix.gleanplumb.MessagingStorage

class MessagingMiddleware(private val storage: MessagingStorage) : Middleware<AppState, AppAction> {

    override fun invoke(
        context: MiddlewareContext<AppState, AppAction>,
        next: (AppAction) -> Unit,
        action: AppAction
    ) {
        when (action) {
            is Evaluate -> {
                val nextMessage = storage.getNextMessage()
                context.dispatch(UpdateMessageToShow(nextMessage))
            }
            is Initialize -> {
                storage.init()
            }
            is MessageClicked -> {
                // Update Nimbus storage.
                val updatedMetadata = action.message.metadata.copy(pressed = true)
                storage.updateMetadata(updatedMetadata)

                // Update app state.
                val newMessages = removeMessage(context, action.message)
                context.dispatch(UpdateMessages(newMessages))
            }
            is MessageDismissed -> {
                val newMessages = removeMessage(context, action.message)
                val updatedMetadata = action.message.metadata.copy(dismissed = true)

                storage.updateMetadata(updatedMetadata)
                context.dispatch(UpdateMessages(newMessages))
            }
            is MessageDisplayed -> {
                val oldMessage = action.message
                val newMetadata = oldMessage.metadata.copy(
                    displayCount = oldMessage.metadata.displayCount + 1
                )
                val newMessage = oldMessage.copy(
                    metadata = newMetadata
                )
                val newMessages = if (newMetadata.displayCount < oldMessage.data.maxDisplayCount) {
                    updateMessage(context, oldMessage, newMessage)
                } else {
                    removeMessage(context, oldMessage)
                }
                context.dispatch(UpdateMessages(newMessages))
                storage.updateMetadata(newMetadata)
            }
        }
    }

    private fun removeMessage(
        context: MiddlewareContext<AppState, AppAction>,
        message: Message
    ): List<Message> {
        return context.state.messagingState.messages - message
    }

    private fun updateMessage(
        context: MiddlewareContext<AppState, AppAction>,
        oldMessage: Message,
        updatedMessage: Message
    ): List<Message> {
        return removeMessage(context, oldMessage) + updatedMessage
    }
}
