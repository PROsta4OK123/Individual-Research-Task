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

// === РЕДАГУВАННЯ ТОВАРІВ ===
let editingProduct = null;

function loadProductForEdit() {
    const productId = document.getElementById('editProductSelect').value;
    
    if (!productId) {
        document.getElementById('editProductForm').classList.add('hidden');
        editingProduct = null;
        return;
    }

    fetch(`${API_BASE}/products`)
        .then(response => response.json())
        .then(products => {
            const product = products.find(p => p.id == productId);
            if (product) {
                editingProduct = product;
                populateEditForm(product);
                document.getElementById('editProductForm').classList.remove('hidden');
            }
        })
        .catch(error => showResult('❌ Помилка завантаження товару: ' + error.message, 'error', 'admin'));
}

function populateEditForm(product) {
    const productType = product.productType || getProductType(product);
    
    // Загальні поля
    document.getElementById('editName').value = product.name || '';
    document.getElementById('editPrice').value = product.price || '';
    document.getElementById('editFirm').value = product.firm || '';
    document.getElementById('editDiscount').value = product.maxDiscountPercentage || '';
    document.getElementById('editImageURL').value = product.imageURL || '';
    document.getElementById('editQuantity').value = product.quantity || '';
    
    // Заголовок
    document.getElementById('editProductTitle').textContent = `Редагування: ${product.name} (${productType})`;
    
    // Специфічні поля
    const specificFieldsContainer = document.getElementById('editSpecificFields');
    specificFieldsContainer.innerHTML = '';
    
    if (productType === 'Laptop') {
        specificFieldsContainer.innerHTML = `
            <div class="form-group">
                <label>Характеристики ноутбука:</label>
                <div class="grid">
                    <input type="number" id="editDiagonalSize" placeholder="Діагональ (дюйми)" value="${product.diagonalSize || ''}">
                    <input type="number" id="editWeight" placeholder="Вага (кг)" step="0.1" value="${product.weight || ''}">
                    <input type="number" id="editCpuCoreCount" placeholder="Кількість ядер" value="${product.cpuCoreCount || ''}">
                    <input type="number" id="editMemoryCount" placeholder="Пам'ять (МБ)" value="${product.memoryCount || ''}">
                </div>
            </div>
        `;
    } else if (productType === 'MobilePhone' || productType === 'Smartphone') {
        let phoneFields = `
            <div class="form-group">
                <label>Характеристики телефону:</label>
                <div class="grid">
                    <select id="editIsContract">
                        <option value="false" ${!product.contract ? 'selected' : ''}>Без контракту</option>
                        <option value="true" ${product.contract ? 'selected' : ''}>З контрактом</option>
                    </select>
                    <input type="number" id="editMaxSimValue" placeholder="Макс. SIM-карт" min="1" value="${product.maxSimValue || ''}">
                </div>
            </div>
        `;
        
        if (productType === 'Smartphone') {
            phoneFields += `
                <div class="form-group">
                    <label>Додаткові характеристики смартфону:</label>
                    <input type="text" id="editOS" placeholder="Операційна система" value="${product.os || ''}">
                    <textarea id="editInstalledPrograms" placeholder="Встановлені програми (через кому)" rows="2">${product.installedPrograms ? product.installedPrograms.join(', ') : ''}</textarea>
                </div>
            `;
        }
        
        specificFieldsContainer.innerHTML = phoneFields;
    }
}

function getProductType(product) {
    // Визначаємо тип товару на основі наявних полів
    if (product.hasOwnProperty('diagonalSize') || product.hasOwnProperty('cpuCoreCount')) {
        return 'Laptop';
    } else if (product.hasOwnProperty('os') || product.hasOwnProperty('installedPrograms')) {
        return 'Smartphone';
    } else if (product.hasOwnProperty('maxSimValue') || product.hasOwnProperty('contract')) {
        return 'MobilePhone';
    }
    return 'Product';
}

function saveProductChanges() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    if (!editingProduct) {
        showResult('❌ Товар не обрано для редагування', 'error', 'admin');
        return;
    }

    const updates = {
        name: document.getElementById('editName').value,
        price: parseFloat(document.getElementById('editPrice').value),
        firm: document.getElementById('editFirm').value,
        maxDiscountPercentage: parseFloat(document.getElementById('editDiscount').value),
        imageURL: document.getElementById('editImageURL').value,
        quantity: parseInt(document.getElementById('editQuantity').value)
    };

    // Додаємо специфічні поля в залежності від типу товару
    const productType = getProductType(editingProduct);
    
    if (productType === 'Laptop') {
        updates.diagonalSize = parseInt(document.getElementById('editDiagonalSize').value);
        updates.weight = parseFloat(document.getElementById('editWeight').value);
        updates.cpuCoreCount = parseInt(document.getElementById('editCpuCoreCount').value);
        updates.memoryCount = parseInt(document.getElementById('editMemoryCount').value);
    } else if (productType === 'MobilePhone' || productType === 'Smartphone') {
        updates.isContract = document.getElementById('editIsContract').value === 'true';
        updates.maxSimValue = parseInt(document.getElementById('editMaxSimValue').value);
        
        if (productType === 'Smartphone') {
            updates.OS = document.getElementById('editOS').value;
            const programsText = document.getElementById('editInstalledPrograms').value;
            updates.installedPrograms = programsText || '';
        }
    }

    fetch(`${API_BASE}/admin/products/${editingProduct.id}?adminId=${currentUser.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult(`✅ Товар "${editingProduct.name}" успішно оновлено!`, 'success', 'admin');
            loadProducts();
            loadProductsForAdmin();
            cancelProductEdit();
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка збереження: ' + error.message, 'error', 'admin'));
}

function cancelProductEdit() {
    document.getElementById('editProductForm').classList.add('hidden');
    document.getElementById('editProductSelect').value = '';
    editingProduct = null;
}

function clearForm(formId) {
    const form = document.getElementById(formId);
    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => input.value = '');
}

// === УПРАВЛІННЯ КОРИСТУВАЧАМИ ===

function loadAllUsers() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    fetch(`${API_BASE}/admin/users?adminId=${currentUser.id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                populateUserSelect(data.users);
                showResult(`✅ Завантажено ${data.totalUsers} користувачів`, 'success', 'admin');
            } else {
                showResult('❌ ' + data.message, 'error', 'admin');
            }
        })
        .catch(error => showResult('❌ Помилка завантаження користувачів: ' + error.message, 'error', 'admin'));
}

function populateUserSelect(users) {
    const select = document.getElementById('userSelect');
    select.innerHTML = '<option value="">Оберіть користувача...</option>';
    
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = `${user.username} (${user.email}) - ${user.role}`;
        if (user.balance !== undefined) {
            option.textContent += ` - Баланс: ${user.balance.toFixed(2)}₴`;
        }
        select.appendChild(option);
    });
}

function loadUserForEdit() {
    const userId = document.getElementById('userSelect').value;
    if (!userId) {
        document.getElementById('editUserForm').classList.add('hidden');
        return;
    }

    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    fetch(`${API_BASE}/admin/users/${userId}?adminId=${currentUser.id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                populateUserEditForm(data.user);
                document.getElementById('editUserForm').classList.remove('hidden');
            } else {
                showResult('❌ ' + data.message, 'error', 'admin');
            }
        })
        .catch(error => showResult('❌ Помилка завантаження користувача: ' + error.message, 'error', 'admin'));
}

function populateUserEditForm(user) {
    document.getElementById('editUserTitle').textContent = `Редагування: ${user.username}`;
    document.getElementById('editUserUsername').value = user.username;
    document.getElementById('editUserEmail').value = user.email;
    document.getElementById('editUserRole').value = user.role;
    document.getElementById('editUserActive').checked = user.isActive;
    
    // Заповнюємо фінансові дані
    document.getElementById('editUserBalance').value = user.balance !== undefined ? user.balance.toFixed(2) : '0.00';
    document.getElementById('editUserTotalPurchases').value = user.totalPurchases !== undefined ? user.totalPurchases.toFixed(2) : '0.00';
    document.getElementById('editUserFullName').value = user.fullName || '';
}

function saveUserChanges() {
    const userId = document.getElementById('userSelect').value;
    if (!userId) {
        showResult('❌ Користувач не обраний', 'error', 'admin');
        return;
    }

    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    const updates = {
        role: document.getElementById('editUserRole').value,
        isActive: document.getElementById('editUserActive').checked,
        balance: parseFloat(document.getElementById('editUserBalance').value),
        totalPurchases: parseFloat(document.getElementById('editUserTotalPurchases').value),
        fullName: document.getElementById('editUserFullName').value
    };

    fetch(`${API_BASE}/admin/users/${userId}?adminId=${currentUser.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult('✅ Дані користувача успішно оновлено!', 'success', 'admin');
            loadAllUsers(); // Оновити список користувачів
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка оновлення користувача: ' + error.message, 'error', 'admin'));
}

function cancelUserEdit() {
    document.getElementById('editUserForm').classList.add('hidden');
    document.getElementById('userSelect').value = '';
}

function resetUserPassword() {
    const userId = document.getElementById('userSelect').value;
    if (!userId) {
        showResult('❌ Користувач не обраний', 'error', 'admin');
        return;
    }

    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    if (!confirm('⚠️ Ви впевнені, що хочете скинути пароль користувача?\nНовий пароль буде: password123')) {
        return;
    }

    fetch(`${API_BASE}/admin/users/${userId}/reset-password?adminId=${currentUser.id}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showResult(`✅ Пароль скинуто! Новий пароль: ${data.newPassword}`, 'success', 'admin', 10000);
        } else {
            showResult('❌ ' + data.message, 'error', 'admin');
        }
    })
    .catch(error => showResult('❌ Помилка скидання пароля: ' + error.message, 'error', 'admin'));
}

function loadUserStats() {
    if (!currentUser || currentUser.role !== 'ADMIN') {
        showResult('❌ Немає прав адміністратора', 'error', 'admin');
        return;
    }

    fetch(`${API_BASE}/admin/users/stats?adminId=${currentUser.id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayUserStats(data.stats);
            } else {
                showResult('❌ ' + data.message, 'error', 'admin');
            }
        })
        .catch(error => showResult('❌ Помилка завантаження статистики: ' + error.message, 'error', 'admin'));
}

function displayUserStats(stats) {
    const statsHtml = `
        <div class="stats-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-top: 20px;">
            <div class="stat-card" style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #667eea; margin: 0 0 10px 0;">👥 Загальна кількість</h4>
                <div style="font-size: 24px; font-weight: bold; color: #333;">${stats.totalUsers}</div>
                <div style="color: #6c757d;">користувачів</div>
            </div>
            <div class="stat-card" style="background: #d4edda; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #28a745; margin: 0 0 10px 0;">✅ Активні</h4>
                <div style="font-size: 24px; font-weight: bold; color: #155724;">${stats.activeUsers}</div>
                <div style="color: #155724;">активних користувачів</div>
            </div>
            <div class="stat-card" style="background: #fff3cd; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #856404; margin: 0 0 10px 0;">👨‍💼 Адміністратори</h4>
                <div style="font-size: 24px; font-weight: bold; color: #856404;">${stats.adminUsers}</div>
                <div style="color: #856404;">адмінів</div>
            </div>
            <div class="stat-card" style="background: #cce7ff; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #004085; margin: 0 0 10px 0;">👤 Користувачі</h4>
                <div style="font-size: 24px; font-weight: bold; color: #004085;">${stats.regularUsers}</div>
                <div style="color: #004085;">звичайних користувачів</div>
            </div>
            ${stats.totalBalance !== undefined ? `
            <div class="stat-card" style="background: #d1ecf1; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #0c5460; margin: 0 0 10px 0;">💰 Загальний баланс</h4>
                <div style="font-size: 24px; font-weight: bold; color: #0c5460;">${stats.totalBalance.toFixed(2)}₴</div>
                <div style="color: #0c5460;">всіх користувачів</div>
            </div>
            <div class="stat-card" style="background: #f8d7da; padding: 20px; border-radius: 10px; text-align: center;">
                <h4 style="color: #721c24; margin: 0 0 10px 0;">🛒 Загальні покупки</h4>
                <div style="font-size: 24px; font-weight: bold; color: #721c24;">${stats.totalPurchases.toFixed(2)}₴</div>
                <div style="color: #721c24;">всіх покупок</div>
            </div>
            ` : ''}
        </div>
    `;
    
    document.getElementById('userStatsResult').innerHTML = statsHtml;
} 