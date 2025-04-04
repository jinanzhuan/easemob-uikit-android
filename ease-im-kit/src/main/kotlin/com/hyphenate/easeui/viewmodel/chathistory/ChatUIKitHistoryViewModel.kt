package com.hyphenate.easeui.viewmodel.chathistory

import androidx.lifecycle.viewModelScope
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatValueCallback
import com.hyphenate.easeui.feature.chat.interfaces.IChatHistoryResultView
import com.hyphenate.easeui.repository.ChatUIKitManagerRepository
import com.hyphenate.easeui.viewmodel.ChatUIKitBaseViewModel
import kotlinx.coroutines.launch

class ChatUIKitHistoryViewModel: ChatUIKitBaseViewModel<IChatHistoryResultView>(), IChatHistoryRequest {

    private val chatRepository by lazy { ChatUIKitManagerRepository() }

    override fun downloadCombineMessage(message: ChatMessage?) {
        viewModelScope.launch {
            chatRepository.downloadCombinedMessageAttachment(message, object : ChatValueCallback<List<ChatMessage>> {
                override fun onSuccess(value: List<ChatMessage>?) {
                    if (value.isNullOrEmpty()) {
                        view?.downloadCombinedMessagesFail(ChatError.GENERAL_ERROR, "No message found.")
                        return
                    }
                    view?.downloadCombinedMessagesSuccess(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    view?.downloadCombinedMessagesFail(error, errorMsg)
                }
            })
        }
    }
}