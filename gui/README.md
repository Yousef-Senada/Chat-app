# Chat Application - Design Patterns Documentation

A modern JavaFX-based chat application that demonstrates the implementation of various software design patterns for clean, maintainable, and scalable code architecture.

---

## 📋 Table of Contents

1. [Singleton Pattern](#1-singleton-pattern)
2. [Service Locator Pattern](#2-service-locator-pattern)
3. [Factory Pattern](#3-factory-pattern)
4. [Observer Pattern](#4-observer-pattern)
5. [MVC (Model-View-Controller) Pattern](#5-mvc-model-view-controller-pattern)
6. [Repository / Store Pattern](#6-repository--store-pattern)
7. [Builder Pattern](#7-builder-pattern)
8. [Data Transfer Object (DTO) Pattern](#8-data-transfer-object-dto-pattern)
9. [Facade Pattern](#9-facade-pattern)

---

## 1. Singleton Pattern

### Overview
The **Singleton Pattern** ensures that a class has only one instance and provides a global point of access to it. This is used throughout the application for managing global state and shared resources.

### Implementation Examples

#### NavigationManager.java
```java
public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    private String cssStylesheet;
    private Object currentController;

    // Private constructor prevents external instantiation
    private NavigationManager() {
    }

    // Thread-safe singleton access
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            currentController = loader.getController();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
```

#### ChatSessionManager.java
```java
public class ChatSessionManager {
    
    private static ChatSessionManager instance;
    private Chat activeChat;
    private List<Chat> recentChats;
    private List<User> contacts;

    private ChatSessionManager() {
        initializeMockData();
    }

    public static ChatSessionManager getInstance() {
        if (instance == null) {
            instance = new ChatSessionManager();
        }
        return instance;
    }

    public void setActiveChat(Chat chat) {
        this.activeChat = chat;
    }

    public Chat getActiveChat() {
        return activeChat;
    }
}
```

### Benefits
- ✅ Controlled access to shared resources
- ✅ Single point of management for navigation and session state
- ✅ Lazy initialization (instances created only when needed)

---

## 2. Service Locator Pattern

### Overview
The **Service Locator Pattern** provides a centralized registry for all services and dependencies. It acts as a single access point for obtaining service instances, implementing a form of dependency injection.

### Implementation Example

#### ServiceLocator.java
```java
/**
 * Service Locator pattern for dependency injection.
 * Provides singleton access to all services and stores.
 */
public class ServiceLocator {

  private static ServiceLocator instance;

  // Infrastructure
  private final TokenStorage tokenStorage;
  private final ApiClient apiClient;

  // API Endpoints
  private final AuthApi authApi;
  private final UsersApi usersApi;
  private final ChatsApi chatsApi;
  private final MessagesApi messagesApi;
  private final ContactsApi contactsApi;

  // Stores
  private final AuthStore authStore;
  private final ChatStore chatStore;
  private final MessageStore messageStore;
  private final ContactStore contactStore;

  // Services
  private final AuthService authService;
  private final ChatService chatService;
  private final MessageService messageService;
  private final ContactService contactService;

  private ServiceLocator() {
    // Initialize infrastructure
    this.tokenStorage = new TokenStorage();
    this.apiClient = new ApiClient(tokenStorage);

    // Initialize API endpoints
    this.authApi = new AuthApi(apiClient);
    this.usersApi = new UsersApi(apiClient);
    this.chatsApi = new ChatsApi(apiClient);
    this.messagesApi = new MessagesApi(apiClient);
    this.contactsApi = new ContactsApi(apiClient);

    // Initialize stores
    this.authStore = new AuthStore(tokenStorage);
    this.chatStore = new ChatStore();
    this.messageStore = new MessageStore();
    this.contactStore = new ContactStore();

    // Initialize services with their dependencies
    this.authService = new AuthService(authApi, usersApi, authStore, tokenStorage);
    this.chatService = new ChatService(chatsApi, chatStore, authStore);
    this.messageService = new MessageService(messagesApi, messageStore);
    this.contactService = new ContactService(contactsApi, contactStore);
  }

  public static synchronized ServiceLocator getInstance() {
    if (instance == null) {
      instance = new ServiceLocator();
    }
    return instance;
  }

  // Service getters
  public AuthService getAuthService() { return authService; }
  public ChatService getChatService() { return chatService; }
  public MessageService getMessageService() { return messageService; }
  public ContactService getContactService() { return contactService; }

  // Store getters
  public AuthStore getAuthStore() { return authStore; }
  public ChatStore getChatStore() { return chatStore; }

  /**
   * Resets the singleton instance.
   * Useful for testing or when user logs out completely.
   */
  public static synchronized void reset() {
    if (instance != null) {
      instance.authStore.logout();
      instance.chatStore.clear();
      instance.messageStore.clear();
      instance.contactStore.clear();
    }
    instance = null;
  }
}
```

### Usage in Controllers
```java
public class ChatController {
  
  @FXML
  public void initialize() {
    // Get services through the Service Locator
    chatService = ServiceLocator.getInstance().getChatService();
    messageService = ServiceLocator.getInstance().getMessageService();
    chatStore = chatService.getStore();
    messageStore = messageService.getStore();
    authStore = ServiceLocator.getInstance().getAuthStore();
  }
}
```

### Benefits
- ✅ Centralized dependency management
- ✅ Easy to swap implementations for testing
- ✅ Decouples components from concrete implementations
- ✅ Single point for initialization and cleanup

---

## 3. Factory Pattern

### Overview
The **Factory Pattern** provides an interface for creating objects without specifying the exact class of the object that will be created. This pattern is used to create message and group components.

### Implementation Examples

#### MessageFactory.java
```java
public class MessageFactory {

    private static final String INCOMING_BG = "#FFFFFF";
    private static final String OUTGOING_BG = "#8B5CF6";
    private static final String INCOMING_TEXT = "#1F2937";
    private static final String OUTGOING_TEXT = "#FFFFFF";

    // Factory method that creates appropriate message component based on type
    public static HBox createMessageComponent(Message message) {
        switch (message.getType()) {
            case IMAGE:
                return createImageMessage(message.getFilePath(), message.getFileName(), 
                                         message.getFileSize(), message.isOutgoing(), 
                                         message.getFormattedTime());
            case VIDEO:
                return createVideoMessage(message.getFilePath(), message.getFileName(), 
                                          message.getFileSize(), message.isOutgoing(), 
                                          message.getFormattedTime());
            case FILE:
                return createFileMessage(message.getFilePath(), message.getFileName(), 
                                         message.getFileSize(), message.isOutgoing(), 
                                         message.getFormattedTime());
            case TEXT:
            default:
                return createTextMessage(message.getContent(), message.isOutgoing(), 
                                         message.getFormattedTime());
        }
    }

    public static HBox createTextMessage(String text, boolean isOutgoing, String time) {
        HBox container = new HBox();
        container.setPadding(new Insets(4, 16, 4, 16));
        container.setAlignment(isOutgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setMaxWidth(400);
        
        if (isOutgoing) {
            bubble.setStyle("-fx-background-color: " + OUTGOING_BG + 
                          "; -fx-background-radius: 16 16 4 16;");
        } else {
            bubble.setStyle("-fx-background-color: " + INCOMING_BG + 
                          "; -fx-background-radius: 16 16 16 4;");
        }

        // Create labels and add to bubble...
        bubble.getChildren().addAll(messageLabel, timeLabel);
        container.getChildren().add(bubble);

        return container;
    }

    public static HBox createImageMessage(String filePath, String fileName, 
                                          String fileSize, boolean isOutgoing, String time) {
        // Image-specific message creation logic
        HBox container = new HBox();
        // ... implementation
        return container;
    }
}
```

#### GroupFactory.java
```java
public class GroupFactory {

    private static final String[] GROUP_COLORS = {
        "#8B5CF6", "#3B82F6", "#22C55E", "#EF4444", 
        "#F59E0B", "#EC4899", "#06B6D4", "#10B981"
    };

    private static int colorIndex = 0;

    // Factory methods for different group types
    public static Group createPublicGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.PUBLIC);
        group.setAvatarColor(getNextColor());
        return group;
    }

    public static Group createPrivateGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.PRIVATE);
        group.setAvatarColor(getNextColor());
        return group;
    }

    public static Group createAdminGroup(String name, String description) {
        Group group = new Group(generateId(), name, description, Group.GroupType.ADMIN);
        group.setAvatarColor(getNextColor());
        return group;
    }

    // Parameterized factory method
    public static Group createGroup(String name, String description, Group.GroupType type) {
        switch (type) {
            case PUBLIC:
                return createPublicGroup(name, description);
            case PRIVATE:
                return createPrivateGroup(name, description);
            case ADMIN:
                return createAdminGroup(name, description);
            default:
                return createPublicGroup(name, description);
        }
    }

    // Factory method with additional configuration
    public static Group createGroupWithOwner(String name, String description, 
                                              Group.GroupType type, User creator) {
        Group group = createGroup(name, description, type);
        group.setCreator(creator);
        group.addAdmin(creator);
        return group;
    }

    private static String generateId() {
        return "group-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
```

#### Static Factory Methods in Message.java
```java
public class Message {
    
    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE
    }

    // Static factory method for text messages
    public static Message createTextMessage(String id, String senderId, 
                                            String senderName, String content, 
                                            boolean outgoing) {
        return new Message(id, senderId, senderName, content, outgoing);
    }

    // Static factory method for image messages
    public static Message createImageMessage(String id, String senderId, 
                                             String senderName, String filePath, 
                                             String fileName, String fileSize, 
                                             boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, "[Image]", outgoing);
        msg.type = MessageType.IMAGE;
        msg.filePath = filePath;
        msg.fileName = fileName;
        msg.fileSize = fileSize;
        return msg;
    }

    // Static factory method for video messages
    public static Message createVideoMessage(String id, String senderId, 
                                             String senderName, String filePath, 
                                             String videoName, String fileSize, 
                                             boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, "[Video]", outgoing);
        msg.type = MessageType.VIDEO;
        msg.filePath = filePath;
        msg.fileName = videoName;
        msg.fileSize = fileSize;
        return msg;
    }

    // Static factory method for file messages
    public static Message createFileMessage(String id, String senderId, 
                                            String senderName, String filePath, 
                                            String fileName, String fileSize, 
                                            boolean outgoing) {
        Message msg = new Message(id, senderId, senderName, fileName, outgoing);
        msg.type = MessageType.FILE;
        msg.filePath = filePath;
        msg.fileName = fileName;
        msg.fileSize = fileSize;
        return msg;
    }
}
```

### Benefits
- ✅ Encapsulates object creation logic
- ✅ Makes code more maintainable and readable
- ✅ Easy to add new message/group types without modifying client code
- ✅ Centralizes complex initialization logic

---

## 4. Observer Pattern

### Overview
The **Observer Pattern** defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically. JavaFX Properties and Observable collections implement this pattern natively.

### Implementation Examples

#### AuthStore.java - Observable Properties
```java
/**
 * Observable store for authentication state.
 * Uses JavaFX properties for UI binding.
 */
public class AuthStore {

  private final ObjectProperty<UserResponse> currentUser = new SimpleObjectProperty<>();
  private final BooleanProperty loggedIn = new SimpleBooleanProperty(false);
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  private final TokenStorage tokenStorage;

  public AuthStore(TokenStorage tokenStorage) {
    this.tokenStorage = tokenStorage;
    this.loggedIn.set(tokenStorage.hasToken());
  }

  // Property getters for binding (Observer pattern)
  public ObjectProperty<UserResponse> currentUserProperty() {
    return currentUser;
  }

  public BooleanProperty loggedInProperty() {
    return loggedIn;
  }

  public BooleanProperty loadingProperty() {
    return loading;
  }

  public StringProperty errorProperty() {
    return error;
  }

  // Actions that notify observers automatically
  public void setCurrentUser(UserResponse user) {
    currentUser.set(user);
    loggedIn.set(user != null);  // Automatically notifies observers
  }

  public void logout() {
    tokenStorage.clearToken();
    currentUser.set(null);
    loggedIn.set(false);
    error.set(null);
  }
}
```

#### ChatStore.java - Observable Collections
```java
/**
 * Observable store for chat state.
 * Uses JavaFX properties for UI binding.
 */
public class ChatStore {

  // Observable collections automatically notify UI on changes
  private final ObservableList<ChatDisplayDto> chats = FXCollections.observableArrayList();
  private final ObjectProperty<ChatDisplayDto> activeChat = new SimpleObjectProperty<>();
  private final ObservableList<MemberDisplayDto> activeChatMembers = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  // Observable list getters
  public ObservableList<ChatDisplayDto> getChats() {
    return chats;
  }

  public ObservableList<MemberDisplayDto> getActiveChatMembers() {
    return activeChatMembers;
  }

  // Actions that trigger observer notifications
  public void setChats(List<ChatDisplayDto> newChats) {
    chats.clear();
    chats.addAll(newChats);  // Observers are notified of the change
  }

  public void addChat(ChatDisplayDto chat) {
    chats.add(0, chat);  // Observers are notified
  }

  public void setActiveChat(ChatDisplayDto chat) {
    activeChat.set(chat);
    if (chat != null && chat.members() != null) {
      activeChatMembers.setAll(chat.members());
    } else {
      activeChatMembers.clear();
    }
  }
}
```

#### ChatController.java - Subscribing to Observations
```java
public class ChatController {

  @FXML
  public void initialize() {
    // Bind loading indicator directly to store property
    if (loadingIndicator != null) {
      loadingIndicator.visibleProperty().bind(messageStore.loadingProperty());
      loadingIndicator.managedProperty().bind(messageStore.loadingProperty());
    }

    // Listen to message list changes (Observer pattern)
    messageStore.getMessages().addListener((ListChangeListener<MessageDisplayDto>) change -> {
      updateMessageList();  // React to data changes
    });

    // Listen to chat members changes
    chatStore.getActiveChatMembers().addListener((ListChangeListener<MemberDisplayDto>) change -> {
      updateMembersList();  // React to member changes
    });
  }

  private void updateMessageList() {
    messagesContainer.getChildren().clear();
    for (MessageDisplayDto message : messageStore.getMessages()) {
      HBox messageComponent = createMessageComponent(message);
      messagesContainer.getChildren().add(messageComponent);
    }
  }
}
```

### Benefits
- ✅ Loose coupling between data and UI
- ✅ Automatic UI updates when data changes
- ✅ Clean separation of concerns
- ✅ Easy to add multiple observers for the same data

---

## 5. MVC (Model-View-Controller) Pattern

### Overview
The **MVC Pattern** separates the application into three main components: Model (data), View (UI), and Controller (logic). This application uses JavaFX FXML for views with corresponding controller classes.

### Implementation Structure

```
src/Java/com/untitled/
├── models/                    # MODEL - Data structures
│   ├── Chat.java
│   ├── Group.java
│   ├── Message.java
│   └── User.java
│
├── views/                     # VIEW - FXML UI definitions
│   ├── Dashboard.fxml
│   ├── Chat.fxml
│   ├── Contacts.fxml
│   ├── Settings.fxml
│   ├── login.fxml
│   └── register.fxml
│
├── controllers/               # CONTROLLER - UI logic
│   ├── ChatController.java
│   ├── DashboardController.java
│   ├── ContactsController.java
│   ├── LoginController.java
│   ├── RegisterController.java
│   └── SettingsController.java
│
├── service/                   # Business logic layer
│   ├── AuthService.java
│   ├── ChatService.java
│   ├── MessageService.java
│   └── ContactService.java
│
└── dto/                       # Data Transfer Objects
    ├── request/
    └── response/
```

### Controller Example
```java
/**
 * Controller for the chat view.
 * Displays messages and allows sending new messages.
 */
public class ChatController {

  // FXML-injected view components
  @FXML private VBox messagesContainer;
  @FXML private ScrollPane messagesScrollPane;
  @FXML private TextField messageInput;
  @FXML private Button sendButton;
  @FXML private Label chatNameLabel;

  // Services for business logic
  private ChatService chatService;
  private MessageService messageService;

  // Stores for state management
  private ChatStore chatStore;
  private MessageStore messageStore;

  @FXML
  public void initialize() {
    // Initialize services from Service Locator
    chatService = ServiceLocator.getInstance().getChatService();
    messageService = ServiceLocator.getInstance().getMessageService();
    
    // Bind UI to data
    messageInput.setOnAction(e -> onSendMessage());
    
    // Load initial data
    if (currentChat != null) {
      loadChatData();
      messageService.loadMessages(currentChat.chatId());
    }
  }

  @FXML
  private void onSendMessage() {
    String content = messageInput.getText();
    if (content == null || content.trim().isEmpty()) return;
    if (currentChat == null) return;

    messageService.sendTextMessage(currentChat.chatId(), content.trim());
    messageInput.clear();
    messageInput.requestFocus();
  }

  @FXML
  private void onBackClick() {
    NavigationManager.getInstance().navigateTo("views/Dashboard.fxml", "Chats");
  }
}
```

### Benefits
- ✅ Clear separation of concerns
- ✅ View can be modified without changing business logic
- ✅ Easy to test controllers in isolation
- ✅ Multiple views can share the same model

---

## 6. Repository / Store Pattern

### Overview
The **Store Pattern** (similar to Repository) provides a collection-like interface for accessing domain objects while abstracting the underlying data storage mechanism. It manages application state and provides a clean API for data operations.

### Implementation Examples

#### ChatStore.java
```java
/**
 * Observable store for chat state.
 * Uses JavaFX properties for UI binding.
 */
public class ChatStore {

  private final ObservableList<ChatDisplayDto> chats = FXCollections.observableArrayList();
  private final ObjectProperty<ChatDisplayDto> activeChat = new SimpleObjectProperty<>();
  private final ObservableList<MemberDisplayDto> activeChatMembers = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  // CRUD-like operations on the store
  public void setChats(List<ChatDisplayDto> newChats) {
    chats.clear();
    chats.addAll(newChats);
  }

  public void addChat(ChatDisplayDto chat) {
    chats.add(0, chat);
  }

  public void removeChat(ChatDisplayDto chat) {
    chats.remove(chat);
  }

  public void setActiveChat(ChatDisplayDto chat) {
    activeChat.set(chat);
    if (chat != null && chat.members() != null) {
      activeChatMembers.setAll(chat.members());
    } else {
      activeChatMembers.clear();
    }
  }

  public void clear() {
    chats.clear();
    activeChat.set(null);
    activeChatMembers.clear();
    error.set(null);
  }

  // Getters for state access
  public ObservableList<ChatDisplayDto> getChats() { return chats; }
  public ChatDisplayDto getActiveChat() { return activeChat.get(); }
  public boolean isLoading() { return loading.get(); }
}
```

#### MessageStore.java
```java
public class MessageStore {

  private final ObservableList<MessageDisplayDto> messages = FXCollections.observableArrayList();
  private final BooleanProperty loading = new SimpleBooleanProperty(false);
  private final StringProperty error = new SimpleStringProperty();

  public ObservableList<MessageDisplayDto> getMessages() {
    return messages;
  }

  public void setMessages(List<MessageDisplayDto> newMessages) {
    messages.clear();
    messages.addAll(newMessages);
  }

  public void addMessage(MessageDisplayDto message) {
    messages.add(message);
  }

  public void clear() {
    messages.clear();
    error.set(null);
  }
}
```

### Benefits
- ✅ Centralized state management
- ✅ Consistent API for data operations
- ✅ Observable state for reactive UI updates
- ✅ Clean separation between data access and business logic

---

## 7. Builder Pattern

### Overview
The **Builder Pattern** is used to construct complex objects step by step. In this application, it's implemented implicitly through the Java HttpClient API and request building.

### Implementation Example

#### ApiClient.java - Request Builder
```java
/**
 * Central HTTP client for all API calls.
 * Uses java.net.http.HttpClient introduced in Java 11.
 */
public class ApiClient {

  private static final String BASE_URL = "http://localhost:8080";
  private final HttpClient httpClient;
  private final TokenStorage tokenStorage;

  public ApiClient(TokenStorage tokenStorage) {
    this.tokenStorage = tokenStorage;
    // Builder pattern for HttpClient
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  // Builder pattern for constructing HTTP requests
  private HttpRequest.Builder buildRequest(String path) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + path))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .timeout(Duration.ofSeconds(30));

    String token = tokenStorage.getToken();
    if (token != null && !token.isEmpty()) {
      builder.header("Authorization", "Bearer " + token);
    }

    return builder;
  }

  public CompletableFuture<String> get(String path) {
    HttpRequest request = buildRequest(path)
        .GET()
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> post(String path, Object body) {
    String jsonBody = JsonMapper.toJson(body);
    HttpRequest request = buildRequest(path)
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> patch(String path, Object body) {
    String jsonBody = JsonMapper.toJson(body);
    HttpRequest request = buildRequest(path)
        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
    return sendAsync(request);
  }

  public CompletableFuture<String> delete(String path) {
    HttpRequest request = buildRequest(path)
        .DELETE()
        .build();
    return sendAsync(request);
  }
}
```

### Benefits
- ✅ Fluent API for constructing complex objects
- ✅ Separation of construction from representation
- ✅ Immutable objects after construction
- ✅ Clear and readable code

---

## 8. Data Transfer Object (DTO) Pattern

### Overview
The **DTO Pattern** is used to transfer data between layers of the application. DTOs are simple objects that encapsulate data without business logic.

### Implementation Examples

#### MessageDisplayDto.java (Java Record)
```java
/**
 * Response DTO for message display data.
 * GET /api/messages/chat/{chatId}
 */
public record MessageDisplayDto(
    UUID messageId,
    SenderDto sender,
    String messageType,
    String content,
    String mediaUrl,
    LocalDateTime timestamp,
    boolean isEdited,
    boolean isDeleted) {
    
  /**
   * Creates a MessageDisplayDto from a JSON string.
   */
  public static MessageDisplayDto fromJson(String json) {
    Map<String, Object> map = JsonMapper.parseJson(json);
    return fromMap(map);
  }

  /**
   * Creates a MessageDisplayDto from a parsed JSON map.
   */
  public static MessageDisplayDto fromMap(Map<String, Object> map) {
    UUID messageId = JsonMapper.getUUID(map, "messageId");
    
    Map<String, Object> senderMap = JsonMapper.getMap(map, "sender");
    SenderDto sender = SenderDto.fromMap(senderMap);
    
    String messageType = JsonMapper.getString(map, "messageType");
    String content = JsonMapper.getString(map, "content");
    String mediaUrl = JsonMapper.getString(map, "mediaUrl");
    LocalDateTime timestamp = JsonMapper.getDateTime(map, "timestamp");
    boolean isEdited = JsonMapper.getBoolean(map, "isEdited");
    boolean isDeleted = JsonMapper.getBoolean(map, "isDeleted");

    return new MessageDisplayDto(messageId, sender, messageType, content, 
                                  mediaUrl, timestamp, isEdited, isDeleted);
  }

  // Helper methods for display
  public String getDisplayContent() {
    if (isDeleted) {
      return "[Message deleted]";
    }
    if (content != null) {
      return content;
    }
    return switch (messageType) {
      case "IMAGE" -> "[Image]";
      case "AUDIO" -> "[Audio]";
      default -> "";
    };
  }

  public boolean isText() { return "TEXT".equals(messageType); }
  public boolean isImage() { return "IMAGE".equals(messageType); }
}
```

#### Request DTO Example - CreateChatRequest
```java
public record CreateChatRequest(
    String chatType,
    String groupName,
    String groupImage,
    List<UUID> memberIds) {
}
```

### Benefits
- ✅ Clean data transfer between layers
- ✅ Immutable objects (using Java Records)
- ✅ Built-in JSON serialization/deserialization
- ✅ Type-safe data containers

---

## 9. Facade Pattern

### Overview
The **Facade Pattern** provides a simplified interface to a complex subsystem. The Service layer acts as a facade, hiding the complexity of API calls, state management, and error handling.

### Implementation Example

#### ChatService.java
```java
/**
 * Service class for chat operations.
 * Acts as a Facade for chat-related operations.
 * Orchestrates API calls and state updates.
 */
public class ChatService {

  private final ChatsApi chatsApi;
  private final ChatStore chatStore;
  private final AuthStore authStore;

  public ChatService(ChatsApi chatsApi, ChatStore chatStore, AuthStore authStore) {
    this.chatsApi = chatsApi;
    this.chatStore = chatStore;
    this.authStore = authStore;
  }

  /**
   * Loads all chats for the current user.
   * Facade method that handles: API call, state update, error handling
   */
  public void loadChats() {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.getUserChats()
        .thenAccept(chats -> {
          Platform.runLater(() -> {
            chatStore.setChats(chats);
            chatStore.setLoading(false);
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            String errorMessage = extractErrorMessage(throwable);
            chatStore.setError(errorMessage);
          });
          return null;
        });
  }

  /**
   * Creates a group chat.
   * Simple interface for complex operation.
   */
  public void createGroupChat(String groupName, String groupImage, List<UUID> memberIds) {
    chatStore.setLoading(true);
    chatStore.clearError();

    chatsApi.createGroupChat(groupName, groupImage, memberIds)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            loadChats();  // Refresh chat list
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            chatStore.setError(extractErrorMessage(throwable));
          });
          return null;
        });
  }

  /**
   * Sets the active chat and loads related data.
   */
  public void setActiveChat(ChatDisplayDto chat) {
    chatStore.setActiveChat(chat);

    // Automatically load members if needed
    if (chat != null && (chat.members() == null || chat.members().isEmpty())) {
      loadChatMembers(chat.chatId());
    }
  }

  /**
   * Leaves a chat - simplified interface for the client.
   */
  public void leaveChat(UUID chatId) {
    if (authStore.getCurrentUser() == null) {
      chatStore.setError("User not logged in");
      return;
    }

    UUID currentUserId = authStore.getCurrentUser().ID();
    chatStore.setLoading(true);

    chatsApi.leaveChat(chatId, currentUserId)
        .thenAccept(response -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            chatStore.clearActiveChat();
            loadChats();
          });
        })
        .exceptionally(throwable -> {
          Platform.runLater(() -> {
            chatStore.setLoading(false);
            chatStore.setError(extractErrorMessage(throwable));
          });
          return null;
        });
  }
}
```

### Benefits
- ✅ Simple interface for complex operations
- ✅ Hides implementation details from clients
- ✅ Reduces coupling between layers
- ✅ Easy to modify internal implementation

---

## 📊 Pattern Summary

| Pattern | Purpose | Key Files |
|---------|---------|-----------|
| **Singleton** | Single instance management | `NavigationManager`, `ChatSessionManager`, `ServiceLocator` |
| **Service Locator** | Dependency injection | `ServiceLocator` |
| **Factory** | Object creation | `MessageFactory`, `GroupFactory`, `Message` |
| **Observer** | Reactive data binding | `AuthStore`, `ChatStore`, `MessageStore` |
| **MVC** | Separation of concerns | Controllers, Models, Views (FXML) |
| **Store/Repository** | State management | `AuthStore`, `ChatStore`, `MessageStore`, `ContactStore` |
| **Builder** | Complex object construction | `ApiClient` (HttpRequest building) |
| **DTO** | Data transfer | `MessageDisplayDto`, `ChatDisplayDto`, `UserResponse` |
| **Facade** | Simplified interface | `ChatService`, `AuthService`, `MessageService` |

---

## 🎯 Architecture Benefits

1. **Maintainability**: Clear separation of concerns makes code easy to understand and modify
2. **Testability**: Decoupled components can be tested in isolation
3. **Scalability**: New features can be added without modifying existing code
4. **Reusability**: Factory and DTO patterns enable code reuse
5. **Reactivity**: Observer pattern enables automatic UI updates

---

## 📁 Project Structure

```
src/Java/com/untitled/
├── Main.java                      # Application entry point
├── NavigationManager.java         # Singleton navigation
├── api/
│   ├── ApiClient.java             # HTTP client (Builder pattern)
│   ├── ApiException.java          # Custom exception
│   ├── TokenStorage.java          # Token management
│   └── endpoints/                 # API endpoint classes
├── controllers/                   # MVC Controllers
├── dto/
│   ├── request/                   # Request DTOs
│   └── response/                  # Response DTOs
├── factories/                     # Factory classes
├── managers/                      # Session managers
├── models/                        # Domain models
├── service/                       # Service layer (Facade)
├── store/                         # State stores (Observer)
├── util/                          # Utilities
└── views/                         # FXML views
```

---

*This documentation provides an overview of the design patterns implemented in the Chat Application. Each pattern contributes to a clean, maintainable, and scalable architecture.*
