<template>
  <div class="message-center-page">
    <div class="page-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane name="SYSTEM">
          <template #label>
            <span>系统通知 <el-badge :value="systemUnread" :hidden="systemUnread === 0" /></span>
          </template>
          <div class="message-list">
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ unread: !msg.isRead }">
              <h4>{{ msg.title }}</h4>
              <p>{{ msg.content }}</p>
              <span class="time">{{ msg.createTime }}</span>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无通知" />
          </div>
        </el-tab-pane>
        <el-tab-pane name="ORDER">
          <template #label>
            <span>交易通知 <el-badge :value="orderUnread" :hidden="orderUnread === 0" /></span>
          </template>
          <div class="message-list">
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ unread: !msg.isRead }">
              <h4>{{ msg.title }}</h4>
              <p>{{ msg.content }}</p>
              <span class="time">{{ msg.createTime }}</span>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无通知" />
          </div>
        </el-tab-pane>
        <el-tab-pane name="PRIVATE">
          <template #label>
            <span>私信 <el-badge :value="privateUnread" :hidden="privateUnread === 0" /></span>
          </template>
          <div class="message-list">
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ unread: !msg.isRead }">
              <div class="message-header">
                <span class="sender">{{ msg.fromUsername || '用户' }}</span>
                <el-button size="small" type="primary" @click="handleReply(msg)">进入聊天</el-button>
              </div>
              <p>{{ msg.content }}</p>
              <span class="time">{{ msg.createTime }}</span>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无私信" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMessageList, markAsReadByType } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'
import { connectChatSocket, disconnectChatSocket, getChatSocketClient } from '@/utils/chatSocket'

const messageStore = useMessageStore()
const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('SYSTEM')
const messages = ref([])
let subscription = null

const systemUnread = computed(() => messageStore.unreadCount.SYSTEM || 0)
const orderUnread = computed(() => messageStore.unreadCount.ORDER || 0)
const privateUnread = computed(() => messageStore.unreadCount.PRIVATE || 0)

const loadMessages = async () => {
  try {
    const res = await getMessageList({ type: activeTab.value, pageNum: 1, pageSize: 100 })
    messages.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to load messages:', error)
  }
}

const handleReply = (msg) => {
  router.push({ path: `/chat/${msg.fromUserId}`, query: { name: msg.fromUsername || '用户' } })
}

const bindSocket = () => {
  if (!localStorage.getItem('token')) {
    return
  }
  connectChatSocket((client) => {
    if (subscription) {
      subscription.unsubscribe()
      subscription = null
    }
    subscription = client.subscribe('/user/queue/message', async () => {
      await messageStore.fetchUnreadCount()
      if (activeTab.value === 'PRIVATE') {
        await loadMessages()
      }
    })
  })
}

watch(activeTab, (newType) => {
  if (!userStore.isLoggedIn) {
    return
  }
  loadMessages()
  markAsReadByType(newType)
  messageStore.fetchUnreadCount()
}, { immediate: true })

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  bindSocket()
})

onBeforeUnmount(() => {
  if (subscription) {
    subscription.unsubscribe()
    subscription = null
  }
  const client = getChatSocketClient()
  if (client && client.connected) {
    disconnectChatSocket()
  }
})
</script>

<style scoped lang="scss">
.message-center-page {
  padding: 20px 0;
}

.message-list {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.message-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;

  &.unread {
    background: #f0f9ff;
    margin: 0 -20px;
    padding: 15px 20px;
  }

  h4 {
    margin-bottom: 8px;
  }

  p {
    color: #666;
    margin-bottom: 8px;
  }

  .time {
    color: #999;
    font-size: 12px;
  }
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .sender {
    font-weight: bold;
    color: #409eff;
  }
}
</style>
