const UI = (() => {
    function $(selector, root = document) {
        return root.querySelector(selector);
    }

    function clear(element) {
        if (!element) {
            return;
        }

        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    function append(parent, children) {
        children.flat().forEach(child => {
            if (child === null || child === undefined || child === false) {
                return;
            }

            parent.appendChild(
                child instanceof Node ? child : document.createTextNode(String(child))
            );
        });
    }

    function el(tagName, attributes = {}, ...children) {
        const element = document.createElement(tagName);

        Object.entries(attributes).forEach(([key, value]) => {
            if (value === null || value === undefined || value === false) {
                return;
            }

            if (key === "className") {
                element.className = value;
                return;
            }

            if (key === "text") {
                element.textContent = value;
                return;
            }

            if (key.startsWith("on") && typeof value === "function") {
                element.addEventListener(key.slice(2).toLowerCase(), value);
                return;
            }

            if (key === "dataset") {
                Object.entries(value).forEach(([dataKey, dataValue]) => {
                    element.dataset[dataKey] = dataValue;
                });
                return;
            }

            element.setAttribute(key, value);
        });

        append(element, children);
        return element;
    }

    function setMessage(element, message = "", isError = true) {
        if (!element) {
            return;
        }

        element.textContent = message;
        element.classList.toggle("success-message", !isError && message !== "");
    }

    function setBusy(button, busy, busyText = "처리 중...") {
        if (!button) {
            return;
        }

        if (busy) {
            button.dataset.originalText = button.textContent;
            button.textContent = busyText;
            button.disabled = true;
            return;
        }

        button.textContent = button.dataset.originalText || button.textContent;
        button.disabled = false;
        delete button.dataset.originalText;
    }

    function openModal(overlay) {
        overlay?.classList.add("active");
    }

    function closeModal(overlay) {
        overlay?.classList.remove("active");
    }

    function formatDate(dateString) {
        if (!dateString) {
            return "";
        }

        const date = new Date(dateString);
        if (Number.isNaN(date.getTime())) {
            return "";
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hour = String(date.getHours()).padStart(2, "0");
        const minute = String(date.getMinutes()).padStart(2, "0");

        return `${year}.${month}.${day} ${hour}:${minute}`;
    }

    function renderPagination(container, pageInfo, onMove) {
        clear(container);
        if (!container || !pageInfo || pageInfo.totalPages <= 1) {
            return;
        }

        const prevButton = el("button", {
            type: "button",
            text: "이전",
            disabled: pageInfo.number === 0,
            onclick: () => onMove(pageInfo.number - 1),
        });

        const nextButton = el("button", {
            type: "button",
            text: "다음",
            disabled: pageInfo.number + 1 >= pageInfo.totalPages,
            onclick: () => onMove(pageInfo.number + 1),
        });

        const label = el("span", {
            text: `${pageInfo.number + 1} / ${pageInfo.totalPages}`,
        });

        append(container, [prevButton, label, nextButton]);
    }

    function bindCharacterCount(input, counter, maxLength) {
        if (!input || !counter) {
            return;
        }

        const update = () => {
            counter.textContent = `${input.value.length} / ${maxLength}`;
        };

        input.addEventListener("input", update);
        update();
    }

    function getQueryParam(name) {
        return new URLSearchParams(location.search).get(name);
    }

    return {
        $,
        clear,
        el,
        setMessage,
        setBusy,
        openModal,
        closeModal,
        formatDate,
        renderPagination,
        bindCharacterCount,
        getQueryParam,
    };
})();
