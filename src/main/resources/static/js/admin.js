// Адміністративні функції

// === АДМІНІСТРАТИВНІ ФУНКЦІЇ ===
function updateQuantity() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const productId = document.getElementById('adminProducts').value;
    const quantity = document.getElementById('newQuantity').value;

    if (!productId || quantity === '') {
        showResult('Оберіть товар та введіть кількість', 'error', 'admin');
        return;
    }

    fetch(`${API_BASE}/admin/products/${productId}/quantity?adminId=${currentUser.id}&quantity=${quantity}`, {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult(`✅ Кількість оновлено: ${data.oldQuantity} → ${data.newQuantity}`, 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
            document.getElementById('newQuantity').value = '';
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка оновлення: ' + error.message, 'error', 'admin'));
}

function deleteProduct() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const productId = document.getElementById('adminProducts').value;

    if (!productId) {
        showResult('Оберіть товар для видалення', 'error', 'admin');
        return;
    }

    if (!confirm('⚠️ Ви впевнені, що хочете видалити цей товар?')) {
        return;
    }

    fetch(`${API_BASE}/admin/products/${productId}?adminId=${currentUser.id}`, {
        method: 'DELETE'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult(`✅ Товар видалено: ${data.deletedProduct.name}`, 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка видалення: ' + error.message, 'error', 'admin'));
}

function showProductForm() {
    const type = document.getElementById('productType').value;
    
    // Ховаємо всі форми
    document.getElementById('laptopForm').classList.add('hidden');
    document.getElementById('phoneForm').classList.add('hidden');
    document.getElementById('smartphoneForm').classList.add('hidden');
    
    // Показуємо потрібну форму
    if (type) {
        document.getElementById(type.replace('-', '') + 'Form').classList.remove('hidden');
    }
}

function createLaptop() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const data = {
        name: document.getElementById('laptopName').value,
        price: parseFloat(document.getElementById('laptopPrice').value),
        firm: document.getElementById('laptopFirm').value,
        maxDiscountPercentage: parseFloat(document.getElementById('laptopDiscount').value),
        diagonalSize: parseInt(document.getElementById('laptopDiagonal').value),
        weight: parseFloat(document.getElementById('laptopWeight').value),
        cpuCoreCount: parseInt(document.getElementById('laptopCores').value),
        memoryCount: parseInt(document.getElementById('laptopMemory').value),
        imageURL: document.getElementById('laptopImageURL').value,
        quantity: parseInt(document.getElementById('laptopQuantity').value) || 0
    };

    fetch(`${API_BASE}/admin/products/laptop?adminId=${currentUser.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult('✅ Ноутбук успішно створено!', 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
            clearForm('laptopForm');
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка створення ноутбука: ' + error.message, 'error', 'admin'));
}

function createMobilePhone() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const data = {
        name: document.getElementById('phoneName').value,
        price: parseFloat(document.getElementById('phonePrice').value),
        firm: document.getElementById('phoneFirm').value,
        maxDiscountPercentage: parseFloat(document.getElementById('phoneDiscount').value),
        isContract: document.getElementById('phoneContract').value === 'true',
        maxSimValue: parseInt(document.getElementById('phoneSimCount').value),
        imageURL: document.getElementById('phoneImageURL').value,
        quantity: parseInt(document.getElementById('phoneQuantity').value) || 0
    };

    fetch(`${API_BASE}/admin/products/mobile-phone?adminId=${currentUser.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult('✅ Мобільний телефон успішно створено!', 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
            clearForm('phoneForm');
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка створення телефону: ' + error.message, 'error', 'admin'));
}

function createSmartphone() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const programsText = document.getElementById('smartphonePrograms').value;
    const programs = programsText ? programsText.split(',').map(p => p.trim()) : [];

    const data = {
        name: document.getElementById('smartphoneName').value,
        price: parseFloat(document.getElementById('smartphonePrice').value),
        firm: document.getElementById('smartphoneFirm').value,
        maxDiscountPercentage: parseFloat(document.getElementById('smartphoneDiscount').value),
        isContract: document.getElementById('smartphoneContract').value === 'true',
        maxSimValue: parseInt(document.getElementById('smartphoneSimCount').value),
        OS: document.getElementById('smartphoneOS').value,
        installedPrograms: programs,
        imageURL: document.getElementById('smartphoneImageURL').value,
        quantity: parseInt(document.getElementById('smartphoneQuantity').value) || 0
    };

    fetch(`${API_BASE}/admin/products/smartphone?adminId=${currentUser.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult('✅ Смартфон успішно створено!', 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
            clearForm('smartphoneForm');
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка створення смартфону: ' + error.message, 'error', 'admin'));
}

function loadProductStats() {
    fetch(`${API_BASE}/admin/products/stats`)
        .then(response => response.json())
        .then(stats => {
            const statsHtml = `
                <div class="success" style="margin-top: 20px;">
                    <h4>📊 Статистика товарів</h4>
                    <div class="grid">
                        <div>
                            <p><strong>📦 Всього товарів:</strong> ${stats.totalProducts}</p>
                            <p><strong>📊 Загальна кількість:</strong> ${stats.totalQuantity}</p>
                        </div>
                        <div>
                            <p><strong>💰 Середня ціна:</strong> ${stats.averagePrice.toFixed(2)}₴</p>
                            <p><strong>⚠️ Немає на складі:</strong> ${stats.outOfStock}</p>
                            <p><strong>📉 Мало на складі:</strong> ${stats.lowStock}</p>
                        </div>
                    </div>
                </div>
            `;
            document.getElementById('statsResult').innerHTML = statsHtml;
        })
        .catch(error => showResult('❌ Помилка завантаження статистики: ' + error.message, 'error', 'admin'));
} 