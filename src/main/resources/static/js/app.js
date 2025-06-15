// Основний файл додатку VoiceMood
const API_BASE = '/api';
let currentUser = null;
let selectedProductId = null;

// Ініціалізація при завантаженні
window.onload = function() {
    checkAuthenticationStatus();
};

// Перевірка стану автентифікації
function checkAuthenticationStatus() {
    const savedUser = localStorage.getItem('currentUser');
    const savedToken = localStorage.getItem('authToken');
    
    if (savedUser && savedToken) {
        // Перевіряємо валідність токена на сервері
        fetch('/api/auth/validate-token', {
            method: 'POST',
            headers: { 
                'Authorization': savedToken,
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                currentUser = JSON.parse(savedUser);
                showMainApp();
            } else {
                // Токен недійсний, очищуємо дані
                console.log('⚠️ Токен недійсний або прострочений');
                showAuthResult('⏰ Ваша сесія прострочена. Будь ласка, увійдіть знову.', 'warning');
                clearUserData();
            }
        })
        .catch(error => {
            console.error('❌ Помилка перевірки токена:', error);
            showAuthResult('🔌 Помилка з\'єднання. Перевірте підключення до інтернету.', 'error');
            clearUserData();
        });
    } else {
        // Показуємо форму авторизації
        showAuthForm();
    }
}

// Функція для показу форми авторизації
function showAuthForm() {
    document.getElementById('authContainer').classList.remove('hidden');
    document.getElementById('mainContainer').classList.add('hidden');
    showLogin();
}

// Функція для очистки даних користувача
function clearUserData() {
    currentUser = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    showAuthForm();
}

// Функція для перевірки часу життя токена
function getTokenExpiryTime(token) {
    if (!token || token.startsWith('guest_')) {
        return null; // Гостьові токени не мають терміну дії
    }
    
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        
        const payload = JSON.parse(jsonPayload);
        return new Date(payload.exp * 1000);
    } catch (error) {
        console.error('Помилка аналізу токена:', error);
        return null;
    }
}

// Функція для автоматичного оновлення токена
async function refreshTokenIfNeeded() {
    const token = localStorage.getItem('authToken');
    if (!token || !currentUser || token.startsWith('guest_')) {
        return false;
    }
    
    const expiryTime = getTokenExpiryTime(token);
    if (!expiryTime) {
        return false;
    }
    
    const currentTime = new Date();
    const timeLeft = expiryTime - currentTime;
    const fifteenMinutes = 15 * 60 * 1000; // 15 хвилин в мілісекундах
    
    // Якщо до закінчення залишається менше 15 хвилин, оновлюємо токен
    if (timeLeft < fifteenMinutes && timeLeft > 0) {
        console.log('🔄 Автоматичне оновлення токена (залишилось:', Math.round(timeLeft / 1000 / 60), 'хвилин)');
        
        try {
            const response = await fetch('/api/auth/refresh-token', {
                method: 'POST',
                headers: { 
                    'Authorization': token,
                    'Content-Type': 'application/json'
                }
            });
            
            const data = await response.json();
            
            if (data.success) {
                // Оновлюємо збережені дані
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('currentUser', JSON.stringify(data.user));
                currentUser = data.user;
                
                console.log('✅ Токен успішно оновлено');
                updateUserInfo(); // Оновлюємо відображення інформації користувача
                
                // Показуємо повідомлення користувачу (опціонально)
                showResult('🔄 Сесію автоматично продовжено', 'success', 'shopping', 3000);
                return true;
            } else {
                console.warn('⚠️ Не вдалося оновити токен:', data.message);
                return false;
            }
        } catch (error) {
            console.error('❌ Помилка оновлення токена:', error);
            return false;
        }
    }
    
    return false;
}

// Періодична перевірка токена (кожні 2 хвилини)
setInterval(async function() {
    if (currentUser && localStorage.getItem('authToken')) {
        // Спочатку намагаємося оновити токен якщо потрібно
        const refreshed = await refreshTokenIfNeeded();
        
        // Якщо не оновили, перевіряємо статус
        if (!refreshed) {
            checkAuthenticationStatus();
        }
    }
}, 2 * 60 * 1000); // 2 хвилини

// Автоматична очистка гостьового акаунту при закритті вкладки
window.addEventListener('beforeunload', function(event) {
    if (currentUser && currentUser.role === 'GUEST') {
        const token = localStorage.getItem('authToken');
        if (token) {
            // Якщо гість витратив гроші, показуємо попередження
            if (currentUser.balance < 2000) {
                event.preventDefault();
                event.returnValue = 'Ви впевнені? Ваш гостьовий акаунт та прогрес будуть видалені!';
            }
            
            // Використовуємо navigator.sendBeacon для надійної відправки при закритті
            const data = JSON.stringify({ token: token });
            navigator.sendBeacon('/api/auth/guest-logout', data);
        }
    }
});

// === НАВІГАЦІЯ ===
function showTab(tabName) {
    // Проверяємо права доступа для админської вкладки
    if (tabName === 'admin' && (!currentUser || currentUser.role !== 'ADMIN')) {
        showResult('❌ У вас немає прав доступа до адміністративної панелі', 'error', 'shopping');
        return;
    }
    
    // Прибираємо активний клас з усіх кнопок
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    
    // Ховаємо всі вкладки
    document.getElementById('shoppingTab').classList.add('hidden');
    document.getElementById('historyTab').classList.add('hidden');
    document.getElementById('adminTab').classList.add('hidden');
    
    // Показуємо потрібну вкладку та активуємо кнопку
    if (tabName === 'shopping') {
        document.getElementById('shoppingTab').classList.remove('hidden');
        document.querySelector('[onclick="showTab(\'shopping\')"]').classList.add('active');
    } else if (tabName === 'history') {
        document.getElementById('historyTab').classList.remove('hidden');
        document.querySelector('[onclick="showTab(\'history\')"]').classList.add('active');
        loadPurchaseHistory(); // Автоматично завантажуємо історію при відкритті вкладки
    } else if (tabName === 'admin') {
        document.getElementById('adminTab').classList.remove('hidden');
        document.querySelector('[onclick="showTab(\'admin\')"]').classList.add('active');
    }
}

function showMainApp() {
    document.getElementById('authContainer').classList.add('hidden');
    document.getElementById('mainContainer').classList.remove('hidden');
    
    // Налаштовуємо інтерфейс залежно від ролі
    let roleIcon, roleText, userInfo;
    
    if (currentUser.role === 'ADMIN') {
        roleIcon = '👨‍💼';
        roleText = 'Адміністратор';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: ${currentUser.discount.toFixed(1)}% | Покупки: ${currentUser.totalPurchases.toFixed(2)}₴`;
    } else if (currentUser.role === 'GUEST') {
        roleIcon = '🎫';
        roleText = 'Гість';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: відсутня`;
    } else {
        roleIcon = '⭐';
        roleText = 'Постійний покупець';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: ${currentUser.discount.toFixed(1)}% | Покупки: ${currentUser.totalPurchases.toFixed(2)}₴`;
    }
    
    document.getElementById('userWelcome').innerHTML = `
        ${roleIcon} ${currentUser.username} (${roleText})<br>
        <small style="font-size: 12px; opacity: 0.9;">${userInfo}</small>
    `;
    
    // Показуємо адмінську вкладку тільки для адміністраторів
    const adminTabBtn = document.getElementById('adminTabBtn');
    if (currentUser.role === 'ADMIN') {
        adminTabBtn.style.display = 'block';
        loadProductsForAdmin();
    } else {
        adminTabBtn.style.display = 'none';
    }
    
    // Завантажуємо дані
    loadProducts();
    
    // Показуємо вкладку покупок за замовчуванням
    showTab('shopping');
}

function updateUserInfo() {
    // Оновлюємо відображення інформації про користувача
    let roleIcon, roleText, userInfo;
    
    if (currentUser.role === 'ADMIN') {
        roleIcon = '👨‍💼';
        roleText = 'Адміністратор';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: ${currentUser.discount.toFixed(1)}% | Покупки: ${currentUser.totalPurchases.toFixed(2)}₴`;
    } else if (currentUser.role === 'GUEST') {
        roleIcon = '🎫';
        roleText = 'Гість';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: відсутня`;
    } else {
        roleIcon = '⭐';
        roleText = 'Постійний покупець';
        userInfo = `Баланс: ${currentUser.balance.toFixed(2)}₴ | Знижка: ${currentUser.discount.toFixed(1)}% | Покупки: ${currentUser.totalPurchases.toFixed(2)}₴`;
    }
    
    document.getElementById('userWelcome').innerHTML = `
        ${roleIcon} ${currentUser.username} (${roleText})<br>
        <small style="font-size: 12px; opacity: 0.9;">${userInfo}</small>
    `;
}

// Функція для виконання API запитів з автоматичним оновленням токена
async function makeAuthenticatedRequest(url, options = {}) {
    // Перевіряємо та оновлюємо токен якщо потрібно
    await refreshTokenIfNeeded();
    
    // Додаємо токен до заголовків
    const token = localStorage.getItem('authToken');
    if (token) {
        options.headers = options.headers || {};
        options.headers['Authorization'] = token;
    }
    
    return fetch(url, options);
}

// === УТИЛІТАРНІ ФУНКЦІЇ ===
function showResult(message, type, tab, duration = 8000) {
    let resultElement;
    if (tab === 'admin') {
        resultElement = document.getElementById('adminResult');
    } else if (tab === 'shopping') {
        resultElement = document.getElementById('shoppingResult');
    } else {
        resultElement = document.getElementById('authResult');
    }
    
    resultElement.innerHTML = `
        ${message}
        <button class="close-btn" onclick="closeResult('${resultElement.id}')" title="Закрити">✕</button>
    `;
    resultElement.className = `result ${type}`;
    resultElement.style.display = 'block';
    
    // Прокручуємо до результату для кращої видимості
    resultElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    
    // Автоматично ховаємо через вказаний час
    clearTimeout(resultElement.timeoutId); // Очищуємо попередній таймер
    resultElement.timeoutId = setTimeout(() => {
        resultElement.style.display = 'none';
    }, duration);
}

function closeResult(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        clearTimeout(element.timeoutId); // Зупиняємо автоматичне приховування
        element.style.display = 'none';
    }
}

function showAuthResult(message, type) {
    showResult(message, type, 'auth', 6000);
}

function clearForm(formId) {
    document.getElementById(formId).querySelectorAll('input, textarea').forEach(input => {
        if (input.type !== 'button') {
            input.value = '';
        }
    });
} 