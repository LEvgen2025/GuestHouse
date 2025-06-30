// Глобальные переменные
let houseIdToDelete = null;

// Вспомогательные функции
function formatPrice(price) {
    return parseFloat(price).toLocaleString('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: price % 1 === 0 ? 0 : 2
    });
}

// Функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function handleOutsideClick(event) {
    if (event.target.className === 'modal') {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });
    }
}

function openEditModal(button) {
    document.getElementById('editHouseId').value = button.getAttribute('data-id');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editPrice').value = button.getAttribute('data-price');
    openModal('editModal');
}

function confirmDelete(button) {
    houseIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}

// Функции для работы с таблицей
function addHouseToTable(house) {
    const tbody = document.querySelector('table tbody');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-id', house.id);

    newRow.innerHTML = `
        <td>${house.name}</td>
        <td>${formatPrice(house.price)}</td>
        <td>
            <button class="edit-btn"
                    data-id="${house.id}"
                    data-name="${house.name}"
                    data-price="${house.price}"
                    onclick="openEditModal(this)">
                <i class="bi bi-pencil"></i>
            </button>
            <button class="delete-btn"
                    data-id="${house.id}"
                    onclick="confirmDelete(this)">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
}

// Обработчики событий
function setupEventListeners() {
    // Обработчик формы добавления
    document.getElementById('addForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const name = document.getElementById('addName').value;
        const price = parseFloat(document.getElementById('addPrice').value);

        if (!name || isNaN(price)) {
            alert('Заполните все поля корректно');
            return;
        }

        try {
            const response = await fetch('/api/houses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify({ name, price })
            });

            if (response.ok) {
                const newHouse = await response.json();
                addHouseToTable(newHouse);
                closeModal('addModal');
                document.getElementById('addForm').reset();
                alert('Дом успешно добавлен!');
            } else {
                alert('Ошибка при добавлении дома: ' + await response.text());
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при добавлении');
        }
    });

    // Обработчик формы редактирования
    document.getElementById('editForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const houseId = document.getElementById('editHouseId').value;
        const name = document.getElementById('editName').value;
        const price = parseFloat(document.getElementById('editPrice').value);

        try {
            const response = await fetch(`/api/houses/${houseId}?name=${encodeURIComponent(name)}&price=${encodeURIComponent(price)}`, {
                method: 'PUT',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            });

            if (response.ok) {
                const row = document.querySelector(`tr[data-id="${houseId}"]`);
                if (row) {
                    row.cells[0].textContent = name;
                    row.cells[1].textContent = formatPrice(price);
                    const editBtn = row.querySelector('.edit-btn');
                    editBtn.setAttribute('data-name', name);
                    editBtn.setAttribute('data-price', price);
                }
                closeModal('editModal');
                alert('Данные успешно обновлены!');
            } else {
                alert('Ошибка в введенных данных');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при отправке данных');
        }
    });

    // Обработчик удаления
    document.getElementById('confirmDeleteBtn').addEventListener('click', async function() {
        if (!houseIdToDelete) return;

        try {
            const response = await fetch(`/api/houses/${houseIdToDelete}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            });

            if (response.ok) {
                document.querySelector(`tr[data-id="${houseIdToDelete}"]`)?.remove();
                alert('Дом успешно удален!');
            } else {
                alert('Ошибка при удалении дома');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при удалении');
        } finally {
            closeModal('deleteModal');
            houseIdToDelete = null;
        }
    });

    // Обработчик клика вне модального окна
    window.addEventListener('click', handleOutsideClick);
}

// Инициализация страницы
function initHousesPage() {
    // Форматирование цен при загрузке
    document.querySelectorAll("tbody td:nth-child(2)").forEach(td => {
        const price = parseFloat(td.textContent);
        if (!isNaN(price)) {
            td.textContent = formatPrice(price);
        }
    });

    setupEventListeners();
}

// Экспорт функций в глобальную область видимости
window.openModal = openModal;
window.closeModal = closeModal;
window.openEditModal = openEditModal;
window.confirmDelete = confirmDelete;

// Запуск после загрузки DOM
document.addEventListener("DOMContentLoaded", initHousesPage);