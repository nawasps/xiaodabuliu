package com.campus.book.service;

import com.campus.book.entity.Cart;
import com.campus.book.vo.BookVO;

import java.util.List;

public interface CartService {

    Cart addToCart(Long bookId, Integer quantity);

    void updateCartQuantity(Long cartId, Integer quantity);

    void removeFromCart(Long cartId);

    void clearCart();

    List<BookVO> getCartItems();
}
