const Likes = (() => {
    async function loadLikeState(postId) {
        return Api.request(`/posts/${postId}/likes`);
    }

    async function likePost(postId) {
        return Api.request(`/posts/${postId}/likes`, {
            method: "POST",
        });
    }

    return {
        loadLikeState,
        likePost,
    };
})();
