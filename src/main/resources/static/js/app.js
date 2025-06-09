// Основний файл додатку VoiceMood
const API_BASE = '/api';
let currentUser = null;
let selectedProductId = null;

// Ініціалізація при завантаженні
window.onload = function() {
    // Перевіряємо збережену аутентифікацію
    const savedUser = localStorage.getItem('currentUser');
    const savedToken = localStorage.getItem('authToken');
    
    if (savedUser && savedToken) {
        currentUser = JSON.parse(savedUser);
        showMainApp();
    }
};

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