(function () {
    const savedTheme = localStorage.getItem("theme") || "light";
    document.documentElement.setAttribute("data-theme", savedTheme);

    const themeToggle = document.getElementById("themeToggle");
    if (themeToggle) {
        themeToggle.addEventListener("click", () => {
            const next = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
            document.documentElement.setAttribute("data-theme", next);
            localStorage.setItem("theme", next);
        });
    }

    const editor = document.getElementById("richEditor");
    const contentInput = document.getElementById("contentInput");
    const form = document.querySelector(".editor-shell");
    const status = document.getElementById("autosaveStatus");

    if (editor && contentInput && form) {
        const sync = () => contentInput.value = editor.innerHTML.trim();
        document.querySelectorAll(".toolbar button").forEach(button => {
            button.addEventListener("click", () => {
                document.execCommand(button.dataset.cmd, false, button.dataset.value || null);
                editor.focus();
                sync();
            });
        });
        form.addEventListener("submit", sync);
        editor.addEventListener("input", () => {
            sync();
            scheduleAutoSave();
        });
        form.querySelectorAll("input, select").forEach(input => {
            input.addEventListener("change", scheduleAutoSave);
            input.addEventListener("input", scheduleAutoSave);
        });
    }

    let saveTimer;
    function scheduleAutoSave() {
        if (!form || !form.dataset.noteId) return;
        clearTimeout(saveTimer);
        status.textContent = "Editing...";
        saveTimer = setTimeout(autoSave, 1200);
    }

    async function autoSave() {
        const noteId = form.dataset.noteId;
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        const checkedTags = Array.from(form.querySelectorAll('input[name="tagIds"]:checked')).map(input => Number(input.value));
        const payload = {
            title: form.querySelector('[name="title"]').value,
            content: contentInput.value,
            categoryId: form.querySelector('[name="categoryId"]').value || null,
            tagIds: checkedTags
        };
        status.textContent = "Saving...";
        try {
            const response = await fetch(`/notes/${noteId}/autosave`, {
                method: "POST",
                headers: {"Content-Type": "application/json", [header]: token},
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error("Auto-save failed");
            status.textContent = "Auto-saved";
        } catch (error) {
            status.textContent = "Auto-save paused";
        }
    }

    const aiPanel = document.querySelector(".ai-panel");
    const aiSummaryButton = document.getElementById("aiSummaryButton");
    const aiQuestionsButton = document.getElementById("aiQuestionsButton");
    const aiSummaryOutput = document.getElementById("aiSummaryOutput");

    if (aiPanel && aiSummaryOutput) {
        const runAiAction = async (path, loadingText, button) => {
            const token = document.querySelector('meta[name="_csrf"]').content;
            const header = document.querySelector('meta[name="_csrf_header"]').content;
            const noteId = aiPanel.dataset.noteId;
            button.disabled = true;
            aiSummaryOutput.classList.remove("d-none");
            aiSummaryOutput.textContent = loadingText;
            try {
                const response = await fetch(`/notes/${noteId}/ai/${path}`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json", [header]: token}
                });
                if (response.status === 404 || response.status === 405) {
                    aiSummaryOutput.textContent = "AI backend is not loaded yet. Restart the Spring Boot app and try again.";
                    return;
                }
                if (!response.ok) throw new Error("AI request failed");
                const data = await response.json();
                aiSummaryOutput.textContent = data.summary || data.message;
            } catch (error) {
                aiSummaryOutput.textContent = "AI is unavailable right now. Check GEMINI_API_KEY or OPENAI_API_KEY and restart the app.";
            } finally {
                button.disabled = false;
            }
        };

        if (aiSummaryButton) {
            aiSummaryButton.addEventListener("click", () => {
                runAiAction("summary", "Generating summary...", aiSummaryButton);
            });
        }

        if (aiQuestionsButton) {
            aiQuestionsButton.addEventListener("click", () => {
                runAiAction("study-questions", "Generating study questions...", aiQuestionsButton);
            });
        }
    }
})();
