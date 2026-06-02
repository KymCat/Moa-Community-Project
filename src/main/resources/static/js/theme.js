const themeToggleButton = document.getElementById("themeToggle");

function applyTheme(theme) {
    document.body.classList.toggle("dark", theme === "dark");

    if (themeToggleButton) {
        themeToggleButton.textContent = theme === "dark" ? "☀️" : "🌙";
    }
}

const savedTheme = localStorage.getItem("theme") || "light";
applyTheme(savedTheme);

if (themeToggleButton) {
    themeToggleButton.addEventListener("click", () => {
        const currentTheme = document.body.classList.contains("dark") ? "dark" : "light";
        const nextTheme = currentTheme === "dark" ? "light" : "dark";

        localStorage.setItem("theme", nextTheme);
        applyTheme(nextTheme);
    });
}