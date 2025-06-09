// Функції для роботи з товарами та модальним вікном

// === ЗАВАНТАЖЕННЯ ДАНИХ ===
function loadProducts() {
    fetch(`${API_BASE}/products`)
        .then(response => response.json())
        .then(products => {
            const productsGrid = document.getElementById('productsGrid');
            productsGrid.innerHTML = '';
            products.forEach(product => {
                const card = document.createElement('div');
                card.className = 'product-card';
                card.onclick = () => {
                    showSelectedProduct(product);
                };
                card.innerHTML = `
                    <div class="product-image">
                        ${product.imageURL ? `<img src="${product.imageURL}" alt="${product.name}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">` : ''}
                        <div style="display: ${product.imageURL ? 'none' : 'flex'}; align-items: center; justify-content: center; width: 100%; height: 100%; font-size: 48px; color: #6c757d;">📦</div>
                    </div>
                    <div class="product-info">
                        <h3 class="product-name">${product.name}</h3>
                        <p class="product-firm">${product.firm}</p>
                        <p class="product-price">${product.price}₴</p>
                        <p class="product-stock ${product.quantity > 0 ? 'in-stock' : 'out-of-stock'}">${product.quantity > 0 ? 'В наявності: ' + product.quantity + ' шт.' : 'Немає на складі'}</p>
                    </div>
                `;
                productsGrid.appendChild(card);
            });
        })
        .catch(error => showResult('❌ Помилка завантаження товарів: ' + error.message, 'error', 'shopping'));
}

function loadProductsForAdmin() {
    fetch(`${API_BASE}/products`)
        .then(response => response.json())
        .then(products => {
            // Заполняем селект управления товарами
            const adminSelect = document.getElementById('adminProducts');
            if (adminSelect) {
                adminSelect.innerHTML = '<option value="">Оберіть товар...</option>';
                products.forEach(product => {
                    const option = document.createElement('option');
                    option.value = product.id;
                    option.textContent = `${product.name} - ${product.price}₴ [Склад: ${product.quantity}]`;
                    adminSelect.appendChild(option);
                });
            }
            
            // Заполняем селект редактирования товаров
            const editSelect = document.getElementById('editProductSelect');
            if (editSelect) {
                editSelect.innerHTML = '<option value="">Оберіть товар...</option>';
                products.forEach(product => {
                    const option = document.createElement('option');
                    option.value = product.id;
                    option.textContent = `${product.name} (${product.firm}) - ${product.price}₴`;
                    editSelect.appendChild(option);
                });
            }
        })
        .catch(error => showResult('❌ Помилка завантаження товарів: ' + error.message, 'error', 'admin'));
}

// === МОДАЛЬНЕ ВІКНО ТОВАРУ ===
function showSelectedProduct(product) {
    selectedProductId = product.id;
    openProductModal(product);
}

function openProductModal(product) {
    selectedProductId = product.id;
    
    // Заповнюємо загальну інформацію
    document.getElementById('modalProductName').textContent = product.name;
    document.getElementById('modalProductTitle').textContent = product.name;
    document.getElementById('modalProductFirm').textContent = product.firm;
    document.getElementById('modalProductPrice').textContent = product.price + '₴';
    
    // Встановлюємо зображення
    const imageContainer = document.getElementById('modalProductImage');
    if (product.imageURL) {
        imageContainer.innerHTML = `<img src="${product.imageURL}" alt="${product.name}" onerror="this.style.display='none'; this.parentElement.innerHTML='<div style=&quot;font-size: 48px; color: #6c757d;&quot;>📦</div>';">`;
    } else {
        imageContainer.innerHTML = '<div style="font-size: 48px; color: #6c757d;">📦</div>';
    }
    
    // Встановлюємо статус наявності
    const stockElement = document.getElementById('modalProductStock');
    if (product.quantity > 0) {
        stockElement.className = 'stock-status in-stock';
        stockElement.innerHTML = `✅ В наявності: ${product.quantity} шт.`;
    } else {
        stockElement.className = 'stock-status out-of-stock';
        stockElement.innerHTML = '❌ Немає на складі';
    }
    
    // Отримуємо деталі товару з сервера
    fetch(`${API_BASE}/products/${product.id}`)
        .then(response => response.json())
        .then(detailedProduct => {
            displayProductCharacteristics(detailedProduct);
        })
        .catch(error => {
            console.error('Помилка завантаження деталей товару:', error);
            displayBasicCharacteristics(product);
        });
    
    // Очищуємо результат покупки
    document.getElementById('modalPurchaseResult').style.display = 'none';
    
    // Показуємо модальне вікно
    document.getElementById('productModal').style.display = 'block';
}

function closeProductModal() {
    document.getElementById('productModal').style.display = 'none';
    selectedProductId = null;
}

function displayProductCharacteristics(product) {
    const characteristicsList = document.getElementById('characteristicsList');
    let characteristics = [];
    
    // Загальні характеристики
    characteristics.push({ label: 'Назва', value: product.name });
    characteristics.push({ label: 'Виробник', value: product.firm });
    characteristics.push({ label: 'Ціна', value: product.price + '₴' });
    characteristics.push({ label: 'Максимальна знижка', value: product.maxDiscountPercentage + '%' });
    characteristics.push({ label: 'Кількість на складі', value: product.quantity + ' шт.' });
    
    // Специфічні характеристики залежно від типу товару
    if (product.diagonalSize !== undefined) {
        // Ноутбук
        characteristics.push({ label: 'Діагональ екрану', value: product.diagonalSize + '"' });
        characteristics.push({ label: 'Вага', value: product.weight + ' кг' });
        characteristics.push({ label: 'Кількість ядер CPU', value: product.cpuCoreCount });
        characteristics.push({ label: "Об'єм пам'яті", value: product.memoryCount + ' МБ' });
    } else if (product.maxSimValue !== undefined) {
        // Мобільний телефон або смартфон
        characteristics.push({ label: 'Тип контракту', value: product.isContract ? 'З контрактом' : 'Без контракту' });
        characteristics.push({ label: 'Максимум SIM-карт', value: product.maxSimValue });
        
        if (product.os !== undefined) {
            // Смартфон
            characteristics.push({ label: 'Операційна система', value: product.os });
            if (product.installedPrograms && product.installedPrograms.length > 0) {
                characteristics.push({ label: 'Встановлені програми', value: product.installedPrograms.join(', ') });
            }
        }
    }
    
    // Генеруємо HTML для характеристик
    characteristicsList.innerHTML = characteristics.map(char => `
        <div class="characteristic-item">
            <span class="characteristic-label">${char.label}:</span>
            <span class="characteristic-value">${char.value}</span>
        </div>
    `).join('');
}

function displayBasicCharacteristics(product) {
    const characteristicsList = document.getElementById('characteristicsList');
    const characteristics = [
        { label: 'Назва', value: product.name },
        { label: 'Виробник', value: product.firm },
        { label: 'Ціна', value: product.price + '₴' },
        { label: 'Кількість на складі', value: product.quantity + ' шт.' }
    ];
    
    characteristicsList.innerHTML = characteristics.map(char => `
        <div class="characteristic-item">
            <span class="characteristic-label">${char.label}:</span>
            <span class="characteristic-value">${char.value}</span>
        </div>
    `).join('');
}

// Закриття модального вікна при кліку поза ним
window.onclick = function(event) {
    const modal = document.getElementById('productModal');
    if (event.target === modal) {
        closeProductModal();
    }
} 