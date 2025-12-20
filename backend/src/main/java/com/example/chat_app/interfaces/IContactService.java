package com.example.chat_app.interfaces;

import com.example.chat_app.model.dto.contact.AddContactRequest;
import com.example.chat_app.model.dto.contact.ContactDisplayResponse;
import com.example.chat_app.model.dto.contact.ContactMatchResponse;
import com.example.chat_app.model.dto.contact.SyncContactRequest;
import com.example.chat_app.model.dto.contact.UpdateContactRequest;
import com.example.chat_app.model.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for contact management operations.
 */
public interface IContactService {

    /**
     * Sync phone contacts and find matching users.
     */
    List<ContactMatchResponse> syncContacts(SyncContactRequest request);

    /**
     * Get all contacts for a user.
     */
    List<ContactDisplayResponse> getAllContacts(User owner);

    /**
     * Get contact by phone number.
     */
    Optional<ContactMatchResponse> getContactByPhoneNumber(String phoneNumber);

    /**
     * Add a new contact.
     */
    ContactDisplayResponse addContact(User owner, AddContactRequest request);

    /**
     * Update contact details.
     */
    ContactDisplayResponse updateContact(User owner, UpdateContactRequest request);

    /**
     * Delete a contact.
     */
    void deleteContact(User owner, UUID contactUserId);
}
