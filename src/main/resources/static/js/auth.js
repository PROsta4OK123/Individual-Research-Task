// Функції аутентифікації

// === АУТЕНТИФІКАЦІЯ ===
function showLogin() {
    document.querySelectorAll('.auth-tabs .tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelector('.auth-tabs .tab-btn:first-child').classList.add('active');
    document.getElementById('loginForm').classList.remove('hidden');
    document.getElementById('registerForm').classList.add('hidden');
}

function showRegister() {
    document.querySelectorAll('.auth-tabs .tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelector('.auth-tabs .tab-btn:last-child').classList.add('active');
    document.getElementById('registerForm').classList.remove('hidden');
    document.getElementById('loginForm').classList.add('hidden');
}

function register() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const adminKey = document.getElementById('adminKey').value;

    if (!username || !email || !password) {
        showAuthResult('Заповніть всі обов\'язкові поля', 'error');
        return;
    }

    const endpoint = adminKey ? '/api/auth/register-admin' : '/api/auth/register';
    const data = { username, email, password };
    if (adminKey) data.adminKey = adminKey;

    fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAuthResult('✅ Реєстрація успішна! Тепер увійдіть до системи.', 'success');
            showLogin();
            clearRegisterForm();
        } else {
            showAuthResult('❌ ' + data.message, 'error');
        }
    })
    .catch(error => showAuthResult('❌ Помилка реєстрації: ' + error.message, 'error'));
}

function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        showAuthResult('Введіть ім\'я користувача та пароль', 'error');
        return;
    }

    fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentUser = data.user;
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMainApp();
        } else {
            showAuthResult('❌ ' + data.message, 'error');
        }
    })
    .catch(error => showAuthResult('❌ Помилка входу: ' + error.message, 'error'));
}

function loginAsGuest() {
    fetch('/api/auth/guest-login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentUser = data.user;
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMainApp();
            showAuthResult('🎫 ' + data.message, 'success');
        } else {
            showAuthResult('❌ ' + data.message, 'error');
        }
    })
    .catch(error => showAuthResult('❌ Помилка входу як гість: ' + error.message, 'error'));
}

function logout() {
    // Якщо це гостьовий акаунт, видаляємо його
    if (currentUser && currentUser.role === 'GUEST') {
        const token = localStorage.getItem('authToken');
        if (token) {
            fetch('/api/auth/guest-logout', {
                method: 'DELETE',
                headers: { 
                    'Authorization': token,
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    console.log('✅ Гостьовий акаунт видалено:', data.message);
                    showAuthResult('🗑️ Гостьовий акаунт видалено. До побачення!', 'success');
                } else {
                    console.warn('⚠️ Помилка видалення гостьового акаунту:', data.message);
                    showAuthResult('👋 Ви вийшли з системи', 'success');
                }
            })
            .catch(error => {
                console.error('❌ Помилка при видаленні гостьового акаунту:', error);
                showAuthResult('👋 Ви вийшли з системи', 'success');
            });
        } else {
            showAuthResult('👋 Ви вийшли з системи', 'success');
        }
    } else {
        showAuthResult('👋 Ви вийшли з системи', 'success');
    }

    // Очищуємо дані користувача
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    
    // Ховаємо основну програму та показуємо авторизацію
    document.getElementById('mainContainer').classList.add('hidden');
    document.getElementById('authContainer').classList.remove('hidden');
    
    // Очищуємо форми
    clearLoginForm();
    clearRegisterForm();
    showLogin();
}

function clearLoginForm() {
    document.getElementById('loginUsername').value = '';
    document.getElementById('loginPassword').value = '';
}

function clearRegisterForm() {
    document.getElementById('regUsername').value = '';
    document.getElementById('regEmail').value = '';
    document.getElementById('regPassword').value = '';
    document.getElementById('adminKey').value = '';
} 