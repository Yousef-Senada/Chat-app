package com.example.chat_app.model.dto.contact;

import java.util.List;

public record SyncContactRequest(
        List<String>  phoneNumbers
) {}
