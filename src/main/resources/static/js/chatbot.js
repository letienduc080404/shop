(function () {
    const FALLBACK_SCOPE_MESSAGE = "Mình là trợ lý mua sắm, mình có thể hỗ trợ bạn tìm sản phẩm, giá, tồn kho và đơn hàng trong shop.";

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
        return div;
    }

    function shouldShowAdminHandoff(answer) {
        if (!answer) {
            return true;
        }
        const normalized = answer.trim().toLowerCase();
        return normalized === FALLBACK_SCOPE_MESSAGE.toLowerCase()
            || normalized.includes("mình chưa có câu trả lời phù hợp")
            || normalized.includes("xin lỗi")
            || normalized.includes("không chắc");
    }

    async function sendToAdminSupport(orderCode, originalQuestion) {
        const csrf = getCsrfInfo();
        if (!csrf || !csrf.header || !csrf.token) {
            throw new Error("NO_CSRF");
        }

        const formData = new FormData();
        formData.append("role", "USER");
        formData.append("message", "Khách cần hỗ trợ từ chatbot. Câu hỏi: " + originalQuestion);

        const response = await fetch("/api/chat/" + encodeURIComponent(orderCode), {
            method: "POST",
            headers: { [csrf.header]: csrf.token },
            body: formData,
            credentials: "same-origin",
        });

        if (!response.ok) {
            throw new Error("ADMIN_CHAT_API_ERROR");
        }
    }

    function appendAdminHandoffOption(container, originalQuestion) {
        const wrap = document.createElement("div");
        wrap.className = "chatbot-admin-handoff";

        const title = document.createElement("p");
        title.className = "chatbot-admin-title";
        title.textContent = "Bạn muốn chuyển sang nhắn admin không?";
        wrap.appendChild(title);

        const button = document.createElement("button");
        button.type = "button";
        button.className = "chatbot-admin-btn";
        button.textContent = "Nhắn admin";
        wrap.appendChild(button);

        button.addEventListener("click", async function () {
            const orderCodeInput = window.prompt("Nhập mã đơn hàng để nhắn admin (ví dụ: ORD-ABC12345):");
            const orderCode = (orderCodeInput || "").trim();
            if (!orderCode) {
                return;
            }

            button.disabled = true;
            button.textContent = "Đang chuyển...";
            try {
                await sendToAdminSupport(orderCode, originalQuestion);
                window.location.href = "/order/track/" + encodeURIComponent(orderCode);
            } catch (error) {
                appendMessage(container, "Không thể chuyển sang chat admin lúc này. Bạn vui lòng đăng nhập và kiểm tra lại mã đơn.", "bot");
            } finally {
                button.disabled = false;
                button.textContent = "Nhắn admin";
            }
        });

        container.appendChild(wrap);
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
            if (shouldShowAdminHandoff(answer)) {
                appendAdminHandoffOption(messagesEl, message);
            }
        } catch (error) {
            appendMessage(messagesEl, "Xin lỗi, hiện tại chatbot đang gặp lỗi kết nối.", "bot");
            appendAdminHandoffOption(messagesEl, message);
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
