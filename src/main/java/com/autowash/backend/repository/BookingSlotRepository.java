package com.autowash.backend.repository;

import com.autowash.backend.entity.BookingSlot;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A08 - GET /slots?from=&to= backs findBySlotDateBetweenAndBlockedFalseOrderBySlotDateAscSlotStartAsc();
 * B13 - generateSlots() wraps fn_generate_slots() for SlotGeneratorJob.
 */
public interface BookingSlotRepository extends JpaRepository<BookingSlot, UUID> {

    List<BookingSlot> findBySlotDateBetweenAndBlockedFalseOrderBySlotDateAscSlotStartAsc(LocalDate from, LocalDate to);

    /**
     * B13 - wraps the DB function fn_generate_slots(p_days INT DEFAULT 30),
     * which inserts 30-minute slots (07:00-20:00, capacity 3) for the next
     * N days, skipping any that already exist (ON CONFLICT DO NOTHING).
     * Returns the number of NEW slots actually inserted.
     */
    default int generateSlots(EntityManager entityManager, int days) {
        Number result = (Number) entityManager
                .createNativeQuery("SELECT fn_generate_slots(:days)")
                .setParameter("days", days)
                .getSingleResult();
        return result.intValue();
    }
}
