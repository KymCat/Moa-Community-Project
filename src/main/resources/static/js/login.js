const loginForm = document.getElementById("loginForm");
const message = document.getElementById("message");

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = document.getElementById("loginId").value;
    const password = document.getElementById("password").value;

    try {
        console.log("로그인 요청 시작");

        const accessToken = await requestApi("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                id,
                password,
            }),
        });

        console.log("로그인 응답 accessToken:", accessToken);
        console.log("응답 타입:", typeof accessToken);

        if (!accessToken || accessToken.trim() === "") {
            throw new Error("accessToken이 비어있음");
        }

        saveAccessToken(accessToken);

        console.log("저장 직후 accessToken:", localStorage.getItem("accessToken"));

        location.href = "/";
    } catch (error) {
        console.error("로그인 실패:", error);
        message.textContent = error.message;
    }
});