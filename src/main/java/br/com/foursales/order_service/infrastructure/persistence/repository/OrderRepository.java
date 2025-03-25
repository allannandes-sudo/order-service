package br.com.foursales.order_service.infrastructure.persistence.repository;

import br.com.foursales.order_service.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query(value = "SELECT o.user_id, COUNT(o.id) as total_orders FROM orders o WHERE o.status = 'PAID' GROUP BY o.user_id ORDER BY total_orders DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5UsersByOrderCount();

    @Query(value = "SELECT o.user_id, AVG(o.total_amount) as average_ticket FROM orders o WHERE o.status = 'PAID' GROUP BY o.user_id", nativeQuery = true)
    List<Object[]> findAverageTicketPerUser();

    @Query(value = "SELECT SUM(total_amount) as total_revenue FROM orders WHERE status = 'PAID' AND MONTH(created_at) = MONTH(CURRENT_DATE()) AND YEAR(created_at) = YEAR(CURRENT_DATE())", nativeQuery = true)
    BigDecimal findTotalRevenueForCurrentMonth();
}