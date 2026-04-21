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
        <el-form-item label="书籍图片" prop="images">
          <el-upload
            list-type="picture-card"
            :file-list="imageFileList"
            :http-request="handleImageUpload"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            :limit="6"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传6张，支持 jpg/png/webp，单张不超过5MB</div>
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
import { Plus } from '@element-plus/icons-vue'
import { publishBook, updateBook, getBookById } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { uploadBookImage } from '@/api/upload'
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
  images: [],
  tags: []
})
const categories = ref([])
const tags = ref([])
const imageFileList = ref([])

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择新旧程度', trigger: 'change' }],
  images: [{ required: true, type: 'array', min: 1, message: '请至少上传1张图片', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const syncImageListToForm = () => {
  form.value.images = imageFileList.value.map(item => item.url)
}

const beforeImageUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 JPG、PNG、WEBP 格式图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleImageUpload = async (options) => {
  try {
    const res = await uploadBookImage(options.file)
    const url = res.data?.url
    if (!url) {
      throw new Error('上传失败')
    }
    imageFileList.value.push({ name: options.file.name, url })
    syncImageListToForm()
    ElMessage.success('图片上传成功')
    options.onSuccess(res)
  } catch (error) {
    ElMessage.error(error.message || '图片上传失败')
    options.onError(error)
  }
}

const handleImageRemove = (file) => {
  imageFileList.value = imageFileList.value.filter(item => item.url !== file.url)
  syncImageListToForm()
}

const handleExceed = () => {
  ElMessage.warning('最多上传6张图片')
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
    const tagRes = await request.get('/tag/list')
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
          images: book.images || [],
          tags: book.tags || []
        }
        imageFileList.value = (book.images || []).map((url, index) => ({
          name: `book-image-${index + 1}`,
          url
        }))
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
      images: form.value.images,
      tags: form.value.tags
    }

    if (isEdit.value) {
      await updateBook(bookId.value, submitData)
      ElMessage.success('修改成功，已重新进入审核')
    } else {
      await publishBook(submitData)
      ElMessage.success('发布成功，等待管理员审核')
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

  :deep(.el-upload--picture-card),
  :deep(.el-upload-list__item) {
    width: 120px;
    height: 120px;
  }

  .upload-tip {
    margin-top: 8px;
    color: #8c8c8c;
    font-size: 12px;
  }
}
</style>
