const Comments = (() => {
    const state = {
        postId: null,
        page: 0,
    };

    const elements = {};

    function init() {
        elements.list = UI.$("#commentList");
        elements.pagination = UI.$("#commentPagination");
        elements.form = UI.$("#commentForm");
        elements.content = UI.$("#commentContent");
        elements.message = UI.$("#commentMessage");
        elements.submitButton = UI.$("#commentSubmitButton") || elements.form?.querySelector("button[type='submit']");
        elements.contentCount = UI.$("#commentContentCount");

        if (!elements.form) {
            return;
        }

        UI.bindCharacterCount(elements.content, elements.contentCount, 6000);
        elements.form.addEventListener("submit", handleSubmit);
    }

    async function load(postId, page = 0) {
        state.postId = postId;
        state.page = page;

        try {
            UI.clear(elements.list);
            elements.list.appendChild(UI.el("p", {
                className: "description",
                text: "댓글을 불러오는 중입니다...",
            }));
            const data = await Api.request(`/posts/${postId}/comments?page=${page}`);
            render(data.content || [], data.page);
        } catch (error) {
            UI.clear(elements.list);
            elements.list.appendChild(UI.el("p", {
                className: "message",
                text: Api.getErrorMessage(error, "댓글을 불러오지 못했습니다."),
            }));
        }
    }

    function render(comments, pageInfo) {
        UI.clear(elements.list);

        if (comments.length === 0) {
            elements.list.appendChild(UI.el("p", {
                className: "description",
                text: "아직 댓글이 없습니다.",
            }));
        } else {
            comments.forEach(comment => {
                elements.list.appendChild(createCommentItem(comment));
            });
        }

        UI.renderPagination(elements.pagination, pageInfo, page => load(state.postId, page));
    }

    function createCommentItem(comment) {
        const currentUser = Auth.getCurrentUser();
        const ownerId = comment.user_id || comment.userId;
        const canEdit = currentUser && ownerId === currentUser.id;

        const content = UI.el("p", {
            className: "comment-content",
            text: comment.content,
        });

        const actions = UI.el("div", { className: "comment-actions" });
        if (canEdit) {
            actions.append(
                UI.el("button", {
                    type: "button",
                    className: "text-button",
                    text: "수정",
                    onclick: () => showEditForm(comment, content, actions),
                }),
                UI.el("button", {
                    type: "button",
                    className: "text-button danger-text",
                    text: "삭제",
                    onclick: () => deleteComment(comment.id),
                })
            );
        }

        return UI.el(
            "article",
            { className: "comment-card" },
            UI.el(
                "div",
                { className: "comment-meta" },
                UI.el("strong", { text: comment.name }),
                UI.el("span", { text: UI.formatDate(comment.createdAt) })
            ),
            content,
            actions
        );
    }

    async function handleSubmit(event) {
        event.preventDefault();
        UI.setMessage(elements.message);

        const content = elements.content.value.trim();
        if (!content) {
            UI.setMessage(elements.message, "댓글을 입력해주세요.");
            return;
        }

        try {
            UI.setBusy(elements.submitButton, true, "등록 중...");
            await Api.request(`/posts/${state.postId}/comments`, {
                method: "POST",
                body: JSON.stringify({ content }),
            });

            elements.form.reset();
            elements.content.dispatchEvent(new Event("input"));
            await load(state.postId, 0);
        } catch (error) {
            UI.setMessage(elements.message, Api.getErrorMessage(error, "댓글 작성에 실패했습니다."));
        } finally {
            UI.setBusy(elements.submitButton, false);
        }
    }

    function showEditForm(comment, contentElement, actionsElement) {
        const originalContent = comment.content;
        const textarea = UI.el("textarea", {
            className: "comment-edit-textarea",
            maxlength: "6000",
        });
        textarea.value = originalContent;

        const counter = UI.el("div", { className: "char-count" });
        UI.bindCharacterCount(textarea, counter, 6000);

        const saveButton = UI.el("button", {
            type: "button",
            className: "text-button",
            text: "저장",
        });

        const cancelButton = UI.el("button", {
            type: "button",
            className: "text-button",
            text: "취소",
        });

        const editActions = UI.el(
            "div",
            { className: "comment-actions" },
            saveButton,
            cancelButton
        );

        contentElement.replaceWith(textarea);
        actionsElement.replaceWith(editActions);
        textarea.focus();

        cancelButton.addEventListener("click", () => {
            textarea.replaceWith(contentElement);
            editActions.replaceWith(actionsElement);
        });

        saveButton.addEventListener("click", async () => {
            await editComment(comment, textarea, contentElement, editActions, actionsElement, saveButton);
        });
    }

    async function editComment(comment, textarea, contentElement, editActions, actionsElement, saveButton) {
        const content = textarea.value.trim();
        if (!content) {
            alert("댓글을 입력해주세요.");
            return;
        }

        try {
            UI.setBusy(saveButton, true, "저장 중...");
            const updated = await Api.request(`/comments/${comment.id}`, {
                method: "PATCH",
                body: JSON.stringify({ content }),
            });

            comment.content = updated.content;
            contentElement.textContent = updated.content;
            textarea.replaceWith(contentElement);
            editActions.replaceWith(actionsElement);
        } catch (error) {
            alert(Api.getErrorMessage(error, "댓글 수정에 실패했습니다."));
        } finally {
            UI.setBusy(saveButton, false);
        }
    }

    async function deleteComment(commentId) {
        if (!confirm("댓글을 삭제할까요?")) {
            return;
        }

        try {
            await Api.request(`/comments/${commentId}`, { method: "DELETE" });
            await load(state.postId, state.page);
        } catch (error) {
            alert(Api.getErrorMessage(error, "댓글 삭제에 실패했습니다."));
        }
    }

    return {
        init,
        load,
    };
})();
