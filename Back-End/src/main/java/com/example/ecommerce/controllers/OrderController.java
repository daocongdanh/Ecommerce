package com.example.ecommerce.controllers;

import com.example.ecommerce.dtos.OrderDTO;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.responses.CommentResponse;
import com.example.ecommerce.responses.OrderResponse;
import com.example.ecommerce.responses.PageResponse;
import com.example.ecommerce.responses.ResponseSuccess;
import com.example.ecommerce.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> createOrder(@Valid @RequestBody OrderDTO orderDTO){
        OrderResponse orderResponse = orderService.createOrder(orderDTO);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Create order successfully")
                .status(HttpStatus.CREATED.value())
                .data(orderResponse)
                .build());
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> getAllOrders(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "5") int limit){
        PageResponse pageResponse = orderService.getAllOrders(page, limit);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Get all orders information successfully")
                .status(HttpStatus.OK.value())
                .data(pageResponse)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseSuccess> getOrderById(@PathVariable long id){
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Get order information successfully")
                .status(HttpStatus.OK.value())
                .data(orderResponse)
                .build());
    }

    @GetMapping("/user/{uid}")
    @PreAuthorize("#uid == authentication.principal.id or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> getOrderByUser(@PathVariable long uid,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "5") int limit){
        PageResponse pageResponse = orderService.getOrderByUser(uid, page, limit);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Get all orders by user information successfully")
                .status(HttpStatus.OK.value())
                .data(pageResponse)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> deleteOrder(@PathVariable long id){
        orderService.deleteOrder(id);
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Delete order successfully")
                .status(HttpStatus.NO_CONTENT.value())
                .build());
    }


    @PutMapping("/update-status/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseSuccess> updateOrderStatus(@PathVariable long id,
                                                             @RequestParam String status){
        return ResponseEntity.ok().body(ResponseSuccess.builder()
                .message("Update order status successfully")
                .status(HttpStatus.OK.value())
                .data(orderService.updateOrderStatus(id, OrderStatus.valueOf(status.toUpperCase())))
                .build());

    }
}
