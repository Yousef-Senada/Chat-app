package com.example.chat_app.controller;

import com.example.chat_app.model.dto.contact.AddContactRequest;
import com.example.chat_app.model.dto.contact.ContactDisplayResponse;
import com.example.chat_app.model.dto.contact.ContactMatchResponse;
import com.example.chat_app.model.dto.contact.SyncContactRequest;
import com.example.chat_app.model.dto.contact.UpdateContactRequest;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactDisplayResponse>> getAllContacts(@AuthenticationPrincipal User owner) {
        System.out.println("=== getAllContacts called, owner: " + (owner != null ? owner.getUsername() : "NULL"));
        List<ContactDisplayResponse> contacts = contactService.getAllContacts(owner);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/phone")
    public ResponseEntity<ContactMatchResponse> getContactByPhoneNumber(@RequestParam String phone) {
        return contactService.getContactByPhoneNumber(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public ResponseEntity<List<ContactMatchResponse>> syncContacts(@RequestBody SyncContactRequest request) {
        return ResponseEntity.ok(contactService.syncContacts(request));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addContact(@AuthenticationPrincipal User owner, @RequestBody AddContactRequest request) {
        contactService.addContact(owner, request);
        return ResponseEntity.ok("Contact added successfully");
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateContact(@AuthenticationPrincipal User owner,
            @RequestBody UpdateContactRequest request) {
        contactService.updateContact(owner, request);
        return ResponseEntity.ok("Contact updated successfully");
    }

    @DeleteMapping("/delete/{contactUserId}")
    public ResponseEntity<?> deleteContact(@AuthenticationPrincipal User owner, @PathVariable UUID contactUserId) {
        contactService.deleteContact(owner, contactUserId);
        return ResponseEntity.noContent().build();
    }
}
