(function () {
    function getCsrfInfo() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        if (!tokenMeta || !headerMeta) {
            return null;
        }
        return {
            header: headerMeta.getAttribute("content"),
            token: tokenMeta.getAttribute("content"),
        };
    }

    function appendMessage(container, text, sender) {
        const div = document.createElement("div");
        div.classList.add("chatbot-message");
        div.classList.add(sender === "user" ? "chatbot-message-user" : "chatbot-message-bot");
        div.textContent = text;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
    }

    function setLoading(isLoading, loadingEl) {
        if (isLoading) {
            loadingEl.classList.remove("chatbot-hidden");
        } else {
            loadingEl.classList.add("chatbot-hidden");
        }
    }

    async function sendMessage(message, messagesEl, loadingEl) {
        const csrf = getCsrfInfo();
        const headers = {
            "Content-Type": "application/json",
        };
        if (csrf && csrf.header && csrf.token) {
            headers[csrf.header] = csrf.token;
        }

        setLoading(true, loadingEl);
        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                headers: headers,
                body: JSON.stringify({ message: message }),
            });

            if (!response.ok) {
                throw new Error("API_ERROR");
            }

            const data = await response.json();
            const answer = data && typeof data.answer === "string" ? data.answer : "Mình chưa có câu trả lời phù hợp.";
            appendMessage(messagesEl, answer, "bot");
        } catch (error) {
            appendMessage(messagesEl, "Xin lỗi, hiện tại chatbot đang gặp lỗi kết nối.", "bot");
        } finally {
            setLoading(false, loadingEl);
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        const widget = document.getElementById("chatbot-widget");
        if (!widget) {
            return;
        }

        const toggleBtn = document.getElementById("chatbot-toggle");
        const panel = document.getElementById("chatbot-panel");
        const closeBtn = document.getElementById("chatbot-close");
        const form = document.getElementById("chatbot-form");
        const input = document.getElementById("chatbot-input");
        const messagesEl = document.getElementById("chatbot-messages");
        const loadingEl = document.getElementById("chatbot-loading");

        toggleBtn.addEventListener("click", function () {
            panel.classList.toggle("chatbot-hidden");
            if (!panel.classList.contains("chatbot-hidden")) {
                input.focus();
            }
        });

        closeBtn.addEventListener("click", function () {
            panel.classList.add("chatbot-hidden");
        });

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            const message = input.value.trim();
            if (!message) {
                return;
            }
            appendMessage(messagesEl, message, "user");
            input.value = "";
            await sendMessage(message, messagesEl, loadingEl);
        });

        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                form.requestSubmit();
            }
        });
    });
})();
