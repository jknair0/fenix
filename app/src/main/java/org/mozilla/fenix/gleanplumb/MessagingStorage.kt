package org.mozilla.fenix.gleanplumb

import android.content.Context
import org.json.JSONObject
import org.mozilla.experiments.nimbus.GleanPlumbInterface
import org.mozilla.experiments.nimbus.internal.FeatureHolder
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.nimbus.Messaging
import org.mozilla.fenix.nimbus.StyleData

class MessagingStorage(
    private val context: Context,
    private val storage: MessageStorage,
    private val gleanPlumb: GleanPlumbInterface,
    private val messagingFeature: FeatureHolder<Messaging>,
    private val appStore: AppStore
) {
    private val nimbusFeature = messagingFeature.value()
    private val customAttributes: JSONObject
        get() = JSONObject()

    fun init() {
        val nimbusTriggers = nimbusFeature.triggers
        val nimbusStyles = nimbusFeature.styles
        val nimbusActions = nimbusFeature.actions

        val nimbusMessages = nimbusFeature.messages
        val defaultStyle = StyleData(context)
        val storageMetadata = storage.getMetadata().associateBy {
            it.id
        }

        val availableMessages = nimbusMessages.map { (key, value) ->
            val action = if (value.action.startsWith("http")) {
                value.action
            } else {
                nimbusActions[value.action] ?: ""
            }

            Message(
                id = key,
                data = value,
                action = action, // empty or blank
                style = nimbusStyles[value.style] ?: defaultStyle,
                metadata = storageMetadata[key] ?: addMetadata(key),
                triggers = value.trigger.mapNotNull { // empty or blank
                    nimbusTriggers[it]
                }
            )
        }.filter {
            it.data.maxDisplayCount >= it.metadata.displayCount &&
                    !it.metadata.dismissed &&
                    !it.metadata.pressed
        })

        appStore.dispatch(AppAction.UpdateMessages(availableMessages))
    }

    fun getMessages() = Unit // List<Messages>

    fun onMessageClicked(message: Message) {
        // Update storage
        storage.updateMetadata(
            message.metadata.copy(
                pressed = true
            )
        )

        val helper = gleanPlumb.createMessageHelper(customAttributes)
        val uuid = helper.getUuid(message.action)
        // TODO: Record uuid metric in glean
        return helper.stringFormat(message.action, uuid)

        // TODO nimbus please add this instead of the three-lines above.
        //val action = gleanPlumb.getMeAction(customAttributes)
    }
    fun onMessageDismissed(message: Message) = Unit
    fun onMessageDisplayed(message: Message) = Unit

    private fun addMetadata(id: String): Message.Metadata {
        return storage.addMetadata(
            Message.Metadata(
                id = id,
            )
        )
    }
}