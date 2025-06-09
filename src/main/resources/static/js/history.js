// Функції для роботи з історією покупок

// === ІСТОРІЯ ПОКУПОК ===
function loadPurchaseHistory() {
    const customerId = currentUser.customerId || currentUser.id;
    
    // Завантажуємо статистику
    loadPurchaseStatistics(customerId);
    
    // Завантажуємо історію покупок
    fetch(`${API_BASE}/purchases/history?customerId=${customerId}`)
        .then(response => response.json())
        .then(purchases => {
            displayPurchaseHistory(purchases);
        })
        .catch(error => {
            console.error('Помилка завантаження історії:', error);
            document.getElementById('purchaseHistory').innerHTML = `
                <div class="empty-history">
                    <div class="empty-history-icon">❌</div>
                    <h3>Помилка завантаження</h3>
                    <p>Не вдалося завантажити історію покупок</p>
                </div>
            `;
        });
}

function loadPurchaseStatistics(customerId) {
    fetch(`${API_BASE}/purchases/statistics?customerId=${customerId}`)
        .then(response => response.json())
        .then(stats => {
            displayPurchaseStatistics(stats);
        })
        .catch(error => {
            console.error('Помилка завантаження статистики:', error);
            document.getElementById('purchaseStats').innerHTML = '';
        });
}

function displayPurchaseStatistics(stats) {
    const statsContainer = document.getElementById('purchaseStats');
    statsContainer.innerHTML = `
        <div class="stat-card">
            <div class="stat-number">${stats.purchaseCount}</div>
            <div class="stat-label">Всього покупок</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.totalSpent.toFixed(2)}₴</div>
            <div class="stat-label">Загальна сума</div>
        </div>
        <div class="stat-card">
            <div class="stat-number">${stats.averagePurchase.toFixed(2)}₴</div>
            <div class="stat-label">Середня покупка</div>
        </div>
    `;
}

function displayPurchaseHistory(purchases) {
    const historyContainer = document.getElementById('purchaseHistory');
    
    if (!purchases || purchases.length === 0) {
        historyContainer.innerHTML = `
            <div class="empty-history">
                <div class="empty-history-icon">🛍️</div>
                <h3>Історія покупок порожня</h3>
                <p>Ви ще не здійснили жодної покупки</p>
            </div>
        `;
        return;
    }

    historyContainer.innerHTML = purchases.map(purchase => {
        const purchaseDate = new Date(purchase.purchaseDate);
        const formattedDate = purchaseDate.toLocaleDateString('uk-UA', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });

        return `
            <div class="purchase-item">
                <div class="purchase-image">
                    ${purchase.productImageURL ? 
                        `<img src="${purchase.productImageURL}" alt="${purchase.productName}" onerror="this.style.display='none'; this.parentElement.innerHTML='<div style=&quot;font-size: 24px; color: #6c757d;&quot;>📦</div>';">` 
                        : '<div style="font-size: 24px; color: #6c757d;">📦</div>'
                    }
                </div>
                <div class="purchase-details">
                    <div class="purchase-title">${purchase.productName}</div>
                    <div class="purchase-firm">${purchase.productFirm}</div>
                    <div class="purchase-prices">
                        ${purchase.appliedDiscount > 0 ? 
                            `<span class="original-price">${purchase.originalPrice.toFixed(2)}₴</span>` 
                            : ''
                        }
                        <span class="final-price">${purchase.finalPrice.toFixed(2)}₴</span>
                        ${purchase.appliedDiscount > 0 ? 
                            `<span class="discount-badge">-${purchase.appliedDiscount.toFixed(1)}%</span>` 
                            : ''
                        }
                    </div>
                    <div class="purchase-date">${formattedDate}</div>
                </div>
            </div>
        `;
    }).join('');
} 