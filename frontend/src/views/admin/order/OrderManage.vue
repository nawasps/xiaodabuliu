<template>
  <div class="order-manage-page">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索订单号" @keyup.enter="loadOrders" style="width: 200px; margin-right: 10px;" />
      <el-select v-model="status" placeholder="订单状态" @change="loadOrders" clearable style="width: 150px;">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="PENDING_PAY" />
        <el-option label="待发货" value="PAID" />
        <el-option label="待收货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="totalAmount" label="金额">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="买家ID">{{ currentOrder.userId }}</el-descriptions-item>
        <el-descriptions-item label="卖家ID">{{ currentOrder.sellerId }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="快递单号">{{ currentOrder.trackingNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '未支付' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ currentOrder.shipTime || '未发货' }}</el-descriptions-item>
        <el-descriptions-item label="收货时间">{{ currentOrder.receiveTime || '未收货' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const orders = ref([])
const loading = ref(false)
const status = ref('')
const keyword = ref('')
const detailVisible = ref(false)
const currentOrder = ref({})

const getStatusText = (status) => {
  const map = {
    'PENDING_PAY': '待付款',
    'PAID': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'PENDING_PAY': 'warning',
    'PAID': '',
    'SHIPPED': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (status.value) {
      params.status = status.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    const res = await request.get('/admin/order/list', { params })
    orders.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

const showDetail = async (row) => {
  const res = await request.get(`/admin/order/${row.id}`)
  currentOrder.value = res.data
  detailVisible.value = true
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.order-manage-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>