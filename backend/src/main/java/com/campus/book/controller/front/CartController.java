package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.dto.CartDTO;
import com.campus.book.entity.Cart;
import com.campus.book.service.CartService;
import com.campus.book.vo.BookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<BookVO>> getCartItems() {
        return Result.success(cartService.getCartItems());
    }

    @PostMapping
    public Result<Cart> addToCart(@Validated @RequestBody CartDTO dto) {
        return Result.success(cartService.addToCart(dto.getBookId(), dto.getQuantity()));
    }

    @PutMapping("/{id}")
    public Result<Void> updateCartQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateCartQuantity(id, quantity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> clearCart() {
        cartService.clearCart();
        return Result.success();
    }
}
