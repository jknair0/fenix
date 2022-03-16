/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.gleanplumb

import android.content.Context
import mozilla.components.support.base.log.logger.Logger
import org.json.JSONObject
import org.mozilla.experiments.nimbus.GleanPlumbInterface
import org.mozilla.experiments.nimbus.GleanPlumbMessageHelper
import org.mozilla.experiments.nimbus.internal.FeatureHolder
import org.mozilla.experiments.nimbus.internal.NimbusException
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction.MessagingAction.UpdateMessages
import org.mozilla.fenix.nimbus.Messaging
import org.mozilla.fenix.nimbus.StyleData

class MessagingStorage(
    private val context: Context,
    private val metadataStorage: MessageMetadataStorage,
    private val gleanPlumb: GleanPlumbInterface,
    private val messagingFeature: FeatureHolder<Messaging>,
    private val appStore: AppStore
) {
    private val logger = Logger("MessagingStorage")
    private val nimbusFeature = messagingFeature.value()
    private val customAttributes: JSONObject
        get() = JSONObject()

    fun init() {
        val nimbusTriggers = nimbusFeature.triggers
        val nimbusStyles = nimbusFeature.styles
        val nimbusActions = nimbusFeature.actions

        val nimbusMessages = nimbusFeature.messages
        val defaultStyle = StyleData(context)
        val storageMetadata = metadataStorage.getMetadata().associateBy {
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
        }

        appStore.dispatch(UpdateMessages(availableMessages))
    }

    fun getNextMessage(): Message? {
        val availableMessages = appStore.state.messagingState.messages
        val helper = gleanPlumb.createMessageHelper(customAttributes)
        var message = availableMessages.firstOrNull {
            isMessageEligible(it, helper)
        } ?: return null


        if (isMessageUnderExperiment(message, nimbusFeature.messageUnderExperiment)) {
            messagingFeature.recordExposure()

            if (message.data.isControl) {
                message = availableMessages.firstOrNull {
                    !it.data.isControl && isMessageEligible(it, helper)
                } ?: return null
            }
        }

        return message
    }

    private fun isMessageUnderExperiment(message: Message, expression: String?): Boolean {
        return when {
            expression.isNullOrBlank() -> {
                false
            }
            expression.endsWith("-") -> {
                message.id.startsWith(expression)
            }
            else -> {
                message.id == expression
            }
        }
    }

    fun getMessageAction(message: Message): String {
        val helper = gleanPlumb.createMessageHelper(customAttributes)
        val uuid = helper.getUuid(message.action)

        return helper.stringFormat(message.action, uuid)
    }

    fun updateMetadata(metadata: Message.Metadata) {
        metadataStorage.updateMetadata(metadata)
    }

    private fun isMessageEligible(
        message: Message,
        helper: GleanPlumbMessageHelper
    ): Boolean {
        return message.triggers.all { condition ->
            try {
                helper.evalJexl(condition)
            } catch (e: NimbusException.EvaluationException) {
                // TODO: report to glean as malformed message
                logger.info("Unable to evaluate $condition")
                false
            }
        }
    }

    private fun addMetadata(id: String): Message.Metadata {
        return metadataStorage.addMetadata(
            Message.Metadata(
                id = id,
            )
        )
    }
}