package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.dto.BookDTO;
import com.campus.book.entity.*;
import com.campus.book.mapper.*;
import com.campus.book.service.BookService;
import com.campus.book.util.LogUtils;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.BookVO;
import com.campus.book.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public Page<BookVO> getFeedBooks(Long userId) {
        List<String> preferenceTags = getUserPreferenceTags(userId);

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.STATUS_ON_SALE)
                .orderByDesc(Book::getCreateTime)
                .last("LIMIT 50");
        List<Book> books = bookMapper.selectList(wrapper);

        List<BookVO> voList = books.stream()
                .map(book -> {
                    BookVO vo = convertToVO(book);
                    double score = calculatePersonalizedScore(book, preferenceTags);
                    vo.setSortScore(score);
                    return vo;
                })
                .sorted((a, b) -> Double.compare(b.getSortScore(), a.getSortScore()))
                .limit(20)
                .collect(Collectors.toList());

        Page<BookVO> voPage = new Page<>();
        voPage.setRecords(voList);
        return voPage;
    }
    @Override
    public BookVO publish(BookDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("发布图书: userId={}, title={}", userId, dto.getTitle());
        
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setOriginalPrice(dto.getOriginalPrice());
        book.setCategoryId(dto.getCategoryId());
        book.setCondition(dto.getCondition());
        book.setUserId(userId);

        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category != null) {
            book.setCategoryName(category.getName());
        }

        User user = userMapper.selectById(userId);
        book.setSellerNickname(user.getNickname());

        try {
            book.setImages(objectMapper.writeValueAsString(dto.getImages()));
        } catch (JsonProcessingException e) {
            LogUtils.logError(logger, "序列化图书图片失败", e);
            book.setImages("[]");
        }

        try {
            book.setTags(objectMapper.writeValueAsString(dto.getTags()));
        } catch (JsonProcessingException e) {
            LogUtils.logError(logger, "序列化图书标签失败", e);
            book.setTags("[]");
        }

        book.setStatus(Constants.STATUS_PENDING);
        book.setViewCount(0);
        book.setFavoriteCount(0);
        book.setIsRecommended(0);

        bookMapper.insert(book);
        
        LogUtils.logBusiness(logger, userId, "发布", "图书", 
            String.format("书名: %s, 价格: %.2f", dto.getTitle(), dto.getPrice()));
        logger.info("图书发布成功: bookId={}, title={}", book.getId(), book.getTitle());
        
        return convertToVO(book);
    }

    @Override
    public BookVO updateBook(Long id, BookDTO dto) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!book.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权修改此商品");
        }

        book.setTitle(dto.getTitle());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setOriginalPrice(dto.getOriginalPrice());
        book.setCategoryId(dto.getCategoryId());
        book.setCondition(dto.getCondition());

        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category != null) {
            book.setCategoryName(category.getName());
        }

        try {
            book.setImages(objectMapper.writeValueAsString(dto.getImages()));
        } catch (JsonProcessingException e) {
            book.setImages("[]");
        }

        try {
            book.setTags(objectMapper.writeValueAsString(dto.getTags()));
        } catch (JsonProcessingException e) {
            book.setTags("[]");
        }

        if (!SecurityUtils.isAdmin()) {
            book.setStatus(Constants.STATUS_PENDING);
        }

        bookMapper.updateById(book);
        return convertToVO(book);
    }

    @Override
    public void deleteBook(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("删除图书请求: userId={}, bookId={}", userId, id);
        
        Book book = bookMapper.selectById(id);
        if (book == null) {
            logger.warn("删除失败: 图书不存在 - bookId={}", id);
            throw new RuntimeException("商品不存在");
        }
        if (!book.getUserId().equals(userId) && !SecurityUtils.isAdmin()) {
            logger.warn("删除失败: 无权删除 - bookId={}, userId={}, bookOwnerId={}", 
                id, userId, book.getUserId());
            throw new RuntimeException("无权删除此商品");
        }
        
        bookMapper.deleteById(id);
        
        LogUtils.logBusiness(logger, userId, "删除", "图书", 
            String.format("图书ID: %d, 书名: %s", id, book.getTitle()));
        logger.info("图书删除成功: bookId={}, title={}", id, book.getTitle());
    }

    @Override
    public BookVO getBookById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("商品不存在");
        }

        Long currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
        }
        boolean isAdmin = false;
        try {
            isAdmin = SecurityUtils.isAdmin();
        } catch (Exception e) {
        }

        boolean canAccess = Constants.STATUS_ON_SALE.equals(book.getStatus())
                || Constants.STATUS_SOLD.equals(book.getStatus())
                || Constants.STATUS_RESERVED.equals(book.getStatus())
                || isAdmin
                || (currentUserId != null && currentUserId.equals(book.getUserId()));
        if (!canAccess) {
            throw new RuntimeException("商品正在审核中");
        }

        return convertToVO(book);
    }

    @Override
    public Page<BookVO> getBookList(Integer pageNum, Integer pageSize, Long categoryId, String condition,
                                    String keyword, Integer minPrice, Integer maxPrice, String sortBy, String sortOrder) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Book::getStatus, Constants.STATUS_ON_SALE);

        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(condition)) {
            wrapper.eq(Book::getCondition, condition);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getDescription, keyword)
                    .or().like(Book::getSellerNickname, keyword));
        }
        if (minPrice != null) {
            wrapper.ge(Book::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Book::getPrice, maxPrice);
        }

        if ("price".equals(sortBy)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Book::getPrice);
            } else {
                wrapper.orderByDesc(Book::getPrice);
            }
        } else if ("createTime".equals(sortBy)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Book::getCreateTime);
            } else {
                wrapper.orderByDesc(Book::getCreateTime);
            }
        } else if ("viewCount".equals(sortBy)) {
            if ("asc".equals(sortOrder)) {
                wrapper.orderByAsc(Book::getViewCount);
            } else {
                wrapper.orderByDesc(Book::getViewCount);
            }
        } else {
            wrapper.orderByDesc(Book::getCreateTime);
        }

        Page<Book> bookPage = bookMapper.selectPage(page, wrapper);
        Page<BookVO> voPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        voPage.setRecords(bookPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<BookVO> getHomeBooks() {
        List<BookVO> hotBooks = getHotBooks();
        List<BookVO> latestBooks = getLatestBooks();

        Map<String, Object> homeData = new HashMap<>();
        homeData.put("hotBooks", hotBooks);
        homeData.put("latestBooks", latestBooks);

        Page<BookVO> voPage = new Page<>();
        voPage.setRecords(hotBooks);
        return voPage;
    }

    private List<String> getUserPreferenceTags(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        Set<String> tagSet = new HashSet<>();

        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId);
        List<Favorite> favorites = favoriteMapper.selectList(favWrapper);

        for (Favorite fav : favorites) {
            Book book = bookMapper.selectById(fav.getBookId());
            if (book != null && book.getTags() != null) {
                try {
                    List<String> tags = objectMapper.readValue(book.getTags(), new TypeReference<List<String>>() {});
                    tagSet.addAll(tags);
                } catch (Exception e) {
                }
            }
        }

        List<Long> orderIds = getUserOrderIds(userId);
        if (orderIds != null && !orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> orderItems = orderItemMapper.selectList(orderWrapper);

            for (OrderItem item : orderItems) {
                Book book = bookMapper.selectById(item.getBookId());
                if (book != null && book.getTags() != null) {
                    try {
                        List<String> tags = objectMapper.readValue(book.getTags(), new TypeReference<List<String>>() {});
                        tagSet.addAll(tags);
                    } catch (Exception e) {
                    }
                }
            }
        }

        return new ArrayList<>(tagSet);
    }

    private List<Long> getUserOrderIds(Long userId) {
        LambdaQueryWrapper<com.campus.book.entity.Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(com.campus.book.entity.Order::getUserId, userId);
        List<com.campus.book.entity.Order> orders = orderMapper.selectList(orderWrapper);
        return orders.stream().map(com.campus.book.entity.Order::getId).collect(Collectors.toList());
    }

    private double calculatePersonalizedScore(Book book, List<String> preferenceTags) {
        double score = 0.0;

        long createTimeScore = (System.currentTimeMillis() - book.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) / (1000 * 60 * 60);

        int viewWeight = 2;
        int favoriteWeight = 5;
        int tagWeight = 10;

        if (book.getViewCount() != null) {
            score += book.getViewCount() * viewWeight;
        }
        if (book.getFavoriteCount() != null) {
            score += book.getFavoriteCount() * favoriteWeight;
        }

        if (book.getTags() != null && preferenceTags != null && !preferenceTags.isEmpty()) {
            try {
                List<String> bookTags = objectMapper.readValue(book.getTags(), new TypeReference<List<String>>() {});
                for (String tag : bookTags) {
                    if (preferenceTags.contains(tag)) {
                        score += tagWeight;
                    }
                }
            } catch (Exception e) {
            }
        }

        return score;
    }

    @Override
    public List<BookVO> getHotBooks() {
        String cacheKey = Constants.BOOK_CACHE_KEY + "hot";
        List<BookVO> cachedBooks = (List<BookVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedBooks != null) {
            return cachedBooks;
        }

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.STATUS_ON_SALE)
                .orderByDesc(Book::getViewCount, Book::getFavoriteCount)
                .last("LIMIT 10");
        List<Book> books = bookMapper.selectList(wrapper);
        List<BookVO> voList = books.stream().map(this::convertToVO).collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, voList);
        return voList;
    }

    @Override
    public List<BookVO> getLatestBooks() {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.STATUS_ON_SALE)
                .orderByDesc(Book::getCreateTime)
                .last("LIMIT 10");
        List<Book> books = bookMapper.selectList(wrapper);
        return books.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void updateBookStatus(Long id, String status) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("商品不存在");
        }

        boolean isAdmin = SecurityUtils.isAdmin();
        boolean isOwner = book.getUserId().equals(SecurityUtils.getCurrentUserId());
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("无权修改此商品状态");
        }

        if (!isAdmin && !Constants.STATUS_OFFLINE.equals(status)) {
            throw new RuntimeException("仅管理员可变更为该状态");
        }

        book.setStatus(status);
        bookMapper.updateById(book);
    }

    @Override
    public Page<Book> getBookPage(Page<Book> page, String status, String keyword) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Book::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getDescription, keyword)
                    .or().like(Book::getSellerNickname, keyword));
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<BookVO> getMyBooks(Integer pageNum, Integer pageSize) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(Book::getCreateTime);
        Page<Book> bookPage = bookMapper.selectPage(page, wrapper);

        Page<BookVO> voPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        voPage.setRecords(bookPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void incrementViewCount(Long id) {
        Book book = bookMapper.selectById(id);
        if (book != null) {
            book.setViewCount(book.getViewCount() + 1);
            bookMapper.updateById(book);
        }
    }

    @Override
    public Map<String, Object> getHomeData() {
        Long userId = null;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
        }

        Map<String, Object> data = new HashMap<>();
        data.put("hotBooks", getHotBooks());
        data.put("latestBooks", getLatestBooks());
        data.put("feedBooks", getFeedBooks(userId).getRecords());
        return data;
    }

    private BookVO convertToVO(Book book) {
        BookVO vo = new BookVO();
        BeanUtils.copyProperties(book, vo);

        try {
            vo.setImages(objectMapper.readValue(book.getImages(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            vo.setImages(new ArrayList<>());
        }

        try {
            vo.setTags(objectMapper.readValue(book.getTags(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            vo.setTags(new ArrayList<>());
        }

        Long currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
        }

        if (currentUserId != null) {
            Long finalCurrentUserId = currentUserId;
            LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
            favWrapper.eq(Favorite::getUserId, finalCurrentUserId).eq(Favorite::getBookId, book.getId());
            vo.setIsFavorite(favoriteMapper.selectCount(favWrapper) > 0);
        } else {
            vo.setIsFavorite(false);
        }

        User seller = userMapper.selectById(book.getUserId());
        if (seller != null) {
            vo.setSellerAvatar(seller.getAvatar());
            vo.setSellerCreditScore(seller.getCreditScore());
        }
        return vo;
    }
}
