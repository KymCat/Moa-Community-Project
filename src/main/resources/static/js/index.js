document.addEventListener("DOMContentLoaded", async () => {
    await checkLogin();

    initUserMenu();
    initLogoutButton();
    initPostCreateModal();

    await loadUserInfo();
    await loadPosts();

});

async function checkLogin() {
    const accessToken = getAccessToken();

    if (accessToken && accessToken.trim() !== "") {
        return;
    }

    console.log("토큰 없음 → 재발행 시도");

    const reissueSuccess = await reissueAccessToken();

    if (!reissueSuccess) {
        console.log("재발행 실패 → 로그인 페이지 이동");
        location.href = "/login.html";
    }
}

function initUserMenu() {
    const userMenuButton = document.getElementById("userMenuButton");
    const userCard = document.getElementById("userCard");

    userMenuButton.addEventListener("click", () => {
        userCard.classList.toggle("active");
    });
}

let currentUserId = null;
async function loadUserInfo() {
    const userId = document.getElementById("userId");
    const userName = document.getElementById("userName");

    try {
        const user = await requestApi("/users/me");

        currentUserId = user.id;

        userId.textContent = user.id;
        userName.textContent = user.name;
    } catch (error) {
        console.error("유저 정보 조회 실패:", error);
    }
}

async function loadPosts(page = 0) {
    const postList = document.getElementById("postList");

    try {
        postList.classList.add("loading");

        await new Promise(resolve => setTimeout(resolve, 200));

        const data = await requestApi(`/posts?page=${page}&size=5`);

        const posts = data.content ?? [];
        const pageInfo = data.page;

        if (posts.length === 0) {
            postList.innerHTML = `<p class="description">게시글이 없습니다.</p>`;
        } else {
            postList.innerHTML = posts.map(post => `
                <article class="post-card">
                    <h2>${post.title}</h2>
                    <p>${post.content}</p>
            
                    <div class="post-footer">
                        <span class="post-writer">작성자: ${post.name}</span>
            
                        <div class="post-right">
                            ${
                            post.userId === currentUserId
                                ? `<button class="edit-post-btn" data-post-id="${post.id}">
                                           수정
                                       </button>`
                                : ""
                        }
            
                            <span class="post-date">${formatDate(post.createdAt)}</span>
                        </div>
                    </div>
                </article>
            `).join("");
        }

        renderPagination(pageInfo);

        requestAnimationFrame(() => {
            postList.classList.remove("loading");
        });

    } catch (error) {
        console.error("게시글 조회 실패:", error);
        postList.innerHTML = `<p class="message">게시글을 불러오지 못했습니다.</p>`;
        postList.classList.remove("loading");
    }
}

function initLogoutButton() {
    const logoutButton = document.getElementById("logoutButton");

    if (!logoutButton) {
        console.log("logoutButton 없음");
        return;
    }

    logoutButton.addEventListener("click", async () => {
        try {
            await requestApi("/auth/logout", {
                method: "POST",
            });

            removeAccessToken();

            alert("로그아웃 되었습니다.");
            location.href = "/login.html";
        } catch (error) {
            console.error("로그아웃 실패:", error);

            removeAccessToken();

            alert("로그아웃 처리 중 문제가 발생했지만, 로컬 토큰은 삭제했습니다.");
            location.href = "/login.html";
        }
    });
}

function renderPagination(pageInfo) {
    const pagination = document.getElementById("pagination");

    if (!pagination || !pageInfo) {
        return;
    }

    pagination.innerHTML = `
        <button id="prevPage" ${pageInfo.number === 0 ? "disabled" : ""}>
            이전
        </button>

        <span>${pageInfo.number + 1} / ${pageInfo.totalPages}</span>

        <button id="nextPage" ${pageInfo.number + 1 >= pageInfo.totalPages ? "disabled" : ""}>
            다음
        </button>
    `;

    document.getElementById("prevPage").addEventListener("click", () => {
        loadPosts(pageInfo.number - 1);
    });

    document.getElementById("nextPage").addEventListener("click", () => {
        loadPosts(pageInfo.number + 1);
    });
}

function formatDate(dateString) {
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();

    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");

    return `${year}년 ${month}월 ${day}일 ${hour}:${minute}`;
}

function initPostCreateModal() {
    const openButton = document.getElementById("openPostModalButton");
    const closeButton = document.getElementById("closePostModalButton");
    const overlay = document.getElementById("postModalOverlay");
    const form = document.getElementById("postCreateForm");
    const titleInput = document.getElementById("postTitle");
    const contentInput = document.getElementById("postContent");
    const message = document.getElementById("postCreateMessage");

    openButton.addEventListener("click", () => {
        overlay.classList.add("active");
        message.textContent = "";
        titleInput.focus();
    });

    closeButton.addEventListener("click", () => {
        closePostModal();
    });

    overlay.addEventListener("click", (event) => {
        if (event.target === overlay) {
            closePostModal();
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const title = titleInput.value.trim();
        const content = contentInput.value.trim();

        try {
            await requestApi("/posts", {
                method: "POST",
                body: JSON.stringify({
                    title,
                    content,
                }),
            });

            closePostModal();

            await loadPosts(0);
        } catch (error) {
            console.error("게시글 작성 실패:", error);
            message.textContent = error.message ?? "게시글 작성에 실패했습니다.";
        }
    });

    function closePostModal() {
        overlay.classList.remove("active");
        form.reset();
        message.textContent = "";
    }
}