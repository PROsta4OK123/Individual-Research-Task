// Функції покупки в модальному вікні

// === ПОКУПКИ В МОДАЛЬНОМУ ВІКНІ ===
function calculatePriceModal() {
    if (!selectedProductId) {
        showModalResult('Товар не обрано', 'error');
        return;
    }

    const customerId = currentUser.customerId || currentUser.id;
    fetch(`${API_BASE}/purchases/calculate-price?customerId=${customerId}&productId=${selectedProductId}`)
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                showModalResult('❌ ' + data.error, 'error');
            } else {
                const message = `
                    <h4>💰 Розрахунок вартості</h4>
                    <p><strong>Початкова ціна:</strong> ${data.originalPrice}₴</p>
                    <p><strong>Ваша знижка:</strong> ${data.appliedDiscount}%</p>
                    <p><strong>Підсумкова ціна:</strong> ${data.finalPrice.toFixed(2)}₴</p>
                    <p><strong>Економія:</strong> ${(data.originalPrice - data.finalPrice).toFixed(2)}₴</p>
                    <p><strong>Можете купити:</strong> ${data.canAfford ? '✅ Так' : '❌ Ні (недостатньо коштів)'}</p>
                `;
                showModalResult(message, 'success');
            }
        })
        .catch(error => showModalResult('❌ Помилка розрахунку: ' + error.message, 'error'));
}

function makePurchaseModal() {
    if (!selectedProductId) {
        showModalResult('Товар не обрано', 'error');
        return;
    }

    fetch(`${API_BASE}/products`)
        .then(response => response.json())
        .then(products => {
            const selectedProduct = products.find(p => p.id == selectedProductId);
            
            if (!selectedProduct) {
                showModalResult('❌ Товар не знайдено', 'error');
                return;
            }
            
            if (selectedProduct.quantity <= 0) {
                showModalResult('❌ Товару немає на складі!', 'error');
                return;
            }
            
            const customerId = currentUser.customerId || currentUser.id;
            return fetch(`${API_BASE}/purchases?customerId=${customerId}&productId=${selectedProductId}`, {
                method: 'POST'
            });
        })
        .then(response => {
            if (!response) return;
            return response.json();
        })
        .then(data => {
            if (!data) return;
            
            if (data.success) {
                const message = `
                    <h4>🎉 Покупка успішна!</h4>
                    <p><strong>Товар:</strong> ${data.productName}</p>
                    <p><strong>Підсумкова ціна:</strong> ${data.finalPrice.toFixed(2)}₴</p>
                    <p><strong>Застосована знижка:</strong> ${data.appliedDiscount}%</p>
                    <p><strong>Залишок коштів:</strong> ${data.remainingMoney.toFixed(2)}₴</p>
                `;
                showModalResult(message, 'success', 8000);
                
                // Оновлюємо баланс користувача
                currentUser.balance = data.remainingMoney;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));
                updateUserInfo();
                
                // Оновлюємо статус товару в модальному вікні
                updateModalProductStock();
                
                // Оновлюємо каталог
                loadProducts();
                if (currentUser.role === 'ADMIN') {
                    loadProductsForAdmin();
                }
            } else {
                showModalResult('❌ ' + data.message, 'error');
            }
        })
        .catch(error => showModalResult('❌ Помилка покупки: ' + error.message, 'error'));
}

function showModalResult(message, type, duration = 6000) {
    const resultElement = document.getElementById('modalPurchaseResult');
    resultElement.innerHTML = `
        ${message}
        <button class="close-btn" onclick="closeModalResult()" title="Закрити">✕</button>
    `;
    resultElement.className = `result ${type}`;
    resultElement.style.display = 'block';
    
    clearTimeout(resultElement.timeoutId);
    resultElement.timeoutId = setTimeout(() => {
        resultElement.style.display = 'none';
    }, duration);
}

function closeModalResult() {
    const element = document.getElementById('modalPurchaseResult');
    if (element) {
        clearTimeout(element.timeoutId);
        element.style.display = 'none';
    }
}

function updateModalProductStock() {
    if (!selectedProductId) return;
    
    fetch(`${API_BASE}/products`)
        .then(response => response.json())
        .then(products => {
            const product = products.find(p => p.id == selectedProductId);
            if (product) {
                const stockElement = document.getElementById('modalProductStock');
                if (product.quantity > 0) {
                    stockElement.className = 'stock-status in-stock';
                    stockElement.innerHTML = `✅ В наявності: ${product.quantity} шт.`;
                } else {
                    stockElement.className = 'stock-status out-of-stock';
                    stockElement.innerHTML = '❌ Немає на складі';
                }
                
                // Оновлюємо характеристики з новою кількістю
                displayProductCharacteristics(product);
            }
        })
        .catch(error => console.error('Помилка оновлення статусу:', error));
} 