const Auth = (() => {
    let currentUser = null;

    async function requireLogin() {
        if (Api.getAccessToken()) {
            return;
        }

        const reissued = await Api.reissueAccessToken();
        if (!reissued) {
            redirectToLoginRequired();
            throw new Error("로그인이 필요합니다.");
        }
    }

    async function loadCurrentUser() {
        currentUser = await Api.request("/users/me");
        return currentUser;
    }

    function getCurrentUser() {
        return currentUser;
    }

    async function logout() {
        try {
            await Api.request("/auth/logout", { method: "POST" });
        } finally {
            Api.removeAccessToken();
            location.href = "/login.html";
        }
    }

    async function updateName(name) {
        await Api.request("/users/me/name", {
            method: "PATCH",
            body: JSON.stringify({ name }),
        });

        if (currentUser) {
            currentUser.name = name;
        }
    }

    async function updatePassword(currentPassword, newPassword) {
        await Api.request("/users/me/password", {
            method: "PATCH",
            body: JSON.stringify({ currentPassword, newPassword }),
        });
    }

    async function deleteAccount() {
        await Api.request("/users/me", { method: "DELETE" });
        Api.removeAccessToken();
    }

    function initAccountSettings(onUserUpdated) {
        const overlay = UI.$("#accountModalOverlay");
        if (!overlay) {
            return;
        }

        const openButton = UI.$("#openAccountModalButton");
        const closeButton = UI.$("#closeAccountModalButton");
        const deleteButton = UI.$("#deleteAccountButton");
        const nameForm = UI.$("#nameUpdateForm");
        const passwordForm = UI.$("#passwordUpdateForm");
        const nameInput = UI.$("#newName");
        const currentPasswordInput = UI.$("#currentPassword");
        const newPasswordInput = UI.$("#newPassword");
        const message = UI.$("#accountMessage");

        openButton?.addEventListener("click", () => {
            nameInput.value = currentUser?.name || "";
            passwordForm.reset();
            UI.setMessage(message);
            UI.openModal(overlay);
            nameInput.focus();
        });

        closeButton?.addEventListener("click", () => UI.closeModal(overlay));
        overlay.addEventListener("click", event => {
            if (event.target === overlay) {
                UI.closeModal(overlay);
            }
        });

        nameForm?.addEventListener("submit", async event => {
            event.preventDefault();
            UI.setMessage(message);
            const submitButton = nameForm.querySelector("button[type='submit']");
            const name = nameInput.value.trim();

            if (!name) {
                UI.setMessage(message, "닉네임을 입력해주세요.");
                return;
            }

            try {
                UI.setBusy(submitButton, true);
                await updateName(name);
                onUserUpdated?.(currentUser);
                UI.setMessage(message, "닉네임이 변경되었습니다.", false);
            } catch (error) {
                UI.setMessage(message, Api.getErrorMessage(error, "닉네임 변경에 실패했습니다."));
            } finally {
                UI.setBusy(submitButton, false);
            }
        });

        passwordForm?.addEventListener("submit", async event => {
            event.preventDefault();
            UI.setMessage(message);
            const submitButton = passwordForm.querySelector("button[type='submit']");
            const currentPassword = currentPasswordInput.value;
            const newPassword = newPasswordInput.value;

            if (!currentPassword || !newPassword) {
                UI.setMessage(message, "현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
                return;
            }

            try {
                UI.setBusy(submitButton, true);
                await updatePassword(currentPassword, newPassword);
                passwordForm.reset();
                UI.setMessage(message, "비밀번호가 변경되었습니다.", false);
            } catch (error) {
                UI.setMessage(message, Api.getErrorMessage(error, "비밀번호 변경에 실패했습니다."));
            } finally {
                UI.setBusy(submitButton, false);
            }
        });

        deleteButton?.addEventListener("click", async () => {
            const confirmed = confirm("정말 탈퇴할까요? 작성한 게시글과 댓글 처리 방식은 서버 정책을 따릅니다.");
            if (!confirmed) {
                return;
            }

            try {
                UI.setBusy(deleteButton, true, "탈퇴 처리 중...");
                await deleteAccount();
                location.href = "/signup.html";
            } catch (error) {
                alert(Api.getErrorMessage(error, "회원 탈퇴에 실패했습니다."));
            } finally {
                UI.setBusy(deleteButton, false);
            }
        });
    }

    function initLoginPage() {
        const form = UI.$("#loginForm");
        if (!form) {
            return;
        }

        const message = UI.$("#message");
        showLoginPageNotice(message);

        UI.$("#goSignupButton")?.addEventListener("click", () => {
            location.href = "/signup.html";
        });

        form.addEventListener("submit", async event => {
            event.preventDefault();
            UI.setMessage(message);

            const id = UI.$("#loginId").value.trim();
            const password = UI.$("#password").value;

            try {
                const accessToken = await Api.request("/auth/login", {
                    method: "POST",
                    body: JSON.stringify({ id, password }),
                });

                if (!accessToken || accessToken.trim() === "") {
                    throw new Error("Access Token이 비어 있습니다.");
                }

                Api.saveAccessToken(accessToken);
                location.href = "/";
            } catch (error) {
                UI.setMessage(message, Api.getErrorMessage(error, "로그인에 실패했습니다."));
            }
        });
    }

    function initSignupPage() {
        const form = UI.$("#signupForm");
        if (!form) {
            return;
        }

        const message = UI.$("#message");
        UI.$("#goLoginButton")?.addEventListener("click", () => {
            location.href = "/login.html";
        });

        form.addEventListener("submit", async event => {
            event.preventDefault();
            UI.setMessage(message);

            const id = UI.$("#loginId").value.trim();
            const password = UI.$("#password").value;
            const name = UI.$("#name").value.trim();

            try {
                await Api.request("/users", {
                    method: "POST",
                    body: JSON.stringify({ id, password, name }),
                });

                UI.setMessage(message, "회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.", false);
                setTimeout(() => {
                    location.href = "/login.html";
                }, 600);
            } catch (error) {
                UI.setMessage(message, Api.getErrorMessage(error, "회원가입에 실패했습니다."));
            }
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        initLoginPage();
        initSignupPage();
    });

    function redirectToLoginRequired() {
        const returnUrl = encodeURIComponent(location.pathname + location.search);
        location.href = `/login.html?reason=login-required&returnUrl=${returnUrl}`;
    }

    function showLoginPageNotice(messageElement) {
        const reason = UI.getQueryParam("reason");
        if (reason !== "login-required") {
            return;
        }

        UI.setMessage(messageElement, "로그인이 필요한 서비스입니다.");
    }

    return {
        requireLogin,
        loadCurrentUser,
        getCurrentUser,
        logout,
        updateName,
        updatePassword,
        deleteAccount,
        initAccountSettings,
    };
})();
