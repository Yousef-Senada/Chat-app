package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Contact;
import com.example.chat_app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    Optional<Contact> findByOwnerAndContactUser(User owner, User contactUser);

    /**
     * Fetch all contacts for an owner with contactUser data pre-loaded.
     * DISTINCT prevents duplicate rows from JOIN FETCH.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
            "JOIN FETCH c.contactUser " +
            "WHERE c.owner = :owner")
    List<Contact> findAllByOwner(@Param("owner") User owner);
}
