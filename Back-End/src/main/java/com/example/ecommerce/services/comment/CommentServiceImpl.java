package com.example.ecommerce.services.comment;

import com.example.ecommerce.dtos.CommentDTO;
import com.example.ecommerce.exceptions.InvalidParamException;
import com.example.ecommerce.exceptions.ResourceNotFoundException;
import com.example.ecommerce.models.Comment;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.User;
import com.example.ecommerce.repositories.CommentRepository;
import com.example.ecommerce.repositories.OrderRepository;
import com.example.ecommerce.responses.CommentResponse;
import com.example.ecommerce.responses.PageResponse;
import com.example.ecommerce.responses.ProductResponse;
import com.example.ecommerce.responses.UserResponse;
import com.example.ecommerce.services.product.ProductService;
import com.example.ecommerce.services.user.UserService;
import com.example.ecommerce.utils.AuthUtil;
import com.example.ecommerce.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ProductService productService;
    private final AuthUtil authUtil;
    private final OrderRepository orderRepository;
    private final FileUtil fileUtil;

    @Override
    @Transactional
    public CommentResponse createComment(CommentDTO commentDTO) {
        authUtil.checkAuth(commentDTO.getUserId());
        if(commentDTO.getRate() > 5)
            throw new InvalidParamException("Rate phải từ 1 đến 5");
        Product product = productService.findById(commentDTO.getProductId());
        User user = userService.findById(commentDTO.getUserId());
        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .rate(commentDTO.getRate())
                .user(user)
                .isPurchased(orderRepository.existsByUser(user))
                .product(product)
                .date(LocalDateTime.now())
                .build();
        List<String> images = fileUtil.uploadFile(commentDTO.getImages());
        if(!images.isEmpty()){
            comment.setImage(String.join(";", images));
        }
        return CommentResponse.fromComment(commentRepository.save(comment),
                ProductResponse.fromProduct(product,0, null),
                UserResponse.fromUser(user));
    }

    @Override
    public PageResponse getAllCommentsByProduct(Long pid,Long rate, int page, int limit) {
        Product product = productService.findById(pid);
        page = page > 0 ? page - 1 : page;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Comment> commentPage = commentRepository.findAllByProductAndRate(product,rate, pageable);
        return PageResponse.builder()
                .page(page + 1)
                .limit(limit)
                .totalPage(commentPage.getTotalPages())
                .totalItem((int)commentPage.getTotalElements())
                .result(commentPage.stream().map(comment ->
                     CommentResponse.fromComment(comment,
                            ProductResponse.fromProduct(comment.getProduct(), 0,null),
                            UserResponse.fromUser(comment.getUser()))
                ).toList())
                .build();
    }

    @Override
    public CommentResponse getCommentById(long id) {
        Comment comment = findById(id);
        return CommentResponse.fromComment(comment,
                ProductResponse.fromProduct(comment.getProduct(), 0,null),
                UserResponse.fromUser(comment.getUser()));
    }

    @Override
    public Comment findById(long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id = " + id));
    }

    @Override
    public PageResponse getAllComments(int page, int limit) {
        page = page > 0 ? page - 1 : page;
        Pageable pageable = PageRequest.of(page, limit, Sort.by("id").descending());
        Page<Comment> commentPage = commentRepository.findAll(pageable);
        return PageResponse.builder()
                .page(page + 1)
                .limit(limit)
                .totalPage(commentPage.getTotalPages())
                .totalItem((int)commentPage.getTotalElements())
                .result(commentPage.stream().map(comment ->
                        CommentResponse.fromComment(comment,
                                ProductResponse.fromProduct(comment.getProduct(), 0,null),
                                UserResponse.fromUser(comment.getUser()))
                ).toList())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(long id) {
        Comment comment = findById(id);
        commentRepository.delete(comment);
    }
}
