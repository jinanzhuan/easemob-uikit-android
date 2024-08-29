package com.hyphenate.easeui.feature.contact.interfaces

import com.hyphenate.easeui.model.EaseUser

interface OnContactEventListener {
    /**
     * Load contact list successfully.
     */
    fun loadContactListSuccess(userList: MutableList<EaseUser>)

    /**
     *Load contact list Fail
     */
    fun loadContactListFail(code: Int, error: String)

    /**
     * Add contact successfully.
     */
    fun addContactSuccess(userId: String) {}

    /**
     * Add contact failed.
     */
    fun addContactFail(code: Int, error: String){}
}