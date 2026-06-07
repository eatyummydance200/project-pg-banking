const logElement = document.getElementById("response-log");

function appendLog(title, payload) {
    const stamp = new Date().toLocaleTimeString("ko-KR", { hour12: false });
    const block = `[${stamp}] ${title}\n${typeof payload === "string" ? payload : JSON.stringify(payload, null, 2)}\n`;
    logElement.textContent = `${block}\n${logElement.textContent}`.trim();
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const text = await response.text();
    let body;

    try {
        body = text ? JSON.parse(text) : {};
    } catch {
        body = text;
    }

    if (!response.ok) {
        throw {
            status: response.status,
            body
        };
    }

    return body;
}

function formDataToObject(form) {
    const data = new FormData(form);
    return Object.fromEntries(data.entries());
}

function numberPayload(data) {
    return Object.fromEntries(
        Object.entries(data).map(([key, value]) => [key, value === "" ? value : Number(value)])
    );
}

function handleSubmit(formId, handler) {
    const form = document.getElementById(formId);
    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const submitButton = form.querySelector("button[type='submit']");
        submitButton.disabled = true;

        try {
            await handler(form);
        } catch (error) {
            appendLog(`ERROR ${formId}`, error.body || error.message || error);
        } finally {
            submitButton.disabled = false;
        }
    });
}

handleSubmit("create-user-form", async (form) => {
    const payload = numberPayload(formDataToObject(form));
    payload.loginId = String(payload.loginId);
    payload.name = String(payload.name);

    const result = await requestJson("/api/users", {
        method: "POST",
        body: JSON.stringify(payload)
    });

    appendLog("CREATE USER", result);
});

handleSubmit("balance-form", async (form) => {
    const payload = numberPayload(formDataToObject(form));
    const result = await requestJson(`/api/accounts/${payload.accountId}/balance`);
    appendLog("GET BALANCE", result);
});

handleSubmit("deposit-form", async (form) => {
    const payload = numberPayload(formDataToObject(form));
    const result = await requestJson(`/api/accounts/${payload.accountId}/deposit`, {
        method: "POST",
        body: JSON.stringify({ amount: payload.amount })
    });

    appendLog("DEPOSIT", result);
});

handleSubmit("withdraw-form", async (form) => {
    const payload = numberPayload(formDataToObject(form));
    const result = await requestJson(`/api/accounts/${payload.accountId}/withdraw`, {
        method: "POST",
        body: JSON.stringify({ amount: payload.amount })
    });

    appendLog("WITHDRAW", result);
});

handleSubmit("transfer-form", async (form) => {
    const payload = numberPayload(formDataToObject(form));
    const result = await requestJson("/api/transfers", {
        method: "POST",
        body: JSON.stringify(payload)
    });

    appendLog("TRANSFER", result);
});

document.getElementById("clear-log-btn").addEventListener("click", () => {
    logElement.textContent = "로그가 비워졌습니다.";
});

document.getElementById("seed-demo-btn").addEventListener("click", () => {
    document.querySelector("#create-user-form [name='loginId']").value = `user${Date.now().toString().slice(-4)}`;
    document.querySelector("#create-user-form [name='name']").value = "테스트 사용자";
    document.querySelector("#create-user-form [name='initialBalance']").value = "100000";

    document.querySelector("#balance-form [name='accountId']").value = "1";
    document.querySelector("#deposit-form [name='accountId']").value = "1";
    document.querySelector("#deposit-form [name='amount']").value = "5000";
    document.querySelector("#withdraw-form [name='accountId']").value = "1";
    document.querySelector("#withdraw-form [name='amount']").value = "1000";
    document.querySelector("#transfer-form [name='fromAccountId']").value = "1";
    document.querySelector("#transfer-form [name='toAccountId']").value = "2";
    document.querySelector("#transfer-form [name='amount']").value = "2500";

    appendLog("SEED", "샘플 입력값을 폼에 채웠습니다.");
});
