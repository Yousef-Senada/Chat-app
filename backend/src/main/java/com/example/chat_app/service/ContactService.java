package com.example.chat_app.service;

import com.example.chat_app.events.ContactUpdatedEvent;
import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.model.dto.contact.*;
import com.example.chat_app.model.dto.contact.UpdateContactRequest;
import com.example.chat_app.model.entity.Contact;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.ContactRepository;
import com.example.chat_app.repository.UserRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.chat_app.interfaces.IContactService;

@Service
public class ContactService implements IContactService {
        private final UserRepository userRepo;
        private final ContactRepository contactRepo;
        private final ApplicationEventPublisher eventPublisher;

        public ContactService(UserRepository userRepo, ContactRepository contactRepo,
                        ApplicationEventPublisher eventPublisher) {
                this.userRepo = userRepo;
                this.contactRepo = contactRepo;
                this.eventPublisher = eventPublisher;
        }

        @Override
        @Transactional
        public List<ContactMatchResponse> syncContacts(SyncContactRequest request) {
                List<String> userPhoneNumbers = request.phoneNumbers();
                List<User> matchedUsers = userRepo.findByPhoneNumberIn(userPhoneNumbers);

                return matchedUsers.stream()
                                .map(user -> new ContactMatchResponse(
                                                user.getId(),
                                                user.getUsername(),
                                                user.getName(),
                                                user.getPhoneNumber()))
                                .collect(Collectors.toList());
        }

        private ContactDisplayResponse convertToDisplayDto(Contact contact) {
                return new ContactDisplayResponse(
                                contact.getId(),
                                contact.getContactUser().getId(),
                                contact.getDisplayName(),
                                contact.getContactUser().getUsername(),
                                contact.getContactUser().getPhoneNumber());
        }

        /**
         * Get all contacts for a user - CACHED for 10 minutes.
         */
        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "contacts", key = "#owner.id")
        public List<ContactDisplayResponse> getAllContacts(User owner) {
                List<Contact> contacts = contactRepo.findAllByOwner(owner);
                return contacts.stream()
                                .map(this::convertToDisplayDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<ContactMatchResponse> getContactByPhoneNumber(String phoneNumber) {
                return userRepo.findByPhoneNumber(phoneNumber)
                                .map(user -> new ContactMatchResponse(
                                                user.getId(),
                                                user.getUsername(),
                                                user.getPhoneNumber(),
                                                user.getName()));
        }

        @Override
        @Transactional
        @CacheEvict(value = "contacts", key = "#owner.id")
        public ContactDisplayResponse addContact(User owner, AddContactRequest request) {
                User contactUser = userRepo.findByPhoneNumber(request.targetPhoneNumber())
                                .orElseThrow(() -> new AppException("Target user not found", HttpStatus.NOT_FOUND));

                if (owner.getId().equals(contactUser.getId())) {
                        throw new AppException("Cannot add yourself as a contact", HttpStatus.BAD_REQUEST);
                }

                if (contactRepo.findByOwnerAndContactUser(owner, contactUser).isPresent()) {
                        throw new AppException("Contact already added", HttpStatus.BAD_REQUEST);
                }

                Contact newContact = new Contact();
                newContact.setOwner(owner);
                newContact.setContactUser(contactUser);
                newContact.setDisplayName(request.customDisplayName());

                Contact savedContact = contactRepo.save(newContact);

                ContactNotificationDto notification = new ContactNotificationDto(
                                owner.getId(),
                                owner.getUsername(),
                                "CONTACT_ADDED");

                // Publish event - Observer pattern
                eventPublisher.publishEvent(new ContactUpdatedEvent(contactUser.getUsername(), notification));

                return convertToDisplayDto(savedContact);
        }

        @Override
        @Transactional
        @CacheEvict(value = "contacts", key = "#owner.id")
        public ContactDisplayResponse updateContact(User owner, UpdateContactRequest request) {
                User targetUser = userRepo.findById(request.targetUserId())
                                .orElseThrow(() -> new AppException("Target user not found", HttpStatus.NOT_FOUND));

                Contact relationship = contactRepo
                                .findByOwnerAndContactUser(owner, targetUser)
                                .orElseThrow(() -> new AppException("Contact relationship not found or unauthorized",
                                                HttpStatus.FORBIDDEN));

                boolean userNeedsSaving = false;
                boolean phoneNumberUpdated = false;

                String newPhone = request.newPhoneNumber();
                if (newPhone != null && !newPhone.trim().isEmpty()) {
                        targetUser.setPhoneNumber(newPhone);
                        userNeedsSaving = true;
                        phoneNumberUpdated = true;
                }

                String newDisplay = request.newDisplayName();
                if (newDisplay != null && !newDisplay.trim().isEmpty()) {
                        relationship.setDisplayName(newDisplay);
                        contactRepo.save(relationship);
                }

                if (userNeedsSaving) {
                        userRepo.save(targetUser);
                }

                if (phoneNumberUpdated) {
                        ContactNotificationDto notification = new ContactNotificationDto(
                                        owner.getId(),
                                        owner.getUsername(),
                                        "CONTACT_DETAILS_UPDATED");
                        // Publish event - Observer pattern
                        eventPublisher.publishEvent(new ContactUpdatedEvent(targetUser.getUsername(), notification));
                }

                return convertToDisplayDto(relationship);
        }

        @Override
        @Transactional
        @CacheEvict(value = "contacts", key = "#owner.id")
        public void deleteContact(User owner, UUID contactUserId) {
                User targetUser = userRepo.getReferenceById(contactUserId);

                Contact contactToDelete = contactRepo
                                .findByOwnerAndContactUser(owner, targetUser)
                                .orElseThrow(() -> new AppException("Contact not found or unauthorized",
                                                HttpStatus.FORBIDDEN));

                contactRepo.delete(contactToDelete);

                ContactNotificationDto notification = new ContactNotificationDto(
                                owner.getId(),
                                owner.getUsername(),
                                "CONTACT_REMOVED");

                // Publish event - Observer pattern
                eventPublisher.publishEvent(new ContactUpdatedEvent(targetUser.getUsername(), notification));
        }
}
