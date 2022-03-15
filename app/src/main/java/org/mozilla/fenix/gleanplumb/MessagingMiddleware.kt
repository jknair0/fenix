package org.mozilla.fenix.gleanplumb

import mozilla.appservices.sync15.stringOrNull
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.appstate.AppState

class MessagingMiddleware(private val storage: MessagingStorage) : Middleware<AppState, AppAction> {
    override fun invoke(
        context: MiddlewareContext<AppState, AppAction>,
        next: (AppAction) -> Unit,
        action: AppAction
    ) {
        when (action) {
            is AppAction.InitializeNimbus -> {
                storage.init()
            }
            is AppAction.MessageClicked -> {
                // Update Nimbus storage.
                storage.onMessageClicked(action.message)

                // Update app state.
                val newMessages = context.state.gleanPlumbMessages - action.message
                context.dispatch(AppAction.UpdateMessages(newMessages))
            }
            is AppAction.MessageDismissed -> {
                storage.onMessageClicked(action.message)
            }
            is AppAction.MessageClicked -> {
                storage.onMessageClicked(action.message)
            }
        }
    }
}
