const Posts = (() => {
    const state = {
        currentPage: 0,
        currentPost: null,
        editMode: false,
        afterSave: null,
    };

    const elements = {};

    function init() {
        elements.postList = UI.$("#postList");
        elements.pagination = UI.$("#pagination");

        elements.postOverlay = UI.$("#postModalOverlay");
        elements.postForm = UI.$("#postCreateForm");
        elements.postModalTitle = UI.$("#postModalTitle");
        elements.postId = UI.$("#postId");
        elements.postTitle = UI.$("#postTitle");
        elements.postContent = UI.$("#postContent");
        elements.postMessage = UI.$("#postCreateMessage");

        elements.detailTitle = UI.$("#detailTitle");
        elements.detailContent = UI.$("#detailContent");
        elements.detailWriter = UI.$("#detailWriter");
        elements.detailDate = UI.$("#detailDate");
        elements.likeButton = UI.$("#likeButton");
        elements.likeCount = UI.$("#likeCount");
        elements.editButton = UI.$("#editPostButton");
        elements.deleteButton = UI.$("#deletePostButton");

        elements.postSubmitButton = UI.$("#postSubmitButton") || elements.postForm?.querySelector("button[type='submit']");
        elements.titleCount = UI.$("#postTitleCount");
        elements.contentCount = UI.$("#postContentCount");

        UI.bindCharacterCount(elements.postTitle, elements.titleCount, 100);
        UI.bindCharacterCount(elements.postContent, elements.contentCount, 6000);
        bindEvents();
    }

    function bindEvents() {
        UI.$("#openPostModalButton")?.addEventListener("click", () => openCreateModal());
        UI.$("#closePostModalButton")?.addEventListener("click", closePostModal);

        elements.postOverlay?.addEventListener("click", event => {
            if (event.target === elements.postOverlay) {
                closePostModal();
            }
        });

        elements.postForm?.addEventListener("submit", handleSubmit);
        elements.likeButton?.addEventListener("click", handleLike);
        elements.editButton?.addEventListener("click", () => openEditModal(state.currentPost));
        elements.deleteButton?.addEventListener("click", deleteCurrentPost);
    }

    async function load(page = 0) {
        state.currentPage = page;
        elements.postList.classList.add("loading");

        try {
            const data = await Api.request(`/posts?page=${page}&size=5`);
            const posts = await attachLikeCounts(data.content || []);
            renderList(posts, data.page);
        } catch (error) {
            UI.clear(elements.postList);
            elements.postList.appendChild(UI.el("p", {
                className: "message",
                text: Api.getErrorMessage(error, "게시글을 불러오지 못했습니다."),
            }));
        } finally {
            requestAnimationFrame(() => {
                elements.postList.classList.remove("loading");
            });
        }
    }

    async function attachLikeCounts(posts) {
        const settled = await Promise.allSettled(
            posts.map(post => Api.request(`/posts/${post.id}`))
        );

        return posts.map((post, index) => {
            const result = settled[index];
            if (result.status === "fulfilled") {
                return {
                    ...post,
                    likeCount: result.value.likeCount || 0,
                };
            }

            return {
                ...post,
                likeCount: 0,
            };
        });
    }

    function renderList(posts, pageInfo) {
        UI.clear(elements.postList);

        if (posts.length === 0) {
            elements.postList.appendChild(UI.el("p", {
                className: "description",
                text: "게시글이 없습니다.",
            }));
        } else {
            posts.forEach(post => elements.postList.appendChild(createPostCard(post)));
        }

        UI.renderPagination(elements.pagination, pageInfo, load);
    }

    function createPostCard(post) {
        const currentUser = Auth.getCurrentUser();
        const isOwner = currentUser && post.userId === currentUser.id;

        const actionGroup = UI.el("div", { className: "post-right" });
        if (isOwner) {
            actionGroup.appendChild(UI.el("button", {
                type: "button",
                className: "edit-post-btn",
                text: "수정",
                onclick: event => {
                    event.stopPropagation();
                    openEditModal(post);
                },
            }));
        }

        actionGroup.appendChild(UI.el("span", {
            className: "post-date",
            text: UI.formatDate(post.createdAt),
        }));

        const meta = UI.el(
            "div",
            { className: "post-card-meta" },
            UI.el("span", { text: `좋아요 ${post.likeCount || 0}` })
        );

        return UI.el(
            "article",
            {
                className: "post-card clickable-card",
                onclick: () => {
                    location.href = `/post.html?id=${post.id}`;
                },
            },
            UI.el("h2", { text: post.title }),
            UI.el("p", { text: post.content }),
            meta,
            UI.el(
                "div",
                { className: "post-footer" },
                UI.el("span", { className: "post-writer", text: `작성자: ${post.name}` }),
                actionGroup
            )
        );
    }

    function openCreateModal() {
        state.editMode = false;
        elements.postModalTitle.textContent = "게시글 작성";
        elements.postForm.reset();
        elements.postId.value = "";
        UI.setMessage(elements.postMessage);
        updateCounts();
        UI.openModal(elements.postOverlay);
        elements.postTitle.focus();
    }

    function openEditModal(post) {
        if (!post) {
            return;
        }

        state.editMode = true;
        elements.postModalTitle.textContent = "게시글 수정";
        elements.postId.value = post.id;
        elements.postTitle.value = post.title || "";
        elements.postContent.value = post.content || "";
        UI.setMessage(elements.postMessage);
        updateCounts();
        UI.openModal(elements.postOverlay);
        elements.postTitle.focus();
    }

    function closePostModal() {
        UI.closeModal(elements.postOverlay);
        elements.postForm.reset();
        UI.setMessage(elements.postMessage);
    }

    async function handleSubmit(event) {
        event.preventDefault();
        UI.setMessage(elements.postMessage);

        const title = elements.postTitle.value.trim();
        const content = elements.postContent.value.trim();

        if (!title || !content) {
            UI.setMessage(elements.postMessage, "제목과 내용을 모두 입력해주세요.");
            return;
        }

        try {
            UI.setBusy(elements.postSubmitButton, true, "저장 중...");
            if (state.editMode) {
                const postId = elements.postId.value;
                await Api.request(`/posts/${postId}`, {
                    method: "PATCH",
                    body: JSON.stringify({ title, content }),
                });
            } else {
                await Api.request("/posts", {
                    method: "POST",
                    body: JSON.stringify({ title, content }),
                });
            }

            closePostModal();
            await handleAfterSave();
        } catch (error) {
            UI.setMessage(elements.postMessage, Api.getErrorMessage(error, "게시글 저장에 실패했습니다."));
        } finally {
            UI.setBusy(elements.postSubmitButton, false);
        }
    }

    async function handleAfterSave() {
        if (typeof state.afterSave === "function") {
            await state.afterSave();
            return;
        }

        await load(state.editMode ? state.currentPage : 0);
    }

    async function loadDetail(postId) {
        try {
            const post = await Api.request(`/posts/${postId}`);
            state.currentPost = post;
            renderDetail(post);
            await Promise.all([
                loadLikeState(postId),
                Comments.load(postId, 0),
            ]);
            return post;
        } catch (error) {
            throw error;
        }
    }

    function renderDetail(post) {
        const currentUser = Auth.getCurrentUser();
        const isOwner = currentUser && post.userId === currentUser.id;

        elements.detailTitle.textContent = post.title;
        elements.detailContent.textContent = post.content;
        elements.detailWriter.textContent = `작성자: ${post.name}`;
        elements.detailDate.textContent = UI.formatDate(post.createdAt);
        elements.likeCount.textContent = `${post.likeCount || 0}개`;
        elements.editButton.hidden = !isOwner;
        elements.deleteButton.hidden = !isOwner;
    }

    async function loadLikeState(postId) {
        const liked = await Likes.loadLikeState(postId);
        elements.likeButton.disabled = liked;
        elements.likeButton.textContent = liked ? "좋아요 완료" : "좋아요";
    }

    async function handleLike() {
        if (!state.currentPost) {
            return;
        }

        try {
            await Likes.likePost(state.currentPost.id);
            state.currentPost.likeCount = (state.currentPost.likeCount || 0) + 1;
            elements.likeCount.textContent = `${state.currentPost.likeCount}개`;
            elements.likeButton.disabled = true;
            elements.likeButton.textContent = "좋아요 완료";
        } catch (error) {
            alert(Api.getErrorMessage(error, "좋아요 처리에 실패했습니다."));
        }
    }

    async function deleteCurrentPost() {
        if (!state.currentPost || !confirm("게시글을 삭제할까요?")) {
            return;
        }

        try {
            UI.setBusy(elements.deleteButton, true, "삭제 중...");
            await Api.request(`/posts/${state.currentPost.id}`, { method: "DELETE" });
            location.href = "/";
        } catch (error) {
            alert(Api.getErrorMessage(error, "게시글 삭제에 실패했습니다."));
        } finally {
            UI.setBusy(elements.deleteButton, false);
        }
    }

    function setAfterSave(callback) {
        state.afterSave = callback;
    }

    function updateCounts() {
        elements.postTitle?.dispatchEvent(new Event("input"));
        elements.postContent?.dispatchEvent(new Event("input"));
    }

    return {
        init,
        load,
        loadDetail,
        openEditModal,
        deleteCurrentPost,
        setAfterSave,
    };
})();
