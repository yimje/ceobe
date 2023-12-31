package com.shiromi.ceobe.cart.controller;

import com.shiromi.ceobe.cart.dto.CartDTO;
import com.shiromi.ceobe.cart.service.CartService;
import com.shiromi.ceobe.cartItem.dto.CartItemDTO;
import com.shiromi.ceobe.item.dto.*;
import com.shiromi.ceobe.member.dto.MemberDTO;
import com.shiromi.config.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.net.HttpCookie;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    //장바구니 저장
//    @GetMapping("/save")
//    public String saveForm(@ModelAttribute ItemDTO itemDTO, Model model) {
//        String userId = itemDTO.getUserId();
//        cartService.save(itemDTO);
//        model.addAttribute("item", itemDTO);
//        return "redirect:/cart/list?userId=" + userId;
//    }
    @GetMapping("/saved")
    public @ResponseBody String cartSave(@ModelAttribute ItemDTO itemDTO , @AuthenticationPrincipal PrincipalDetails principalDetails){
        String userId = cartService.save(itemDTO, principalDetails.getMemberEntity());
        return userId;
    }
    //장바구니 리스트
    @GetMapping("/list")
    public String listForm(@RequestParam("userId") String userId, Model model) {
        List<CartItemDTO> cartItemDTOList = cartService.findAll(userId);
        model.addAttribute("cartList", cartItemDTOList);
        return "cartPages/cartList";
    }

    //장바구니 상세페이지
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model){
        CartItemDTO cartDTO = cartService.findById(id);
        model.addAttribute("cart",cartDTO);
        return "cartPages/cartList";
    }

    //장바구니 삭제
    @GetMapping("/delete")
    public @ResponseBody String delete(@RequestParam("cartId")Long id){
        cartService.delete(id);
        return "success";
    }

    //장바구니 수정
//    @GetMapping("/update")
//    public String update(@ModelAttribute CartDTO cartDTO){
//        System.out.println("cartDTO = " + cartDTO);
//        cartService.update(cartDTO);
//        return "redirect:/cart/list?userId="+cartDTO.getUserId();
//    }

    //장바구니 수량변경 ajax
    @GetMapping("/change")
    public @ResponseBody String change(@ModelAttribute CartDTO cartDTO){
        System.out.println("cartDTO = " + cartDTO);
        cartService.update(cartDTO);
        return "success";
    }

}