
变更 2：UserPublish.vue
文件: frontend/src/views/front/user/UserPublish.vue

完整替换为以下内容：

<template>
  <div class="publish-page">
    <div class="page-container">
      <h2>{{ isEdit ? '编辑书籍' : '发布书籍' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入书籍标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="4" placeholder="请输入书籍描述" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader v-model="form.categoryId" :options="categories" placeholder="请选择分类" />
        </el-form-item>
        <el-form-item label="新旧程度" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择新旧程度">
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
            <el-option label="七成新" value="七成新" />
            <el-option label="六成新" value="六成新" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple placeholder="选择标签">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishBook, updateBook, getBookById } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const bookId = computed(() => route.params.bookId)
const isEdit = computed(() => !!bookId.value)

const form = ref({
  title: '',
  description: '',
  categoryId: null,
  condition: '',
  price: 0,
  originalPrice: 0,
  tags: []
})
const categories = ref([])
const tags = ref([])

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择新旧程度', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const transformCategory = (data) => {
  return data.map(item => ({
    value: item.id,
    label: item.name,
    children: item.children?.length > 0 ? transformCategory(item.children) : []
  }))
}

onMounted(async () => {
  try {
    const catRes = await getCategoryTree()
    categories.value = transformCategory(catRes.data || [])
    const tagRes = await request.get('/admin/tag/list')
    tags.value = tagRes.data || []

    if (isEdit.value) {
      const res = await getBookById(bookId.value)
      const book = res.data
      if (book) {
        form.value = {
          title: book.title || '',
          description: book.description || '',
          categoryId: book.categoryId,
          condition: book.condition || '',
          price: book.price || 0,
          originalPrice: book.originalPrice || 0,
          tags: book.tags || []
        }
      }
    }
  } catch (error) {
    console.error('Failed to load data:', error)
  }
})

const handleSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const categoryId = Array.isArray(form.value.categoryId)
      ? form.value.categoryId[form.value.categoryId.length - 1]
      : form.value.categoryId

    const submitData = {
      title: form.value.title,
      description: form.value.description,
      categoryId: categoryId,
      condition: form.value.condition,
      price: form.value.price,
      originalPrice: form.value.originalPrice,
      tags: form.value.tags
    }

    if (isEdit.value) {
      await updateBook(bookId.value, submitData)
      ElMessage.success('修改成功')
    } else {
      await publishBook(submitData)
      ElMessage.success('发布成功')
    }
    router.push('/user/my-books')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.publish-page {
  padding: 20px 0;

  h2 {
    margin-bottom: 20px;
  }
}
</style>