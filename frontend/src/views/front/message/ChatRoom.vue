<template>
  <div class="chat-room-page">
    <div class="page-container">
      <div class="chat-card">
        <div class="chat-header">
          <div>
            <h3>实时聊天</h3>
            <p>与 {{ peerName }} 对话中</p>
          </div>
          <el-button @click="$router.push('/message')">返回消息中心</el-button>
        </div>

        <div ref="messageListRef" class="chat-messages">
          <div
            v-for="item in messages"
            :key="item.id || item.tempId"
            class="chat-item"
            :class="{ mine: item.fromUserId === currentUserId }"
          >
            <div class="bubble">{{ item.content }}</div>
            <div class="meta">{{ item.fromUserId === currentUserId ? '我' : (item.fromUsername || peerName) }} · {{ item.createTime || '刚刚' }}</div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无聊天记录" />
        </div>

        <div class="chat-input">
          <el-input
            v-model="content"
            type="textarea"
            :rows="3"
            placeholder="请输入消息，Enter发送，Shift+Enter换行"
            @keydown.enter.prevent="handleEnter"
          />
          <div class="actions">
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPrivateMessages, markAsReadByType, sendPrivateMessage } from '@/api/message'
import { connectChatSocket, disconnectChatSocket, getChatSocketClient } from '@/utils/chatSocket'
import { useMessageStore } from '@/stores/message'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()

const currentUserId = Number(JSON.parse(localStorage.getItem('userInfo') || '{}')?.id || 0)
const peerUserId = computed(() => Number(route.params.userId))
const peerName = computed(() => route.query.name || `用户${peerUserId.value}`)

const messages = ref([])
const content = ref('')
const sending = ref(false)
const messageListRef = ref(null)
let subscription = null

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const loadHistory = async () => {
  const res = await getPrivateMessages(peerUserId.value, { pageNum: 1, pageSize: 200 })
  messages.value = res.data || []
  await markAsReadByType('PRIVATE')
  await messageStore.fetchUnreadCount()
  await scrollToBottom()
}

const bindSocket = () => {
  connectChatSocket((client) => {
    if (subscription) {
      subscription.unsubscribe()
      subscription = null
    }
    subscription = client.subscribe('/user/queue/chat', async (frame) => {
      const payload = JSON.parse(frame.body)
      const isCurrentConversation =
        (payload.fromUserId === peerUserId.value && payload.toUserId === currentUserId) ||
        (payload.fromUserId === currentUserId && payload.toUserId === peerUserId.value)
      if (isCurrentConversation) {
        messages.value.push(payload)
        await markAsReadByType('PRIVATE')
      }
      await messageStore.fetchUnreadCount()
      await scrollToBottom()
    })
  }, () => {
    ElMessage.warning('实时通道连接中断，已自动重连')
  })
}

const send = async () => {
  const text = content.value.trim()
  if (!text) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sending.value = true
  try {
    const res = await sendPrivateMessage({
      toUserId: peerUserId.value,
      content: text
    })
    messages.value.push({
      ...res.data,
      tempId: `local-${Date.now()}`,
      content: text,
      fromUserId: currentUserId,
      toUserId: peerUserId.value,
      fromUsername: JSON.parse(localStorage.getItem('userInfo') || '{}')?.nickname || '我'
    })
    content.value = ''
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const handleEnter = (event) => {
  if (event.shiftKey) {
    content.value += '\n'
    return
  }
  send()
}

onMounted(async () => {
  if (!currentUserId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!peerUserId.value) {
    ElMessage.warning('聊天对象不存在')
    router.push('/message')
    return
  }
  await loadHistory()
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
.chat-room-page {
  padding: 20px 0;
}

.chat-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    margin-bottom: 4px;
  }

  p {
    color: #64748b;
    font-size: 12px;
  }
}

.chat-messages {
  height: 520px;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, #f8fbff 0%, #f3f8ff 100%);
}

.chat-item {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  .bubble {
    max-width: 68%;
    background: #ffffff;
    border: 1px solid #dbe7ff;
    border-radius: 12px;
    padding: 10px 12px;
    line-height: 1.5;
    color: #334155;
  }

  .meta {
    margin-top: 4px;
    font-size: 12px;
    color: #94a3b8;
  }

  &.mine {
    align-items: flex-end;

    .bubble {
      background: #4f86f7;
      border-color: #4f86f7;
      color: #fff;
    }
  }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #eef2f7;

  .actions {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
