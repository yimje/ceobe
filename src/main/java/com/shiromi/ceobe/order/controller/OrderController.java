package com.shiromi.ceobe.order.controller;

import com.shiromi.ceobe.item.dto.ItemDTO;
import com.shiromi.ceobe.member.dto.MemberDTO;
import com.shiromi.ceobe.order.dto.OrderDTO;
import com.shiromi.ceobe.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    //주문상세페이지 이동
    @GetMapping("/order/save")
    public String saveForm(@ModelAttribute ItemDTO itemDTO, Model model) {
        log.info("itemDTO = {}, model = {}",itemDTO, model);
        model.addAttribute("item", itemDTO);
        return "orderPages/orderSave";
    }
    //주문하기
    @PostMapping("/order/save")
    public String save(@ModelAttribute OrderDTO orderDTO, Model model) {
        System.out.println("시작orderDTO = " + orderDTO + ", model = " + model);
        orderService.save(orderDTO);
        model.addAttribute("order", orderDTO);
        System.out.println("저장후orderDTO = " + orderDTO + ", model = " + model);
        return "redirect:/";
    }
    @PostMapping("/order/save2")
    public @ResponseBody String save2(@RequestParam("cartList") JSONArray itemDTOList, Model model , HttpSession session) throws JSONException {
        Object member = session.getAttribute("member");
        member = (MemberDTO) member;
        String userId = ((MemberDTO) member).getUserId();
        System.out.println("userId11 = " + session.getAttribute("member"));
        System.out.println("userId22 = " + userId);
        System.out.println("itemDTOList2 = " + itemDTOList + ", model = " + model);
        orderService.save2(itemDTOList,userId);
        return "success";
    }
}