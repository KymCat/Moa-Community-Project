const MyPage = (() => {
    const tabNames = ["profile", "posts", "comments"];

    async function init() {
        await Auth.requireLogin();
        const user = await Auth.loadCurrentUser();

        renderUser(user);
        bindTabs();
        bindAccountActions();

        await activateTab(getInitialTab(), false);
    }

    function renderUser(user) {
        UI.$("#mypageUserId").textContent = user.id;
        UI.$("#mypageUserName").textContent = user.name;
        UI.$("#mypageNewName").value = user.name;
    }

    function bindTabs() {
        const tabs = getTabs();

        tabs.forEach(tab => {
            tab.addEventListener("click", () => activateTab(tab.dataset.tab));
            tab.addEventListener("keydown", event => handleTabKeydown(event, tabs));
        });
    }

    function getTabs() {
        return [...document.querySelectorAll("[role='tab'][data-tab]")];
    }

    function getInitialTab() {
        const tabName = location.hash.replace("#", "");
        return tabNames.includes(tabName) ? tabName : "profile";
    }

    async function activateTab(tabName, updateHash = true) {
        const tabs = getTabs();
        const panels = [...document.querySelectorAll("[role='tabpanel'][data-panel]")];

        tabs.forEach(tab => {
            const active = tab.dataset.tab === tabName;
            tab.classList.toggle("active", active);
            tab.setAttribute("aria-selected", String(active));
            tab.tabIndex = active ? 0 : -1;
        });

        panels.forEach(panel => {
            const active = panel.dataset.panel === tabName;
            panel.classList.toggle("active", active);
            panel.hidden = !active;
        });

        if (updateHash) {
            history.replaceState(null, "", `#${tabName}`);
        }

        if (tabName === "posts") {
            await loadMyPosts(0);
        }

        if (tabName === "comments") {
            await loadMyComments(0);
        }
    }

    function handleTabKeydown(event, tabs) {
        const currentIndex = tabs.indexOf(event.currentTarget);
        let nextIndex = currentIndex;

        if (event.key === "ArrowRight") {
            nextIndex = (currentIndex + 1) % tabs.length;
        } else if (event.key === "ArrowLeft") {
            nextIndex = (currentIndex - 1 + tabs.length) % tabs.length;
        } else if (event.key === "Home") {
            nextIndex = 0;
        } else if (event.key === "End") {
            nextIndex = tabs.length - 1;
        } else {
            return;
        }

        event.preventDefault();
        tabs[nextIndex].focus();
        activateTab(tabs[nextIndex].dataset.tab);
    }

    function bindAccountActions() {
        const passwordForm = UI.$("#mypagePasswordForm");

        UI.$("#mypageLogoutButton").addEventListener("click", () => Auth.logout());
        UI.$("#mypageDeleteAccountButton").addEventListener("click", deleteAccount);
        UI.$("#mypageNameForm").addEventListener("submit", updateName);
        passwordForm.addEventListener("submit", updatePassword);
        passwordForm.addEventListener("invalid", showPasswordValidationError, true);
        passwordForm.addEventListener("input", clearPasswordMessage);
        document.querySelectorAll("[data-password-target]")
            .forEach(button => button.addEventListener("click", togglePasswordVisibility));
    }

    function togglePasswordVisibility(event) {
        const button = event.currentTarget;
        const input = document.getElementById(button.dataset.passwordTarget);
        const visible = input.type === "text";
        const fieldName = input.id === "mypageCurrentPassword" ? "현재 비밀번호" : "새 비밀번호";

        input.type = visible ? "password" : "text";
        button.textContent = visible ? "👁" : "🙈";
        button.setAttribute("aria-pressed", String(!visible));
        button.setAttribute("aria-label", `${fieldName} ${visible ? "표시" : "숨기기"}`);
        input.focus();
    }

    function showPasswordValidationError(event) {
        const message = UI.$("#mypagePasswordMessage");
        const errorMessage = event.target.validity.valueMissing
            ? "현재 비밀번호와 새 비밀번호를 모두 입력해주세요."
            : "비밀번호는 8자 이상 15자 이하로 입력해주세요.";

        UI.setMessage(message, errorMessage, true);
    }

    function clearPasswordMessage() {
        UI.setMessage(UI.$("#mypagePasswordMessage"));
    }

    async function updateName(event) {
        event.preventDefault();

        const form = event.currentTarget;
        const input = UI.$("#mypageNewName");
        const message = UI.$("#mypageNameMessage");
        const submitButton = form.querySelector("button[type='submit']");
        const name = input.value.trim();

        UI.setMessage(message);

        try {
            UI.setBusy(submitButton, true, "저장 중...");
            await Auth.updateName(name);
            UI.$("#mypageUserName").textContent = name;
            UI.setMessage(message, "닉네임이 변경되었습니다.", false);
        } catch (error) {
            UI.setMessage(message, Api.getErrorMessage(error, "닉네임 변경에 실패했습니다."));
        } finally {
            UI.setBusy(submitButton, false);
        }
    }

    async function updatePassword(event) {
        event.preventDefault();

        const form = event.currentTarget;
        const currentPasswordInput = UI.$("#mypageCurrentPassword");
        const newPasswordInput = UI.$("#mypageNewPassword");
        const message = UI.$("#mypagePasswordMessage");
        const submitButton = form.querySelector("button[type='submit']");

        UI.setMessage(message);

        try {
            UI.setBusy(submitButton, true, "저장 중...");
            await Auth.updatePassword(currentPasswordInput.value, newPasswordInput.value);
            form.reset();
            UI.setMessage(message, "비밀번호가 변경되었습니다.", false);
        } catch (error) {
            UI.setMessage(
                message,
                Api.getErrorMessage(error, "비밀번호 변경에 실패했습니다."),
                true
            );
        } finally {
            UI.setBusy(submitButton, false);
        }
    }

    async function deleteAccount() {
        const deleteButton = UI.$("#mypageDeleteAccountButton");
        const confirmed = confirm("정말 탈퇴할까요? 탈퇴 후에는 계정을 복구할 수 없습니다.");

        if (!confirmed) {
            return;
        }

        try {
            UI.setBusy(deleteButton, true, "탈퇴 처리 중...");
            await Auth.deleteAccount();
            location.href = "/signup.html";
        } catch (error) {
            alert(Api.getErrorMessage(error, "회원 탈퇴에 실패했습니다."));
            UI.setBusy(deleteButton, false);
        }
    }

    async function loadMyPosts(page) {
        const list = UI.$("#myPostList");
        const pagination = UI.$("#myPostPagination");

        renderLoading(list, "작성한 게시글을 불러오는 중입니다...");

        try {
            const data = await Api.request(`/posts/me?page=${page}`);
            renderMyPosts(data.content || []);
            UI.$("#myPostCount").textContent = `${data.page.totalElements}개`;
            UI.renderPagination(pagination, data.page, loadMyPosts);
        } catch (error) {
            renderError(list, Api.getErrorMessage(error, "작성한 게시글을 불러오지 못했습니다."));
            UI.clear(pagination);
        }
    }

    function renderMyPosts(posts) {
        const list = UI.$("#myPostList");
        UI.clear(list);

        if (posts.length === 0) {
            renderEmpty(list, "아직 작성한 게시글이 없습니다.", "첫 게시글을 작성해보세요.");
            return;
        }

        posts.forEach(post => {
            list.appendChild(UI.el(
                "a",
                { className: "mypage-list-card", href: `/post.html?id=${post.id}` },
                UI.el("div", { className: "mypage-list-main" },
                    UI.el("h3", { text: post.title }),
                    UI.el("p", { text: post.content })
                ),
                UI.el("div", { className: "mypage-list-meta" },
                    UI.el("span", { text: UI.formatDate(post.createdAt) }),
                    UI.el("span", { className: "list-link-label", text: "게시글 보기 →" })
                )
            ));
        });
    }

    async function loadMyComments(page) {
        const list = UI.$("#myCommentList");
        const pagination = UI.$("#myCommentPagination");

        renderLoading(list, "작성한 댓글을 불러오는 중입니다...");

        try {
            const data = await Api.request(`/comments/me?page=${page}`);
            renderMyComments(data.content || []);
            UI.$("#myCommentCount").textContent = `${data.page.totalElements}개`;
            UI.renderPagination(pagination, data.page, loadMyComments);
        } catch (error) {
            renderError(list, Api.getErrorMessage(error, "작성한 댓글을 불러오지 못했습니다."));
            UI.clear(pagination);
        }
    }

    function renderMyComments(comments) {
        const list = UI.$("#myCommentList");
        UI.clear(list);

        if (comments.length === 0) {
            renderEmpty(list, "아직 작성한 댓글이 없습니다.", "게시글에서 의견을 남겨보세요.");
            return;
        }

        comments.forEach(comment => {
            list.appendChild(UI.el(
                "a",
                { className: "mypage-list-card comment-history-card", href: `/post.html?id=${comment.post_id}` },
                UI.el("div", { className: "mypage-list-main" },
                    UI.el("p", { className: "comment-history-content", text: comment.content })
                ),
                UI.el("div", { className: "mypage-list-meta" },
                    UI.el("span", { text: UI.formatDate(comment.createdAt) }),
                    UI.el("span", { className: "list-link-label", text: `게시글 #${comment.post_id} →` })
                )
            ));
        });
    }

    function renderLoading(list, message) {
        UI.clear(list);
        list.appendChild(UI.el("p", { className: "mypage-state", text: message }));
    }

    function renderError(list, message) {
        UI.clear(list);
        list.appendChild(UI.el("p", { className: "message mypage-state", text: message }));
    }

    function renderEmpty(list, title, description) {
        list.appendChild(UI.el(
            "div",
            { className: "empty-state" },
            UI.el("strong", { text: title }),
            UI.el("p", { text: description })
        ));
    }

    return { init };
})();

document.addEventListener("DOMContentLoaded", () => {
    MyPage.init().catch(error => console.error(error));
});
