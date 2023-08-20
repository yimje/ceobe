package com.shiromi.ceobe.comment.service;

import com.shiromi.ceobe.comment.dto.CommentDTO;
import com.shiromi.ceobe.comment.entity.CommentEntity;
import com.shiromi.ceobe.comment.repository.CommentRepository;
import com.shiromi.ceobe.item.entity.ItemEntity;
import com.shiromi.ceobe.item.repository.ItemRepository;
import com.shiromi.ceobe.member.repository.MemberRepository;
import com.shiromi.ceobe.order.entity.OrderEntity;
import com.shiromi.ceobe.order.repository.OrderRepository;
import com.shiromi.ceobe.orderItem.entity.OrderItemEntity;
import com.shiromi.ceobe.orderItem.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Long save(CommentDTO commentDTO) {
        OrderEntity orderEntity = orderRepository.findById(commentDTO.getOrderId()).get();
        orderEntity.setReview("작성완료");
        OrderItemEntity orderItemEntity = orderItemRepository.findByOrderEntity(orderEntity);
        ItemEntity itemEntity = orderItemEntity.getItemEntity();
        CommentEntity commentEntity = CommentEntity.toCommentEntity(itemEntity, commentDTO);
        Long id = commentRepository.save(commentEntity).getId();
        return id;
    }

    @Transactional
    public Map<String,Object> findAll(Long itemId, Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        final int pageLimit = 5;
        ItemEntity itemEntity = itemRepository.findById(itemId).get();
        Page<CommentEntity> commentEntities = commentRepository.findByItemEntity(itemEntity, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
        Page<CommentDTO> commentDTOPage = commentEntities.map(
                comment -> new CommentDTO(
                        comment.getId(),
                        comment.getCommentWriter(),
                        comment.getCommentContents(),
                        comment.getCreatedTime(),
                        comment.getStarCount()
                ));
        return listPaging(pageable,commentDTOPage,itemId);
//        ItemEntity itemEntity = itemRepository.findById(itemId).get();
//        List<CommentEntity> commentEntities = itemEntity.getCommentEntityList();
//        List<CommentDTO> commentDTOList = new ArrayList<>();
//        for(CommentEntity commentEntity : commentEntities){
//            commentDTOList.add(CommentDTO.toCommentDTO(commentEntity));
//        }
//        return commentDTOList;
    }

    public List<CommentDTO> list() {
        List<CommentEntity> commentEntityList = commentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            commentDTOList.add(CommentDTO.toCommentDTO(commentEntity));
        }
        return commentDTOList;
    }

    public CommentDTO findById(Long id) {
        Optional<CommentEntity> optionalCommentEntity = commentRepository.findById(id);
        if(optionalCommentEntity.isPresent()){
            return CommentDTO.toCommentDTO(optionalCommentEntity.get());
        }else{
            return null;
        }
    }


    public CommentDTO update(Long id) {
        CommentEntity commentEntity = commentRepository.findById(id).get();
        CommentDTO commentDTO = CommentDTO.toCommentDTO(commentEntity);
        return commentDTO;
    }

    public void update2(CommentDTO commentDTO) {
        System.out.println("코멘트 서비스 넘어옴");
        System.out.println("commentDTO = " + commentDTO);
        CommentEntity commentEntity = commentRepository.findById(commentDTO.getId()).get();
        commentEntity.setCommentContents(commentDTO.getCommentContents());
        commentEntity.setStarCount(commentDTO.getStarCount());
        commentRepository.save(commentEntity);
    }

    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    //코멘트리스트 페이징 처리
    private Map<String,Object> listPaging (Pageable pageable, Page<CommentDTO> commentDTOList, Long itemId) {
        Map<String, Object> map = new HashMap<>();

        if (commentDTOList.getTotalPages() != 0) {
            map.put("commentList", commentDTOList);
            int blockLimit = 3;
            int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
            int endPage = Math.min((startPage + blockLimit - 1), commentDTOList.getTotalPages());
            map.put("startPage", startPage);
            map.put("endPage", endPage);
            map.put("itemId",itemId);
        }else {
            map.put("commentList", "empty");
        }
        return map;
    }


}
