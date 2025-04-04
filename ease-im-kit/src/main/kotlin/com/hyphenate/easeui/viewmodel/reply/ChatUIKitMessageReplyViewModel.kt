package com.hyphenate.easeui.viewmodel.reply

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.R
import com.hyphenate.easeui.viewmodel.ChatUIKitBaseViewModel
import com.hyphenate.easeui.common.ChatError
import com.hyphenate.easeui.common.ChatFileMessageBody
import com.hyphenate.easeui.common.ChatImageMessageBody
import com.hyphenate.easeui.common.ChatLocationMessageBody
import com.hyphenate.easeui.common.ChatMessage
import com.hyphenate.easeui.common.ChatMessageType
import com.hyphenate.easeui.common.ChatTextMessageBody
import com.hyphenate.easeui.common.ChatVideoMessageBody
import com.hyphenate.easeui.common.ChatVoiceMessageBody
import com.hyphenate.easeui.common.ChatUIKitConstant
import com.hyphenate.easeui.common.extensions.getEmojiText
import com.hyphenate.easeui.common.extensions.getUserCardInfo
import com.hyphenate.easeui.common.extensions.getUserInfo
import com.hyphenate.easeui.common.extensions.isUserCardMessage
import com.hyphenate.easeui.common.extensions.toUser
import com.hyphenate.easeui.common.utils.ChatUIKitFileUtils
import com.hyphenate.easeui.feature.chat.internal.setTargetSpan
import com.hyphenate.easeui.feature.chat.reply.interfaces.IChatMessageReplyResultView
import com.hyphenate.easeui.model.ChatUIKitUser
import com.hyphenate.easeui.model.getNickname

open class ChatUIKitMessageReplyViewModel: ChatUIKitBaseViewModel<IChatMessageReplyResultView>(), IChatMessageReplyRequest {

    override fun showQuoteMessageInfo(message: ChatMessage?) {
        if (message == null || message.body == null) {
            view?.onShowError(ChatError.GENERAL_ERROR, "Message or body cannot be null.")
            return
        }
        val user: ChatUIKitUser? = message.getUserInfo()?.toUser()
        var from:String? = null
        from = if (user == null) {
            message.from
        } else {
            user.getRemarkOrName()
        }
        view?.showQuoteMessageNickname(from)
        val builder = SpannableStringBuilder()
        var localPath: String? = null
        var remoteUrl: String? = null
        when (message.type) {
            ChatMessageType.TXT -> if (message.getBooleanAttribute(
                    ChatUIKitConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                    false
                )
            ) {
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_emoji_type))
            } else {
                val textBody = message.body as ChatTextMessageBody
                builder.append(
                    "${textBody.message.getEmojiText(ChatUIKitClient.getContext()!!)}"
                    ).toString()
            }

            ChatMessageType.VOICE -> {
                val voiceBody = message.body as ChatVoiceMessageBody
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_voice_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(" ")
                    .append((if (voiceBody.length > 0) voiceBody.length else 0).toString() + "\"")
                view?.showQuoteMessageAttachment(
                    ChatMessageType.VOICE,
                    null,
                    null,
                    R.drawable.uikit_chatfrom_voice_playing
                )
            }

            ChatMessageType.VIDEO -> {
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_video_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                val videoBody = message.body as ChatVideoMessageBody
                videoBody?.let {
                    if (!TextUtils.isEmpty(it.localThumb) && ChatUIKitFileUtils.isFileExistByUri(
                            ChatUIKitClient.getContext(),
                            it.localThumbUri
                        )
                    ) {
                        localPath = it.localThumb
                    }
                    remoteUrl = it.thumbnailUrl
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.VIDEO,
                        localPath,
                        remoteUrl,
                        R.drawable.uikit_chat_quote_icon_video
                    )
                }

            }

            ChatMessageType.FILE -> {
                val fileBody = message.body as ChatFileMessageBody
                builder.append(ChatUIKitClient.getContext()?.getResources()?.getString(R.string.uikit_message_reply_file_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(" ")
                    .append(fileBody.fileName)
                view?.showQuoteMessageAttachment(
                    ChatMessageType.FILE,
                    null,
                    null,
                    R.drawable.uikit_chat_quote_message_attachment
                )
            }

            ChatMessageType.IMAGE -> {
                builder.append(ChatUIKitClient.getContext()?.getResources()?.getString(R.string.uikit_message_reply_image_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                val imageBody = message.body as ChatImageMessageBody
                imageBody?.let {
                    if (!imageBody.thumbnailUrl.isNullOrEmpty() && ChatUIKitFileUtils.isFileExistByUri(
                            ChatUIKitClient.getContext(), imageBody.thumbnailLocalUri())) {
                        localPath = imageBody.thumbnailLocalPath()
                    } else if (!imageBody.localUrl.isNullOrEmpty() && ChatUIKitFileUtils.isFileExistByUri(
                        ChatUIKitClient.getContext(), imageBody.localUri)) {
                        localPath = imageBody.localUrl
                    }
                    remoteUrl = imageBody.remoteUrl
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.IMAGE,
                        localPath,
                        remoteUrl,
                        R.drawable.uikit_chat_quote_icon_image
                    )
                }
            }

            ChatMessageType.LOCATION -> {
                val locationBody = message.body as ChatLocationMessageBody
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_location_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (locationBody != null && !TextUtils.isEmpty(locationBody.address)) {
                    builder.append(": ").append(locationBody.address)
                }
            }

            ChatMessageType.CUSTOM -> {
                if (message.isUserCardMessage()) {
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.CUSTOM,
                        null,
                        null,
                        R.drawable.uikit_chat_quote_icon_user_card
                    )
                    builder.append(message.getUserCardInfo()?.name)
                        .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                            0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_custom_type))
                        .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                            0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            ChatMessageType.COMBINE -> {
                view?.showQuoteMessageAttachment(
                    ChatMessageType.COMBINE,
                    null,
                    null,
                    R.drawable.uikit_chat_quote_icon_combine
                )
                builder.append(
                    ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_combine_type)
                )
            }

            else -> {}
        }
        view?.showQuoteMessageContent(builder)
    }

}