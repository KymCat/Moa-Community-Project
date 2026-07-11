document.addEventListener("DOMContentLoaded", async () => {
    try {
        await Auth.requireLogin();
        await Auth.loadCurrentUser();

        const postId = UI.getQueryParam("id");
        if (!postId) {
            throw new Error("게시글 ID가 없습니다.");
        }

        bindDetailEvents();

        Comments.init();
        Posts.init();
        Posts.setAfterSave(async () => {
            await loadDetail(postId);
        });

        await loadDetail(postId);
    } catch (error) {
        showDetailError(Api.getErrorMessage(error, "게시글을 불러오지 못했습니다."));
    }
});

function bindDetailEvents() {
    UI.$("#backToListButton")?.addEventListener("click", () => {
        location.href = "/";
    });
}

async function loadDetail(postId) {
    const loading = UI.$("#detailLoading");
    const article = UI.$("#detailArticle");
    const error = UI.$("#detailError");

    loading.hidden = false;
    article.hidden = true;
    UI.setMessage(error);

    try {
        const post = await Posts.loadDetail(postId);
        document.title = `${post.title} - Blog`;
        article.hidden = false;
        loading.hidden = true;
    } catch (err) {
        loading.hidden = true;
        showDetailError(Api.getErrorMessage(err, "게시글을 불러오지 못했습니다."));
    }
}

function showDetailError(message) {
    const loading = UI.$("#detailLoading");
    const article = UI.$("#detailArticle");
    const error = UI.$("#detailError");

    if (loading) {
        loading.hidden = true;
    }

    if (article) {
        article.hidden = true;
    }

    UI.setMessage(error, message);
}
