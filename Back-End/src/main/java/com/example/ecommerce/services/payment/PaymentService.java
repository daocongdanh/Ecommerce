package com.example.ecommerce.services.payment;

import com.example.ecommerce.dtos.OrderDTO;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.models.Order;
import com.example.ecommerce.responses.OrderResponse;
import com.example.ecommerce.responses.PaymentResponse;
import com.example.ecommerce.services.order.OrderService;
import com.example.ecommerce.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${payment.vnPay.url}")
    private String vnp_PayUrl;
    @Value("${payment.vnPay.returnUrl}")
    private String vnp_ReturnUrl;
    @Value("${payment.vnPay.tmnCode}")
    private String vnp_TmnCode ;
    @Value("${payment.vnPay.secretKey}")
    private String secretKey;
    @Value("${payment.vnPay.version}")
    private String vnp_Version;
    @Value("${payment.vnPay.command}")
    private String vnp_Command;
    @Value("${payment.vnPay.orderType}")
    private String orderType;

    private final OrderService orderService;

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", this.vnp_Version);
        vnpParamsMap.put("vnp_Command", this.vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", this.vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef",  VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" +  VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", this.orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnp_ExpireDate);
        return vnpParamsMap;
    }

    @Transactional
    public PaymentResponse createVnPayPayment(long price, String bankCode,
                                              HttpServletRequest request, OrderDTO orderDTO) {
        long amount = price * 100L;
        Map<String, String> vnpParamsMap = getVNPayConfig();
        OrderResponse orderResponse = orderService.createOrder(orderDTO);
        
        orderService.updateOrderStatus(orderResponse.getId(), OrderStatus.PROCESSING);

        vnpParamsMap.put("vnp_ReturnUrl", this.vnp_ReturnUrl + "?orderId=" + orderResponse.getId());
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(secretKey, hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;
        return PaymentResponse.builder()
                .code("00")
                .paymentUrl(paymentUrl)
                .build();
    }

//    private String buildQueryString(OrderDTO orderDTO) throws UnsupportedEncodingException {
//        StringBuilder queryString = new StringBuilder();
//        queryString.append(this.vnp_ReturnUrl).append("?")
//                .append("fullName=").append(URLEncoder.encode(orderDTO.getFullName(), StandardCharsets.UTF_8))
//                .append("&phone=").append(URLEncoder.encode(orderDTO.getPhone(), StandardCharsets.UTF_8))
//                .append("&email=").append(URLEncoder.encode(orderDTO.getEmail(), StandardCharsets.UTF_8))
//                .append("&address=").append(URLEncoder.encode(orderDTO.getAddress(), StandardCharsets.UTF_8))
//                .append("&userId=").append(orderDTO.getUserId())
//                .append("&paymentMethodId=").append(orderDTO.getPaymentMethodId());
//
//        String cartItemIds = orderDTO.getCartItemIds().stream()
//                .map(String::valueOf)
//                .collect(Collectors.joining(","));
//        queryString.append("&cartItemIds=").append(URLEncoder.encode(cartItemIds, StandardCharsets.UTF_8));
//
//        return queryString.toString();
//    }

    @Transactional
    public void handlePaymentFailed(HttpServletRequest request){
        long orderId = Long.parseLong(request.getParameter("orderId"));
        Order order = orderService.findById(orderId);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderService.save(order);
    }
}
