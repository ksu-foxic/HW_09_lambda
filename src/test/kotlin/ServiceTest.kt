import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ServiceTest {

    private lateinit var chatService: ChatService
    private lateinit var messageService: MessageService
    private val user1 = User(1, "Eva")
    private  val user2 = User(2, "Adam")
    private  val user3 = User(23, "Barbi")

    @Before
    fun clearBeforeTest() {

        chatService = ChatService()
        messageService = MessageService(chatService)

        chatService.clear()
        messageService.clear()
    }

    @Test
    fun addMessage() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        val chats = chatService.get()
        assertEquals("Hello",chats.first().messages.first().text)
    }

    @Test
    fun addMessageTwo() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        messageService.add(Message(sender = user2, receiver = user1, text = "Hi"))

        val chats = chatService.get()
        assertEquals(2, chats.first().messages.size)
    }

    @Test
    fun deleteChat() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        val chatId = chatService.get().first().chatId
        val result = chatService.delete(chatId)

        assertTrue(result)
    }

    @Test
    fun deleteMessage() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        val messId = messageService.get().first().idMessage
        val result = messageService.delete(messId)

        assertTrue(result)
    }

    @Test
    fun editMessage() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        messageService.edit(1, Message(1, user1, user2, "Hi"))
        assertEquals("Hi", messageService.get().first().text)
    }

    @Test
    fun unReadChats() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        messageService.add(Message(sender = user3, receiver = user2, text = "Hi"))

        val unreadChats = chatService.getUnresdChatsCount(user2)
        assertEquals(2, unreadChats)
    }

    @Test
    fun unReadMessages() {
        messageService.add(Message(sender = user1, receiver = user2, text = "Hello"))
        messageService.add(Message(sender = user3, receiver = user1, text = "Hi"))
        val last = chatService.getLastMessages()

        assertTrue(last.any {it.contains("Hi")})
        assertTrue(last.any {it.contains("Hello")})
    }

    @Test
    fun getMessages() {
        messageService.add(Message(sender = user1, receiver = user2, text = "first"))
        messageService.add(Message(sender = user1, receiver = user2, text = "second"))
        val before = chatService.getUnresdChatsCount(user2)
        val mess = chatService.getMessagesFromChat(user2, user1, 2)
        val after = chatService.getUnresdChatsCount(user2)

        assertTrue(before > after)
        assertEquals(2,mess.size)
    }
}