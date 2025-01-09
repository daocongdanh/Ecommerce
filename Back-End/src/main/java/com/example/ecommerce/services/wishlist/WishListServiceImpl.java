package com.example.ecommerce.services.wishlist;

import com.example.ecommerce.dtos.WishListDTO;
import com.example.ecommerce.exceptions.InvalidParamException;
import com.example.ecommerce.exceptions.ResourceNotFoundException;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.WishList;
import com.example.ecommerce.repositories.WishListRepository;
import com.example.ecommerce.responses.WishListResponse;
import com.example.ecommerce.services.product.ProductService;
import com.example.ecommerce.services.user.UserService;
import com.example.ecommerce.services.wishlist.WishListService;
import com.example.ecommerce.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {
    private final WishListRepository wishListRepository;
    private final UserService userService;
    private final ProductService productService;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public WishListResponse createWishList(WishListDTO wishListDTO) {
        authUtil.checkAuth(wishListDTO.getUserId());
        User user = userService.findById(wishListDTO.getUserId());
        Product product = productService.findById(wishListDTO.getProductId());
        if(wishListRepository.existsByProductAndUser(product, user))
            throw new InvalidParamException("Product already exists with id = " + product.getId());
        WishList wishList = WishList.builder()
                .user(user)
                .product(product)
                .build();
        return WishListResponse.fromWishList(wishListRepository.save(wishList));
    }

    @Override
    public List<WishListResponse> getAllWishListsByUser(long uid) {
        User user = userService.findById(uid);
        return wishListRepository.findAllByUser(user)
                .stream()
                .map(WishListResponse::fromWishList)
                .toList();
    }

    @Override
    @Transactional
    public void deleteOne(long productId) {
        Product product = productService.findById(productId);
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        WishList wishList = wishListRepository.findByProductAndUser(product,user)
                .orElseThrow(() -> new ResourceNotFoundException("WishList not found"));
        authUtil.checkAuth(wishList.getUser().getId());
        wishListRepository.delete(wishList);
    }

    @Override
    @Transactional
    public void deleteAll(long uid) {
        User user = userService.findById(uid);
        List<WishList> wishLists = wishListRepository.findAllByUser(user);
        wishListRepository.deleteAll(wishLists);
    }
}
