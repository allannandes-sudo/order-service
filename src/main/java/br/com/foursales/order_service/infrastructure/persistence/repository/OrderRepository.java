package br.com.foursales.order_service.infrastructure.persistence.repository;

import br.com.foursales.order_service.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT o.userId, COUNT(o.id) as totalOrders " +
            "FROM OrderEntity o WHERE o.status = 'PAID' " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "GROUP BY o.userId ORDER BY totalOrders DESC")
    List<Object[]> findTop5UsersByOrderCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.userId, AVG(o.totalAmount) as averageTicket " +
            "FROM OrderEntity o WHERE o.status = 'PAID' " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "GROUP BY o.userId ORDER BY averageTicket DESC")
    List<Object[]> findAverageTicketPerUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) as total_revenue " +
            "FROM orders WHERE status = 'PAID' " +
            "AND MONTH(created_at) = MONTH(CURRENT_DATE()) " +
            "AND YEAR(created_at) = YEAR(CURRENT_DATE())", nativeQuery = true)
    BigDecimal findTotalRevenueForCurrentMonth();
}