package com.shiromi.ceobe.item.service;

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
    public Page<ItemDTO> findAll(Pageable pageable, String sort, String search, String category) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = pageable.getPageSize();
        Page<ItemEntity> itemEntityList = itemRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, sort)));
        if (sort.equals("itemPrice")) {
            itemEntityList = itemRepository.findByItemNameContaining(search, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.ASC, sort)));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }
        if (category.equals("itemName")) {
            itemEntityList = itemRepository.findByItemNameContaining(search, PageRequest.of(page, pageLimit, Sort.by(sort).descending()));
            Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
            return itemDTOList;
        }

        Page<ItemDTO> itemDTOList = itemEntityList.map(ItemDTO::toItemDTO);
        return itemDTOList;
    }

    //이름으로 검색
    @Transactional
    public ItemDTO findById(Long id) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(id);
        if (itemEntityOptional.isPresent()) {
            return ItemDTO.toItemDTO(itemEntityOptional.get());
        } else {
            return null;
        }
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
}