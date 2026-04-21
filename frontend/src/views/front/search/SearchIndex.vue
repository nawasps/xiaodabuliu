文件: frontend/src/views/front/search/SearchIndex.vue

<template>
  <div class="search-page">
    <div class="page-container">
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索书籍" size="large" @keyup.enter="handleSearch">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <div class="hot-search" v-if="hotSearch.length > 0">
        <span class="label">热门搜索：</span>
        <el-tag v-for="item in hotSearch" :key="item" class="hot-tag" @click="handleHotSearch(item)">{{ item }}</el-tag>
      </div>

      <div class="search-history" v-if="searchHistory.length > 0">
        <span class="label">搜索历史：</span>
        <el-tag v-for="item in searchHistory" :key="item" class="history-tag" closable @close="handleDeleteHistory(item)" @click="handleHistorySearch(item)">{{ item }}</el-tag>
        <el-button size="small" text @click="handleClearHistory">清空</el-button>
      </div>

      <div class="filters">
        <el-select v-model="categoryId" placeholder="分类" @change="handleSearch" clearable>
  <el-option label="全部" value="" />
  <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
</el-select>
        <el-select v-model="condition" placeholder="新旧程度" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="全新" value="全新" />
          <el-option label="九成新" value="九成新" />
          <el-option label="八成新" value="八成新" />
          <el-option label="七成新" value="七成新" />
          <el-option label="六成新" value="六成新" />
        </el-select>
        <el-input-number v-model="minPrice" placeholder="最低价" :min="0" @change="handleSearch" />
        <span>-</span>
        <el-input-number v-model="maxPrice" placeholder="最高价" :min="0" @change="handleSearch" />
        <el-select v-model="sortBy" placeholder="排序" @change="handleSearch">
          <el-option label="最新发布" value="createTime" />
          <el-option label="价格最低" value="price" />
          <el-option label="价格最高" value="price" />
          <el-option label="最热门" value="viewCount" />
        </el-select>
        <el-select v-model="sortOrder" placeholder="顺序" @change="handleSearch">
          <el-option label="降序" value="desc" />
          <el-option label="升序" value="asc" />
        </el-select>
      </div>

      <div class="book-grid" v-if="books.length > 0">
        <div v-for="book in books" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
          <div class="book-cover">
            <el-image :src="book.coverImage || book.images?.[0] || '/placeholder.png'" fit="cover" />
          </div>
          <div class="book-info">
            <h3 class="text-ellipsis">{{ book.title }}</h3>
            <p class="condition">{{ book.condition }}</p>
            <p class="price">¥{{ book.price }}</p>
          </div>
        </div>
      </div>
      <el-empty v-else description="未找到相关书籍" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getBookList } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { getSearchHistory, clearSearchHistory, deleteSearchHistory, getHotSearch, addSearchHistory } from '@/api/search'
import { ElMessage } from 'element-plus'

const route = useRoute()
const keyword = ref(route.query.keyword || '')
const categoryId = ref('')
const condition = ref('')
const minPrice = ref(null)
const maxPrice = ref(null)
const sortBy = ref('createTime')
const sortOrder = ref('desc')
const books = ref([])
const searchHistory = ref([])
const hotSearch = ref([])
const categories = ref([])

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}
const handleSearch = async () => {
  try {
    if (keyword.value.trim()) {
      await addSearchHistory(keyword.value.trim())
    }
    const res = await getBookList({
      keyword: keyword.value,
      categoryId: categoryId.value || null,
      condition: condition.value || null,
      minPrice: minPrice.value || null,
      maxPrice: maxPrice.value || null,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    })
    books.value = res.data?.records || []
    loadSearchHistory()
    loadHotSearch()
  } catch (error) {
    console.error('Search failed:', error)
  }
}
const handleHotSearch = (item) => {
  keyword.value = item
  handleSearch()
}

const handleHistorySearch = (item) => {
  keyword.value = item
  handleSearch()
}

const handleDeleteHistory = async (item) => {
  try {
    await deleteSearchHistory(item)
    searchHistory.value = searchHistory.value.filter(h => h !== item)
  } catch (error) {
    console.error('Delete history failed:', error)
  }
}

const handleClearHistory = async () => {
  try {
    await clearSearchHistory()
    searchHistory.value = []
  } catch (error) {
    console.error('Clear history failed:', error)
  }
}

const loadSearchHistory = async () => {
  try {
    const res = await getSearchHistory()
    searchHistory.value = res.data || []
  } catch (error) {
    console.error('Failed to load search history:', error)
  }
}

const loadHotSearch = async () => {
  try {
    const res = await getHotSearch()
    hotSearch.value = res.data || []
  } catch (error) {
    console.error('Failed to load hot search:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadSearchHistory(),
    loadHotSearch(),
    handleSearch()
  ])
})
</script>

<style scoped lang="scss">
.search-page {
  padding: 20px 0;
}

.search-bar {
  margin-bottom: 20px;
}

.hot-search, .search-history {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  .label {
    font-size: 14px;
    color: #666;
  }

  .hot-tag, .history-tag {
    cursor: pointer;
  }
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.book-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.book-cover {
  height: 180px;
  background: #f5f5f5;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.book-info {
  padding: 12px;

  h3 {
    font-size: 14px;
    margin-bottom: 8px;
  }

  .condition {
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
  }

  .price {
    color: #ff4d4f;
    font-weight: 600;
  }
}
</style>
