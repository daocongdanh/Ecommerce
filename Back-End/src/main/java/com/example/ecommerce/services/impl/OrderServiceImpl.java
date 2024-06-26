package com.example.ecommerce.services.impl;

import com.example.ecommerce.dtos.OrderDTO;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.exceptions.ResourceNotFoundException;
import com.example.ecommerce.models.*;
import com.example.ecommerce.repositories.*;
import com.example.ecommerce.responses.OrderResponse;
import com.example.ecommerce.responses.PageResponse;
import com.example.ecommerce.services.EmailService;
import com.example.ecommerce.services.OrderService;
import com.example.ecommerce.utils.EmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartItemRepository cartItemRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<CartItem> cartItems = cartItemRepository.findAllByCart(cartRepository.findByUser(user).get());
        Double totalPrice = cartItems.stream()
                .mapToDouble(item -> {
                    Product p = item.getProduct();
                    return item.getQuantity() * p.getPrice() * (1 - (double) p.getDiscount() /100);
                }).sum();
        Order order = Order.builder()
                .fullName(orderDTO.getFullName())
                .phone(orderDTO.getPhone())
                .email(orderDTO.getEmail())
                .address(orderDTO.getAddress())
                .orderDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .orderStatus(OrderStatus.PENDING)
                .active(true)
                .user(user)
                .build();
        orderRepository.save(order);
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItem cartItem : cartItems){
            OrderDetail orderDetail = OrderDetail.builder()
                    .quantity(cartItem.getQuantity())
                    .product(cartItem.getProduct())
                    .order(order)
                    .price(cartItem.getQuantity() * cartItem.getProduct().getPrice()
                            * (1 - (double)cartItem.getProduct().getDiscount()/100))
                    .build();
            orderDetails.add(orderDetailRepository.save(orderDetail));
        }
        cartItemRepository.deleteAll(cartItems);

        // Send mail
        String toMail = order.getEmail();
        String subject = "Cellphones Thông báo xác nhận quý khách đã đặt hàng thành công #" + order.getId();
        EmailTemplate emailTemplate = new EmailTemplate(order, orderDetails);
        String body = emailTemplate.body();
        emailService.sendEmail(toMail,subject,body);
        return OrderResponse.fromOrder(order);
    }

    @Override
    public PageResponse getAllOrders(int page, int limit) {
        page = page > 0 ? page - 1 : page;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return PageResponse.builder()
                .page(page + 1)
                .limit(limit)
                .totalPage(orderPage.getTotalPages())
                .result(orderPage.stream().map(OrderResponse::fromOrder).toList())
                .build();
    }

    @Override
    public OrderResponse getOrderById(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderResponse.fromOrder(order);
    }

    @Override
    public PageResponse getOrderByUser(long uid, int page, int limit) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        page = page > 0 ? page - 1 : page;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Order> orderPage = orderRepository.findAllByUser(user, pageable);
        return PageResponse.builder()
                .page(page + 1)
                .limit(limit)
                .totalPage(orderPage.getTotalPages())
                .result(orderPage.stream().map(OrderResponse::fromOrder).toList())
                .build();
    }

    @Override
    public OrderResponse updateOrderStatus(long id, OrderStatus orderStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        return OrderResponse.fromOrder(order);
    }

    @Override
    public void deleteOrder(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setActive(false);
        orderRepository.save(order);
    }
}
