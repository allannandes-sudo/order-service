package br.com.foursales.order_service.application.event.mapper;

import br.com.foursales.order_service.application.event.dto.OrderItemEvent;
import br.com.foursales.order_service.infrastructure.persistence.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;


@Mapper(componentModel = "spring", imports = {BigDecimal.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderEventMapper {

    List<OrderItemEvent> toOrderItemEvents(List<OrderItemEntity> items);

    @Mapping(target = "unitPrice", source = "price")
    @Mapping(target = "totalAmount", expression = "java(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))")
    OrderItemEvent toOrderItemEvent(OrderItemEntity item);
}
