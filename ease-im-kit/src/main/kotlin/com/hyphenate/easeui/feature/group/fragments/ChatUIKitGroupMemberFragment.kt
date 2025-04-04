package com.hyphenate.easeui.feature.group.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyphenate.easeui.ChatUIKitClient
import com.hyphenate.easeui.base.ChatUIKitBaseListFragment
import com.hyphenate.easeui.base.ChatUIKitBaseRecyclerViewAdapter
import com.hyphenate.easeui.common.ChatClient
import com.hyphenate.easeui.common.ChatGroup
import com.hyphenate.easeui.common.ChatLog
import com.hyphenate.easeui.common.ChatUIKitConstant
import com.hyphenate.easeui.common.extensions.getOwnerInfo
import com.hyphenate.easeui.common.helper.ContactSortedHelper
import com.hyphenate.easeui.feature.group.adapter.ChatUIKitGroupMemberListAdapter
import com.hyphenate.easeui.feature.group.interfaces.IUIKitGroupResultView
import com.hyphenate.easeui.feature.group.interfaces.IGroupMemberEventListener
import com.hyphenate.easeui.model.ChatUIKitProfile
import com.hyphenate.easeui.model.ChatUIKitUser
import com.hyphenate.easeui.model.setUserInitialLetter
import com.hyphenate.easeui.viewmodel.group.ChatUIKitGroupViewModel
import com.hyphenate.easeui.viewmodel.group.IGroupRequest

open class ChatUIKitGroupMemberFragment:ChatUIKitBaseListFragment<ChatUIKitUser>(),IUIKitGroupResultView {
    private var groupViewModel: IGroupRequest? = null
    protected var groupId:String?=null
    protected var currentGroup:ChatGroup?=null
    private var sortedList:MutableList<ChatUIKitUser> = mutableListOf()
    private var listener:IGroupMemberEventListener?=null

    companion object {
        private const val TAG = "ChatUIKitGroupMemberFragment"
    }

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            groupId = arguments?.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID) ?: ""
        }
        super.initView(savedInstanceState)
        setSearchViewVisible(false)
        groupId?.let {
            currentGroup = ChatClient.getInstance().groupManager().getGroup(it)
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
        groupViewModel?.attachView(this)
    }

    override fun initListener() {
        super.initListener()
        binding?.rvList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // When scroll to bottom, load more data
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val visibleList = mListAdapter.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    groupId?.let { id ->
                        val idList = visibleList?.map { user->
                            user.userId
                        }
                        if (idList.isNullOrEmpty()){
                            return
                        }
                        groupViewModel?.fetchMemberInfo(id, idList)
                    }
                }
            }
        })
    }

    override fun initData() {
        super.initData()
        loadData()
    }

    open fun loadData(){
        groupId?.let {
            groupViewModel?.fetchGroupMemberFromService(it)
        }
    }

    fun loadLocalData(){
        finishRefresh()
        groupId?.let { groupId->
            sortedList.clear()
            groupViewModel?.loadLocalMember(groupId)
        }
    }

    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return ChatUIKitGroupMemberListAdapter(groupId)
    }

    override fun refreshData() {
        loadLocalData()
    }

    override fun fetchGroupMemberSuccess(user: List<ChatUIKitUser>) {
        sortedList = user.toMutableList()
        finishRefresh()
        groupId?.let {
            val ownerInfo = currentGroup?.getOwnerInfo()
            ownerInfo?.let {
                it.setUserInitialLetter()
                it.let { it1 ->
                    if (!sortedList.contains(it1)){
                        sortedList.add(it1)
                    }
                }
            }
            sortedList = ContactSortedHelper.sortedList(sortedList).toMutableList()
            mListAdapter.setData(sortedList)
            listener?.onGroupMemberLoadSuccess(sortedList)
        }
    }

    override fun fetchGroupMemberFail(code: Int, error: String) {
        finishRefresh()
        ChatLog.e(TAG,"fetchGroupMemberFail $code $error")
    }

    override fun onItemClick(view: View?, position: Int) {
        sortedList.let {
            ChatLog.d(TAG,"onItemClick data size ${it.size} - position:$position")
            if (it.isNotEmpty() && it.size > position){
                if (ChatUIKitClient.getCurrentUser()?.id != it[position].userId){
                    listener?.onGroupMemberListItemClick(view,it[position])
                }
            }
        }
    }

    fun setOnGroupMemberItemClickListener(listener: IGroupMemberEventListener){
        this.listener = listener
    }

    override fun loadLocalMemberSuccess(members: List<ChatUIKitUser>) {
        sortedList = members.toMutableList()
        finishRefresh()
        groupId?.let {
            val ownerInfo = currentGroup?.getOwnerInfo()
            ownerInfo?.let {
                it.setUserInitialLetter()
                it.let { it1 ->
                    if (!sortedList.contains(it1)){
                        sortedList.add(it1)
                    }
                }
            }
            sortedList = ContactSortedHelper.sortedList(sortedList).toMutableList()
            mListAdapter.setData(sortedList)
            listener?.onGroupMemberLoadSuccess(sortedList)
        }
    }

    override fun fetchMemberInfoSuccess(members: Map<String, ChatUIKitProfile>?) {
        super.fetchMemberInfoSuccess(members)
        mListAdapter.notifyDataSetChanged()
    }

    override fun fetchMemberInfoFail(code: Int, error: String) {
        super.fetchMemberInfoFail(code, error)
    }
}