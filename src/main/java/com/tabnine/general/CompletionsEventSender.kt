package com.tabnine.general

import com.intellij.openapi.application.ApplicationManager
import com.tabnine.binary.BinaryRequestFacade
import com.tabnine.binary.requests.analytics.EventRequest
import com.tabnine.inline.CompletionOrder

class CompletionsEventSender(private val binaryRequestFacade: BinaryRequestFacade) {
    fun sendToggleInlineSuggestionEvent(order: CompletionOrder, index: Int) {
        val eventOrder = if (order == CompletionOrder.NEXT) {
            "next"
        } else {
            "previous"
        }
        val eventName = "toggle-$eventOrder-suggestion"
        val event = EventRequest(eventName, mapOf("suggestion_index" to index.toString()))

        sendEventAsync(event)
    }

    fun sendManualSuggestionTrigger() {
        val event = EventRequest("manual-suggestion-trigger", mapOf())
        sendEventAsync(event)
    }

    private fun sendEventAsync(event: EventRequest) {
        ApplicationManager.getApplication().invokeLater {
            binaryRequestFacade.executeRequest(event)
        }
    }
}
