document.addEventListener("DOMContentLoaded", async () => {
    await checkLogin();

    initUserMenu();
    initLogoutButton();

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
        const data = await requestApi(`/posts?page=${page}&size=5`);

        console.log("게시글 응답:", data);

        const posts = data.content ?? [];
        const pageInfo = data.page;

        if (posts.length === 0) {
            postList.innerHTML = `<p class="description">게시글이 없습니다.</p>`;
            renderPagination(pageInfo);
            return;
        }

        postList.innerHTML = posts.map(post => `
            <article class="post-card">
                <h2>${post.title}</h2>
                <p>${post.content}</p>
        
                <div class="post-meta">
                    <span>작성자: ${post.name}</span>
                    <span>${post.createdAt}</span>
                </div>
        
                ${
                    post.userId === currentUserId
                        ? `<button class="edit-post-btn" data-post-id="${post.id}">
                               수정
                           </button>`
                        : ""
                }
            </article>
        `).join("");

        renderPagination(pageInfo);

    } catch (error) {
        console.error("게시글 조회 실패:", error);
        postList.innerHTML = `<p class="message">게시글을 불러오지 못했습니다.</p>`;
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