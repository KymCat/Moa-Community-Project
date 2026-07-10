document.addEventListener("DOMContentLoaded", async () => {
    try {
        await Auth.requireLogin();

        initUserMenu();
        initLogoutButton();

        const user = await Auth.loadCurrentUser();
        renderUser(user);
        Auth.initAccountSettings(renderUser);

        Posts.init();
        await Posts.load(0);
    } catch (error) {
        console.error(error);
    }
});

function initUserMenu() {
    const userMenuButton = UI.$("#userMenuButton");
    const userCard = UI.$("#userCard");

    userMenuButton?.addEventListener("click", () => {
        userCard.classList.toggle("active");
    });
}

function initLogoutButton() {
    const logoutButton = UI.$("#logoutButton");
    logoutButton?.addEventListener("click", () => {
        Auth.logout();
    });
}

function renderUser(user) {
    UI.$("#userId").textContent = user.id;
    UI.$("#userName").textContent = user.name;
}
