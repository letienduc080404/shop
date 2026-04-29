(function () {
    const FALLBACK_SCOPE_MESSAGE = "Mình là trợ lý mua sắm, mình có thể hỗ trợ bạn tìm sản phẩm, giá, tồn kho và đơn hàng trong shop.";
    const SUPPORT_CHAT_STORAGE_KEY = "shop_direct_support_key";
    const SUPPORT_CHAT_POLL_MS = 4000;

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

    function setPanelVisible(panelEl, visible) {
        if (!panelEl) {
            return;
        }
        panelEl.classList.toggle("chatbot-hidden", !visible);
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

    async function sendToAdminSupport(conversationKey, originalQuestion) {
        const csrf = getCsrfInfo();
        if (!csrf || !csrf.header || !csrf.token) {
            throw new Error("NO_CSRF");
        }

        const formData = new FormData();
        formData.append("role", "USER");
        formData.append("message", originalQuestion);

        const response = await fetch("/api/chat/" + encodeURIComponent(conversationKey), {
            method: "POST",
            headers: { [csrf.header]: csrf.token },
            body: formData,
            credentials: "same-origin",
        });

        if (!response.ok) {
            throw new Error("ADMIN_CHAT_API_ERROR");
        }
    }

    async function loadSupportMessages(conversationKey) {
        const response = await fetch("/api/chat/" + encodeURIComponent(conversationKey), {
            method: "GET",
            credentials: "same-origin",
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error("UNAUTHORIZED");
        }
        if (!response.ok) {
            throw new Error("LOAD_SUPPORT_FAILED");
        }
        const data = await response.json();
        return Array.isArray(data) ? data : [];
    }

    function renderSupportMessages(container, messages) {
        container.innerHTML = "";
        if (!messages || messages.length === 0) {
            appendMessage(container, "Bạn đã mở chat hỗ trợ trực tiếp. Hãy gửi nội dung để admin hỗ trợ nhanh hơn.", "bot");
            return;
        }

        messages.forEach(function (msg) {
            const sender = msg && msg.senderRole === "USER" ? "user" : "bot";
            appendMessage(container, msg.message || "", sender);
        });
    }

    async function fetchMyOrderCodes() {
        const response = await fetch("/api/chatbot/my-orders", {
            method: "GET",
            credentials: "same-origin",
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error("UNAUTHORIZED");
        }
        if (!response.ok) {
            throw new Error("LOAD_ORDERS_FAILED");
        }
        const data = await response.json();
        return Array.isArray(data) ? data : [];
    }

    function appendOrderPicker(container, orderCodes, originalQuestion, doneCallback) {
        const pickWrap = document.createElement("div");
        pickWrap.className = "chatbot-admin-handoff";

        const title = document.createElement("p");
        title.className = "chatbot-admin-title";
        title.textContent = "Chọn đơn hàng bạn muốn hỗ trợ:";
        pickWrap.appendChild(title);

        const list = document.createElement("div");
        list.className = "chatbot-admin-actions";

        orderCodes.forEach(function (orderCode) {
            const itemBtn = document.createElement("button");
            itemBtn.type = "button";
            itemBtn.className = "chatbot-admin-btn chatbot-order-select-btn";
            itemBtn.textContent = orderCode;
            itemBtn.addEventListener("click", async function () {
                list.querySelectorAll("button").forEach(function (b) { b.disabled = true; });
                try {
                    await sendToAdminSupport("ORDER-" + orderCode.toUpperCase(), originalQuestion);
                    window.location.href = "/order/track/" + encodeURIComponent(orderCode);
                } catch (error) {
                    appendMessage(container, "Không thể chuyển yêu cầu theo đơn hàng lúc này.", "bot");
                    list.querySelectorAll("button").forEach(function (b) { b.disabled = false; });
                }
            });
            list.appendChild(itemBtn);
        });

        pickWrap.appendChild(list);
        container.appendChild(pickWrap);
        container.scrollTop = container.scrollHeight;
        doneCallback();
    }

    function appendAdminHandoffOption(container, originalQuestion, supportChatContext) {
        const wrap = document.createElement("div");
        wrap.className = "chatbot-admin-handoff";

        const title = document.createElement("p");
        title.className = "chatbot-admin-title";
        title.textContent = "Bạn muốn chuyển sang nhắn admin không?";
        wrap.appendChild(title);

        const actions = document.createElement("div");
        actions.className = "chatbot-admin-actions";

        const orderBtn = document.createElement("button");
        orderBtn.type = "button";
        orderBtn.className = "chatbot-admin-btn";
        orderBtn.textContent = "Theo mã đơn";
        actions.appendChild(orderBtn);

        const directBtn = document.createElement("button");
        directBtn.type = "button";
        directBtn.className = "chatbot-admin-btn";
        directBtn.textContent = "Liên hệ trực tiếp";
        actions.appendChild(directBtn);

        wrap.appendChild(actions);

        function setButtonsDisabled(disabled) {
            orderBtn.disabled = disabled;
            directBtn.disabled = disabled;
        }

        orderBtn.addEventListener("click", async function () {
            setButtonsDisabled(true);
            try {
                const orderCodes = await fetchMyOrderCodes();
                if (orderCodes.length === 0) {
                    appendMessage(container, "Bạn chưa có đơn hàng nào để chọn hỗ trợ.", "bot");
                    setButtonsDisabled(false);
                    return;
                }
                appendOrderPicker(container, orderCodes, originalQuestion, function () {
                    setButtonsDisabled(false);
                });
            } catch (error) {
                if (error && error.message === "UNAUTHORIZED") {
                    appendMessage(container, "Bạn cần đăng nhập để chọn đơn hàng hỗ trợ.", "bot");
                } else {
                    appendMessage(container, "Không tải được danh sách đơn hàng lúc này.", "bot");
                }
                setButtonsDisabled(false);
            }
        });

        directBtn.addEventListener("click", async function () {
            setButtonsDisabled(true);
            try {
                const directKey = supportChatContext.getOrCreateConversationKey();
                await sendToAdminSupport(directKey, originalQuestion);
                supportChatContext.open();
                await supportChatContext.sync();
            } catch (error) {
                appendMessage(container, "Không thể gửi yêu cầu liên hệ trực tiếp tới admin lúc này.", "bot");
            } finally {
                setButtonsDisabled(false);
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

    async function sendMessage(message, messagesEl, loadingEl, supportChatContext) {
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
                appendAdminHandoffOption(messagesEl, message, supportChatContext);
            }
        } catch (error) {
            appendMessage(messagesEl, "Xin lỗi, hiện tại chatbot đang gặp lỗi kết nối.", "bot");
            appendAdminHandoffOption(messagesEl, message, supportChatContext);
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
        const reopenSupportBtn = document.getElementById("reopen-support-chat");
        const form = document.getElementById("chatbot-form");
        const input = document.getElementById("chatbot-input");
        const messagesEl = document.getElementById("chatbot-messages");
        const loadingEl = document.getElementById("chatbot-loading");
        const supportPanel = document.getElementById("support-chat-panel");
        const supportCloseBtn = document.getElementById("support-chat-close");
        const supportMessagesEl = document.getElementById("support-chat-messages");
        const supportLoadingEl = document.getElementById("support-chat-loading");
        const supportForm = document.getElementById("support-chat-form");
        const supportInput = document.getElementById("support-chat-input");

        let supportConversationKey = localStorage.getItem(SUPPORT_CHAT_STORAGE_KEY);
        let supportPollTimerId = null;

        function updateReopenSupportButtonVisibility() {
            if (!reopenSupportBtn) {
                return;
            }
            const hasConversation = !!supportConversationKey;
            reopenSupportBtn.classList.toggle("chatbot-hidden", !hasConversation);
        }

        function getOrCreateSupportConversationKey() {
            if (!supportConversationKey) {
                supportConversationKey = "DIRECT-" + Date.now();
                localStorage.setItem(SUPPORT_CHAT_STORAGE_KEY, supportConversationKey);
                updateReopenSupportButtonVisibility();
            }
            return supportConversationKey;
        }

        async function syncSupportMessages() {
            if (!supportConversationKey) {
                return;
            }
            setLoading(true, supportLoadingEl);
            try {
                const messages = await loadSupportMessages(supportConversationKey);
                renderSupportMessages(supportMessagesEl, messages);
            } catch (error) {
                if (error && error.message === "UNAUTHORIZED") {
                    setPanelVisible(supportPanel, false);
                    localStorage.removeItem(SUPPORT_CHAT_STORAGE_KEY);
                    supportConversationKey = null;
                    updateReopenSupportButtonVisibility();
                    appendMessage(messagesEl, "Bạn cần đăng nhập để tiếp tục chat hỗ trợ trực tiếp.", "bot");
                }
            } finally {
                setLoading(false, supportLoadingEl);
            }
        }

        function startSupportPolling() {
            if (supportPollTimerId) {
                clearInterval(supportPollTimerId);
            }
            supportPollTimerId = window.setInterval(syncSupportMessages, SUPPORT_CHAT_POLL_MS);
        }

        function openSupportPanel() {
            setPanelVisible(supportPanel, true);
            startSupportPolling();
            syncSupportMessages();
            if (supportInput) {
                supportInput.focus();
            }
        }

        const supportChatContext = {
            getOrCreateConversationKey: getOrCreateSupportConversationKey,
            open: openSupportPanel,
            sync: syncSupportMessages,
        };

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
            await sendMessage(message, messagesEl, loadingEl, supportChatContext);
        });

        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        if (supportCloseBtn) {
            supportCloseBtn.addEventListener("click", function () {
                setPanelVisible(supportPanel, false);
            });
        }

        if (reopenSupportBtn) {
            reopenSupportBtn.addEventListener("click", function () {
                openSupportPanel();
            });
        }

        if (supportForm) {
            supportForm.addEventListener("submit", async function (event) {
                event.preventDefault();
                const message = supportInput.value.trim();
                if (!message) {
                    return;
                }

                const conversationKey = getOrCreateSupportConversationKey();
                appendMessage(supportMessagesEl, message, "user");
                supportInput.value = "";
                try {
                    await sendToAdminSupport(conversationKey, message);
                    await syncSupportMessages();
                } catch (error) {
                    appendMessage(supportMessagesEl, "Không gửi được tin nhắn hỗ trợ lúc này.", "bot");
                }
            });
        }

        if (supportConversationKey) {
            openSupportPanel();
        }

        updateReopenSupportButtonVisibility();

        window.addEventListener("beforeunload", function () {
            if (supportPollTimerId) {
                clearInterval(supportPollTimerId);
            }
        });
    });
})();
