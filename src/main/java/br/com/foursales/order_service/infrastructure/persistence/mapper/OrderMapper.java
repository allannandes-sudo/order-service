package br.com.foursales.order_service.infrastructure.persistence.mapper;

import br.com.foursales.order_service.domain.model.Order;
import br.com.foursales.order_service.domain.model.OrderItem;
import br.com.foursales.order_service.infrastructure.persistence.entity.OrderEntity;
import br.com.foursales.order_service.infrastructure.persistence.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "in.id")
    @Mapping(target = "userId", source = "in.userId")
    @Mapping(target = "items", source = "in.items")
    @Mapping(target = "status", source = "in.status")
    @Mapping(target = "totalAmount", source = "in.totalAmount")
    @Mapping(target = "createdAt", source = "in.createdAt")
    OrderEntity toEntity(Order in);

    @Mapping(target = "id", source = "orderEntity.id")
    @Mapping(target = "userId", source = "orderEntity.userId")
    @Mapping(target = "items", source = "orderEntity.items")
    @Mapping(target = "status", source = "orderEntity.status")
    @Mapping(target = "totalAmount", source = "orderEntity.totalAmount")
    @Mapping(target = "createdAt", source = "orderEntity.createdAt")
    Order toDto(OrderEntity orderEntity);

    @Mapping(target = "price", source = "unitPrice")
    OrderItemEntity toEntityOrderItem (OrderItem in);

    @Mapping(target = "unitPrice", source = "price")
    OrderItem toEntityOrderItem (OrderItemEntity in);
}