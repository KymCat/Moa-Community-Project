const Api = (() => {
    const API_BASE_URL = "";
    const ACCESS_TOKEN_KEY = "accessToken";

    function getAccessToken() {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    }

    function saveAccessToken(accessToken) {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    }

    function removeAccessToken() {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
    }

    function isPublicUrl(url) {
        const path = url.split("?")[0];
        return path === "/auth/login"
            || path === "/auth/reissue"
            || path === "/users";
    }

    async function request(url, options = {}, retry = true) {
        const headers = new Headers(options.headers || {});

        if (options.body && !headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }

        const accessToken = getAccessToken();
        if (accessToken && !isPublicUrl(url)) {
            headers.set("Authorization", `Bearer ${accessToken}`);
        }

        const response = await fetch(API_BASE_URL + url, {
            ...options,
            headers,
            credentials: "include",
        });

        if (response.status === 401 && retry && !isPublicUrl(url)) {
            const reissued = await reissueAccessToken();
            if (reissued) {
                return request(url, options, false);
            }

            removeAccessToken();
            if (location.pathname !== "/login.html") {
                const returnUrl = encodeURIComponent(location.pathname + location.search);
                location.href = `/login.html?reason=login-required&returnUrl=${returnUrl}`;
            }
            throw new Error("로그인이 필요합니다.");
        }

        if (!response.ok) {
            throw await parseError(response);
        }

        return parseResponse(response);
    }

    async function parseResponse(response) {
        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            return response.json();
        }

        return response.text();
    }

    async function parseError(response) {
        const contentType = response.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            const body = await response.json();
            return {
                status: response.status,
                code: body.code,
                message: body.message || "요청 처리에 실패했습니다.",
                path: body.path,
            };
        }

        return {
            status: response.status,
            message: await response.text() || "요청 처리에 실패했습니다.",
        };
    }

    async function reissueAccessToken() {
        try {
            const response = await fetch(API_BASE_URL + "/auth/reissue", {
                method: "POST",
                credentials: "include",
            });

            if (!response.ok) {
                return false;
            }

            const accessToken = await parseResponse(response);
            if (!accessToken || accessToken.trim() === "") {
                return false;
            }

            saveAccessToken(accessToken);
            return true;
        } catch {
            return false;
        }
    }

    function getErrorMessage(error, fallback = "요청 처리에 실패했습니다.") {
        return error?.message || fallback;
    }

    return {
        request,
        getAccessToken,
        saveAccessToken,
        removeAccessToken,
        reissueAccessToken,
        getErrorMessage,
    };
})();
