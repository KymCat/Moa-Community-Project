const API_BASE_URL = "";

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function saveAccessToken(accessToken) {
    localStorage.setItem("accessToken", accessToken);
}

function removeAccessToken() {
    localStorage.removeItem("accessToken");
}

function isPublicUrl(url) {
    return url === "/auth/login"
        || url === "/auth/reissue"
        || url === "/users";
}

async function requestApi(url, options = {}) {
    return sendRequest(url, options, true);
}

async function sendRequest(url, options = {}, retry) {
    const accessToken = getAccessToken();

    const headers = {
        "Content-Type": "application/json",
        ...options.headers,
    };

    if (accessToken && !isPublicUrl(url)) {
        headers.Authorization = `Bearer ${accessToken}`;
    }

    const response = await fetch(API_BASE_URL + url, {
        ...options,
        headers,
        credentials: "include",
    });

    console.log("요청 URL:", url);
    console.log("응답 status:", response.status);

    if (response.status === 401 && retry && !isPublicUrl(url)) {
        console.log("401 발생 → 재발행 시도");

        const reissueSuccess = await reissueAccessToken();

        if (reissueSuccess) {
            return sendRequest(url, options, false);
        }

        removeAccessToken();

        if (location.pathname !== "/login.html") {
            location.href = "/login.html";
        }

        return;
    }

    if (!response.ok) {
        const errorBody = await response.json();
        throw errorBody;
    }

    return parseResponse(response);
}

async function parseResponse(response) {
    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }

    return response.text();
}

async function reissueAccessToken() {
    try {
        const response = await fetch("/auth/reissue", {
            method: "POST",
            credentials: "include",
        });

        console.log("재발행 응답 status:", response.status);

        if (!response.ok) {
            return false;
        }

        const newAccessToken = await parseResponse(response);

        if (!newAccessToken || newAccessToken.trim() === "") {
            return false;
        }

        saveAccessToken(newAccessToken);

        return true;
    } catch (error) {
        console.error("토큰 재발행 실패:", error);
        return false;
    }
}