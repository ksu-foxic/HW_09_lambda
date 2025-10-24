import kotlin.text.clear

data class Chat(
    var chatId: Int = 0,
    val users: List<User>,
    val messages: MutableList<Message> = mutableListOf()
) {
    override fun toString(): String {
        val messageText = messages.joinToString(separator = "\n") { " - ${it.sender.name}:${it.text}" }
        val userNames = users.joinToString(", ") { it.name }
        return "Чат №$chatId (пользователи: $userNames:\n$messageText"
    }
}

data class User(
    val idUser: Int,
    val name: String
)

data class Message(
    var idMessage: Int = 0,
    val sender: User,
    val receiver: User,
    var text: String,
    var isRead: Boolean = false
)

interface Service<T> {
    fun add(item: T)
    fun get(): List<T>
    fun delete(id: Int): Boolean
    fun edit(id: Int, editItem: T): T?
}

class ChatService : Service<Chat> {
    private val chats = mutableListOf<Chat>()
    private var nextId = 1

    override fun add(item: Chat) {
        chats.add(item.apply { if (chatId == 0) chatId = nextId++ })
    }

    override fun get(): List<Chat> = chats.toList()

    override fun delete(id: Int): Boolean {
        return chats.removeIf { it.chatId == id }
    }

    override fun edit(id: Int, editItem: Chat): Chat? {
        val chat = chats.find { it.chatId == id }
        if (chat != null) {
            chat.messages.clear()
            chat.messages.addAll(editItem.messages)
        }
        return chat
    }

    fun findChat(user1: User, user2: User): Chat? {
        return chats.find { chat -> chat.users.toSet() == setOf(user1, user2) }
    }

    fun printChat() {
        if (chats.isEmpty()) println("Нет чатов")
        println("------------- Список чатов ----------------")
        println(chats.joinToString(separator = "\n\n") { it.toString() })
    }

    fun getUnresdChatsCount(user: User): Int {
        return chats.count { chat -> chat.users.contains(user) && chat.messages.any { it.receiver == user && !it.isRead } }
    }

    fun getLastMessages(): List<String> {
        return chats.map { chat ->
            if (chat.messages.isEmpty()) "Чат №${chat.chatId}: нет сообщений"
            else {
                val last = chat.messages.last()
                "Чат №${chat.chatId}: ${last.sender.name}: ${last.text}"
            }
        }
    }

    fun getMessagesFromChat(user: User, peer: User, count: Int): List<Message> {
        val chat = chats.find { it.users.toSet() == setOf(user, peer) } ?: return emptyList()
        val recentMessages = chat.messages.takeLast(count)
        recentMessages.forEach { if (it.receiver == user) it.isRead = true }
        return recentMessages
    }

    fun clear () {
        chats.clear()
        nextId = 1
    }
}

class MessageService(private val chatService: ChatService) : Service<Message> {
    private val messages = mutableListOf<Message>()
    private var nextIdM = 1

    override fun add(item: Message) {
        var chat = chatService.findChat(item.sender, item.receiver)

        if (chat == null) {
            chat = Chat(users = listOf(item.sender, item.receiver))
            chatService.add(chat)
        }

        val newMessage = item.copy(idMessage = nextIdM++)
        chat.messages.add(newMessage)
        messages.add(newMessage)
    }

    override fun get(): List<Message> = messages.toList()

    override fun delete(id: Int): Boolean {
        chatService.get().forEach { chat -> chat.messages.removeIf { it.idMessage == id } }
        return messages.removeIf { it.idMessage == id }
    }

    override fun edit(id: Int, editItem: Message): Message? {
        val message = messages.find { it.idMessage == id } ?: return null
        message.text = editItem.text

        chatService.get().forEach { chat -> chat.messages.find { it.idMessage == id }?.let { it.text = editItem.text } }
        return message
    }
    fun clear () {
        messages.clear()
        nextIdM = 1
    }
}

fun main() {
    val chatService = ChatService()
    val messageService = MessageService(chatService)
    chatService.printChat()

    val user1 = User(1, "Eva")
    val user2 = User(2, "Adam")
    val user3 = User(3, "Barbi")
    val user4 = User(1, "Ken")

    val message1 = Message(0, user1, user2, "Hello")
    messageService.add(message1)
    val message2 = Message(0, user2, user1, "Hi")
    messageService.add(message2)
    chatService.printChat()

    val message3 = Message(0, user3, user4, "Hello Ken")
    messageService.add(message3)
    val message4 = Message(0, user4, user3, "Hi Barbi")
    messageService.add(message4)
    chatService.printChat()

    println("\n Непрочитанных чатов у Eva: ${chatService.getUnresdChatsCount(user1)}")

    println("\n Последние сообщения из чатов:")
    chatService.getLastMessages().forEach { println(it) }

    println("\n Последние 2 сообщения между Eva и Adam:")
    val mess = chatService.getMessagesFromChat(user1, user2, 2)
    mess.forEach { println("${it.sender.name}: ${it.text}") }
    println("\n Непрочитанных чатов у Eva: ${chatService.getUnresdChatsCount(user1)}")

    val message5 = Message(0, user1, user2, "How are you?")
    messageService.add(message5)
    chatService.printChat()

    chatService.delete(2)
    messageService.delete(5)
    chatService.printChat()

    messageService.edit(1, Message(1, user1, user2, "new Hello"))
    chatService.printChat()
}