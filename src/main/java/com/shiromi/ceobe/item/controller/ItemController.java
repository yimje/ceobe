package com.shiromi.ceobe.item.controller;

import com.shiromi.ceobe.comment.service.CommentService;
import com.shiromi.ceobe.item.dto.ItemDTO;
import com.shiromi.ceobe.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    //상품 저장 화면
    @GetMapping("/item/save")
    public String saveForm() {
        return "itemPages/itemSave";
    }
    //상품 저장처리
    @PostMapping("/item/save")
    public String save(@ModelAttribute ItemDTO itemDTO) throws IOException {
        itemService.save(itemDTO);
        return "redirect:/item/main";
    }
    //상품 메인
    @GetMapping("/item/main")
    public String findAll(@PageableDefault(page = 1,size = 5) Pageable pageable, Model model ,
                          @RequestParam(required = false , value = "sort", defaultValue = "id") String sort,
                          @RequestParam(required = false , value = "search", defaultValue = "") String search,
                          @RequestParam(required = false , value = "category", defaultValue = "") String category){
        Map<String, Object> map = itemService.findAll(pageable, sort, search , category);
        model.addAllAttributes(map);
        return "itemPages/itemMain";
    }

    //상품 상세조회
    @GetMapping("/item/")
    public String findById(@PageableDefault(page = 1) Pageable pageable,
                           @RequestParam("itemId") Long itemId, Model model){
        log.info("itemId : {} ", itemId);
        model.addAttribute("item", itemService.findById(itemId));
        model.addAttribute("commentListPage",commentService.findAll(itemId,pageable));
        return "itemPages/itemDetail";
    }

    //상품 수정 화면
    @GetMapping("/item/update/{id}")
    public String updateForm(@PathVariable Long id, Model model){
        ItemDTO itemDTO = itemService.findById(id);
        model.addAttribute("item",itemDTO);
        return "itemPages/itemUpdate";
    }

    //상품 수정 처리
    @PostMapping("/item/update")
    public String update (@ModelAttribute ItemDTO itemDTO,Model model) throws IOException{
        System.out.println("itemDTO = " + itemDTO + ", model = " + model);
        itemService.update(itemDTO);
        ItemDTO itemDTO1 = itemService.findById(itemDTO.getId());
        model.addAttribute("board",itemDTO1);
        return "redirect:/item/main";
    }

    //상품 삭제
    @GetMapping("/item/delete/{id}")
    public String delete(@PathVariable Long id){
        itemService.delete(id);
        return"redirect:/item/main";
    }

    //주문확인에서 상품 이름으로 상세조회
    @GetMapping("/items")
    public String findById(@RequestParam("orderName") String orderName, Model model){
        System.out.println(" orderName = " + orderName + ", model = " + model);
        ItemDTO itemDTO = itemService.findByOrderName(orderName);
        model.addAttribute("item",itemDTO);
        return "itemPages/itemDetail";
    }

    //리팩토링 1순위
    //카테고리별로 띄우기
    @GetMapping("/item/category1")
    public String findSmall(@PageableDefault(page = 1,size = 5)Pageable pageable, Model model ,
                            @RequestParam(required = false , value = "sort", defaultValue = "id") String sort,
                            @RequestParam(required = false , value = "search", defaultValue = "") String search,
                            @RequestParam(required = false , value = "category", defaultValue = "") String category){
        Page<ItemDTO> itemDTOList = itemService.findSmall(pageable, sort, search , category);
        if(itemDTOList.getTotalElements() == 0){
            model.addAttribute("message","null");
        }
        model.addAttribute("itemList",itemDTOList);
        int blockLimit = 3;
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = ((startPage + blockLimit - 1) < itemDTOList.getTotalPages()) ? startPage + blockLimit - 1 : itemDTOList.getTotalPages();
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        return "itemPages/itemCtry1";
    }

    @GetMapping("/item/category2")
    public String findMedium(@PageableDefault(page = 1,size = 5)Pageable pageable, Model model ,
                             @RequestParam(required = false , value = "sort", defaultValue = "id") String sort,
                             @RequestParam(required = false , value = "search", defaultValue = "") String search,
                             @RequestParam(required = false , value = "category", defaultValue = "") String category) {
        System.out.println("pageable = " + pageable + ", model = " + model + ", sort = " + sort + ", search = " + search + ", category = " + category);
        Page<ItemDTO> itemDTOList = itemService.findMedium(pageable, sort, search, category);
        if (itemDTOList.getTotalElements() == 0) {
            model.addAttribute("message", "null");
        }
        model.addAttribute("itemList", itemDTOList);
        int blockLimit = 3;
        int startPage = (((int) (Math.ceil((double) pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = ((startPage + blockLimit - 1) < itemDTOList.getTotalPages()) ? startPage + blockLimit - 1 : itemDTOList.getTotalPages();
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        return "itemPages/itemCtry2";
    }

    @GetMapping("/item/category3")
    public String findLarge(@PageableDefault(page = 1,size = 5)Pageable pageable, Model model ,
                            @RequestParam(required = false , value = "sort", defaultValue = "id") String sort,
                            @RequestParam(required = false , value = "search", defaultValue = "") String search,
                            @RequestParam(required = false , value = "category", defaultValue = "") String category){
        Page<ItemDTO> itemDTOList = itemService.findLarge(pageable, sort, search , category);
        if(itemDTOList.getTotalElements() == 0){
            model.addAttribute("message","null");
        }
        model.addAttribute("itemList",itemDTOList);

        int blockLimit = 3;
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), itemDTOList.getTotalPages());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("search", search);
        model.addAttribute("category", category);

        return "itemPages/itemCtry3";
    }
}
