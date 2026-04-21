package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.book.common.constants.Constants;
import com.campus.book.entity.Book;
import com.campus.book.entity.Cart;
import com.campus.book.mapper.BookMapper;
import com.campus.book.mapper.CartMapper;
import com.campus.book.service.CartService;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.BookVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Cart addToCart(Long bookId, Integer quantity) {
        Long userId = SecurityUtils.getCurrentUserId();

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!Constants.STATUS_ON_SALE.equals(book.getStatus())) {
            throw new RuntimeException("商品不可售");
        }
        if (book.getUserId().equals(userId)) {
            throw new RuntimeException("不能购买自己的商品");
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getBookId, bookId);
        Cart existingCart = cartMapper.selectOne(wrapper);

        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            cartMapper.updateById(existingCart);
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setBookId(bookId);
        cart.setBookTitle(book.getTitle());
        cart.setPrice(book.getPrice());
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
        return cart;
    }

    @Override
    public void updateCartQuantity(Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车商品不存在");
        }
        if (!cart.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权操作此购物车");
        }
        if (quantity <= 0) {
            cartMapper.deleteById(cartId);
        } else {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车商品不存在");
        }
        if (!cart.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权操作此购物车");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    @Override
    public List<BookVO> getCartItems() {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> carts = cartMapper.selectList(wrapper);

        List<BookVO> books = new ArrayList<>();
        for (Cart cart : carts) {
            Book book = bookMapper.selectById(cart.getBookId());
            if (book != null && Constants.STATUS_ON_SALE.equals(book.getStatus())) {
                BookVO vo = new BookVO();
                BeanUtils.copyProperties(book, vo);
                try {
                    vo.setImages(objectMapper.readValue(book.getImages(), new TypeReference<List<String>>() {}));
                } catch (Exception e) {
                    vo.setImages(new ArrayList<>());
                }
                books.add(vo);
            }
        }
        return books;
    }
}
