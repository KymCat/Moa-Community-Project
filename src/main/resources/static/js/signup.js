const signupForm = document.getElementById("signupForm");
const message = document.getElementById("message");

signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const id = document.getElementById("loginId").value;
    const password = document.getElementById("password").value;
    const name = document.getElementById("name").value;

    try {
        await requestApi("/users", {
            method: "POST",
            body: JSON.stringify({
                id,
                password,
                name,
            }),
        });

        alert("회원가입 성공! 로그인해주세요.");
        location.href = "/login.html";
    } catch (error) {
        console.error("회원가입 실패:", error);
        message.textContent = error.message;
    }
});