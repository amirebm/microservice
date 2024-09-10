package com.microservice.order_service.service;

import com.microservice.order_service.dto.InventoryResponse;
import com.microservice.order_service.dto.OrderLineItemsDto;
import com.microservice.order_service.dto.OrderRequest;
import com.microservice.order_service.event.OrderPlacedEvent;
import com.microservice.order_service.model.Order;
import com.microservice.order_service.model.OrderLineItems;
import com.microservice.order_service.repository.OrderRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemList(orderLineItems);


        List<String> skuCodes = order.getOrderLineItemList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //call inventory service and place order if the product is in stock
        InventoryResponse[] inventoryResponseArray= webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

       boolean allProductsInStock= Arrays.stream(inventoryResponseArray)
               .allMatch(InventoryResponse::isInStock);
        if (allProductsInStock) {
            orderRepository.save(order);
            OrderPlacedEvent orderPlacedEvent=new OrderPlacedEvent(order.getOrderNumber());
            log.info("start - Sending OrderPlacedEvent {} to Kafka topic order placed",orderPlacedEvent);
            kafkaTemplate.send("order placed",orderPlacedEvent);
            log.info("end - Sending OrderPlacedEvent {} to Kafka topic order placed",orderPlacedEvent);

            return "order placed successfully";
            //Send the message to kafka topic

        } else {
                throw new IllegalArgumentException("Product is not in stock, please try again later");
            }       }
    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems=new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
