document.addEventListener("DOMContentLoaded", function () {

    const sendButton = document.getElementById("send-button");
    const chatInput = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("messages");
    const form = document.querySelector(".input-area");

    if (!sendButton || !chatInput || !messagesContainer || !form) {
        return;
    }

    sendButton.addEventListener("click", sendMessage);

    chatInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            sendMessage();
        }
    });

    async function sendMessage() {

        const prompt = chatInput.value.trim();

        if (!prompt) {
            return;
        }

        sendButton.disabled = true;
        chatInput.disabled = true;

        // Показываем сообщение пользователя
        const userDiv = document.createElement("div");
        userDiv.className = "message user";

        const avatar = document.createElement("img");
        avatar.src = "/images/user.png";
        avatar.alt = "User";

        const bubble = document.createElement("div");
        bubble.className = "bubble";
        bubble.textContent = prompt;

        userDiv.appendChild(avatar);
        userDiv.appendChild(bubble);

        messagesContainer.appendChild(userDiv);

        messagesContainer.scrollTop =
            messagesContainer.scrollHeight;

        try {
            // Получаем URL из th:action
            const url = form.action;

            // Формируем POST-параметр
            const formData = new URLSearchParams();
            formData.append("prompt", prompt);

            const response = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData
            });

            if (!response.ok) {
                throw new Error(
                    `HTTP ${response.status}: ${response.statusText}`
                );
            }

            // Controller возвращает redirect:/chat/{chatId}.
            // fetch сам его обработает, но браузер страницу не переключит.
            window.location.href = response.url;

        } catch (error) {
            console.error("Ошибка отправки сообщения:", error);

            sendButton.disabled = false;
            chatInput.disabled = false;
        }
    }
});