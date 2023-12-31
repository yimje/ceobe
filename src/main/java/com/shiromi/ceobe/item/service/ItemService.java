package com.shiromi.ceobe.item.service;

import com.shiromi.ceobe.comment.dto.CommentDTO;
import com.shiromi.ceobe.item.dto.ItemDTO;
import com.shiromi.ceobe.item.entity.ItemEntity;
import com.shiromi.ceobe.item.repository.ItemRepository;
import com.shiromi.ceobe.itemFile.entity.ItemFileEntity;
import com.shiromi.ceobe.itemFile.repository.ItemFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemFileRepository itemFileRepository;





    //저장
    public Long save(ItemDTO itemDTO) throws IOException {
        if (itemDTO.getItemFile().get(0).isEmpty()) {
            System.out.println("파일 없음");
            ItemEntity itemEntity = ItemEntity.toItemSaveEntity(itemDTO);
            return itemRepository.save(itemEntity).getId();
        } else {
            System.out.println("파일 있음");
            ItemEntity itemEntity = ItemEntity.toItemSaveFileEntity(itemDTO);
            Long savedId = itemRepository.save(itemEntity).getId();
            ItemEntity entity = itemRepository.findById(savedId).get();
            for (MultipartFile itemFile : itemDTO.getItemFile()) {
                String originalFileNameItem = itemFile.getOriginalFilename();
                String storedFileNameItem = System.currentTimeMillis() + "-" + originalFileNameItem;
                String savePath = "C:\\springboot_img_final\\" + storedFileNameItem;
                itemFile.transferTo(new File(savePath));
                ItemFileEntity itemFileEntity = ItemFileEntity.toSaveItemFileEntity(entity, originalFileNameItem, storedFileNameItem);
                itemFileRepository.save(itemFileEntity);
            }
            return savedId;
        }
    }
    //검색기능
    @Transactional
    public Map<String, Object> findAll(Pageable pageable, String sort, String search, String category) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = pageable.getPageSize();
        Page<ItemEntity> itemEntityList = itemRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, sort)));
        if (sort.equals("itemPrice")) {
            itemEntityList = itemRepository.findByItemNameContaining(search, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.ASC, sort)));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return listPaging(pageable,itemDTOList,sort,search,category);
        }
        if (category.equals("itemName")) {
            itemEntityList = itemRepository.findByItemNameContaining(search, PageRequest.of(page, pageLimit, Sort.by(sort).descending()));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return listPaging(pageable,itemDTOList,sort,search,category);
        }

        Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);

        return listPaging(pageable,itemDTOList,sort,search,category);
    }

    //이름으로 검색
    @Transactional
    public ItemDTO findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(IllegalAccessError::new)
                .toItemDTO();
    }
    //업데이트
    @Transactional
    public Long update(ItemDTO itemDTO) throws IOException {
        if (itemDTO.getItemFileUpdate().get(0).isEmpty()) {
            System.out.println("파일 없음");
            ItemEntity itemEntity = itemRepository.findById(itemDTO.getId()).get();
            itemEntity.setItemCount(itemDTO.getItemCount());
            itemEntity.setItemCategory(itemDTO.getItemCategory());
            itemEntity.setItemPrice(itemDTO.getItemPrice());
            itemEntity.setItemName(itemDTO.getItemName());
            itemEntity.setItemContents(itemDTO.getItemContents());
            Long id = itemEntity.getId();
            itemRepository.save(itemEntity);
            return id;

        } else {
            System.out.println("itemDTO = " + itemDTO);
            ItemEntity itemEntity2 = itemRepository.findById(itemDTO.getId()).get();
            System.out.println("아이템파일1" + itemEntity2);
            itemFileRepository.deleteByItemEntity(itemEntity2);

            ItemEntity itemEntity = ItemEntity.toItemUpdateEntity(itemDTO);
            Long savedId = itemRepository.save(itemEntity).getId();
            ItemEntity entity = itemRepository.findById(savedId).get();
            for (MultipartFile itemFileUpdate : itemDTO.getItemFileUpdate()) {
                String originalFileNameItem = itemFileUpdate.getOriginalFilename();
                String storedFileNameItem = System.currentTimeMillis() + "-" + originalFileNameItem;
                String savePath = "C:\\springboot_img_final\\" + storedFileNameItem;
//            itemFileUpdate.transferTo(new File(savePath));
                Path path = Paths.get(savePath).toAbsolutePath();
                itemFileUpdate.transferTo(path.toFile());

                ItemFileEntity itemFileEntity = ItemFileEntity.toSaveItemFileEntity(entity, originalFileNameItem, storedFileNameItem);
                itemFileRepository.save(itemFileEntity);

            }
            return savedId;
        }
    }
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional
    public ItemDTO findByOrderName(String orderName) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findByItemName(orderName);
        if (itemEntityOptional.isPresent()) {
            return ItemDTO.toItemDTO(itemEntityOptional.get());
        } else {
            return null;
        }
    }

    //리팩토링 대상 1순위
    @Transactional
    public Page<ItemDTO> findSmall(Pageable pageable, String sort, String search, String category) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = pageable.getPageSize();
        Page<ItemEntity> itemEntityList = itemRepository.findByItemCategory("small", PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, sort)));
        if (sort.equals("itemPrice")) {
            itemEntityList = itemRepository.findByItemNameContainingAndItemCategory(search, "small", PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.ASC, sort)));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }
        if (category.equals("itemName")) {
            itemEntityList = itemRepository.findByItemNameContainingAndItemCategory(search, "small", PageRequest.of(page, pageLimit, Sort.by(sort).descending()));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }

        Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
        return itemDTOList;
    }


    @Transactional
    public Page<ItemDTO> findMedium(Pageable pageable, String sort, String search, String category) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = pageable.getPageSize();
        Page<ItemEntity> itemEntityList = itemRepository.findByItemCategory("middle", PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, sort)));
        if (sort.equals("itemPrice")) {
            System.out.println("1번");
            itemEntityList = itemRepository.findByItemCategoryAndItemNameLike("middle",search,PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.ASC, sort)));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }
        if (category.equals("itemName")) {
            System.out.println("2번");
            itemEntityList = itemRepository.findByItemCategoryAndItemNameLike("middle",search, PageRequest.of(page, pageLimit, Sort.by(sort).descending()));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;

        }
        System.out.println("3번");
        Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
        return itemDTOList;
    }


    @Transactional
    public Page<ItemDTO> findLarge(Pageable pageable, String sort, String search, String category) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = pageable.getPageSize();
        Page<ItemEntity> itemEntityList = itemRepository.findByItemCategory("large", PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, sort)));
        if (sort.equals("itemPrice")) {
            itemEntityList = itemRepository.findByItemNameContainingAndItemCategory(search,"large",PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.ASC, sort)));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }
        if (category.equals("itemName")) {
            itemEntityList = itemRepository.findByItemNameContainingAndItemCategory(search,"large", PageRequest.of(page, pageLimit, Sort.by(sort).descending()));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }

        Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
        return itemDTOList;
    }

    private Map<String,Object> listPaging (Pageable pageable, Page<ItemDTO> itemDTOList, String sort, String search, String category) {
        Map<String, Object> map = new HashMap<>();
        int blockLimit = 3;
        int startPage = (((int) (Math.ceil((double) pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), itemDTOList.getTotalPages());
        map.put("startPage", startPage);
        map.put("endPage", endPage);
        map.put("sort", sort);
        map.put("size", pageable.getPageSize());
        map.put("page", pageable.getPageNumber());
        map.put("search", search);
        map.put("category", category);
        if(itemDTOList.getTotalElements() == 0){
            map.put("message","null");
        }
        map.put("itemList",itemDTOList);

        return map;
    }

}
