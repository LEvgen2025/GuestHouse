document.addEventListener("DOMContentLoaded", function () {
    // Форматирование цены (добавление знака рубля)
    document.querySelectorAll("tbody td:nth-child(2)").forEach(function (td) {
        const price = parseFloat(td.textContent);
        if (!isNaN(price)) {
            td.textContent = formatPrice(price);
        }
    });

    // Инициализация обработчиков событий после загрузки DOM
    initEventListeners();
});

function initEventListeners() {
    // Обработчик формы редактирования
    const editForm = document.getElementById('editForm');
    if (editForm) {
        editForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            const houseId = document.getElementById('editHouseId').value;
            const name = document.getElementById('editName').value;
            const price = parseFloat(document.getElementById('editPrice').value);

            try {
                const response = await fetch(`/api/houses/${houseId}?name=${encodeURIComponent(name)}&price=${encodeURIComponent(price)}`, {
                                    method: 'PUT'
                });

                if (response.ok) {
                    const updatedHouse = await response.json();
                    const row = document.querySelector(`tr[data-id="${houseId}"]`);
                    if (row) {
                        row.cells[0].textContent = updatedHouse.name;
                        row.cells[1].textContent = formatPrice(updatedHouse.price);
                        const editBtn = row.querySelector('.edit-btn');
                        editBtn.setAttribute('data-name', updatedHouse.name);
                        editBtn.setAttribute('data-price', updatedHouse.price);
                    }
                    closeModal('editModal');
                    alert('Данные успешно обновлены!');
                } else {
                    alert('Ошибка при обновлении данных');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Произошла ошибка при отправке данных');
            }
        });
    }

    // Обработчик формы добавления
    const addForm = document.getElementById('addForm');
    if (addForm) {
        addForm.addEventListener('submit', async function (e) {
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
                    },
                    body: JSON.stringify({
                        name: name,
                        price: price
                    })
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
    }

    // Обработчик кнопки удаления
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', async function () {
            if (!houseIdToDelete) return;

            try {
                const response = await fetch(`/api/houses/${houseIdToDelete}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    const row = document.querySelector(`tr[data-id="${houseIdToDelete}"]`);
                    if (row) {
                        row.remove();
                    }
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
    }
}

// Глобальная переменная для хранения ID дома для удаления
let houseIdToDelete = null;

function formatPrice(price) {
    return price.toLocaleString('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: price % 1 === 0 ? 0 : 2
    });
}

// Общие функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Закрытие при клике вне модального окна
window.onclick = function (event) {
    if (event.target.className === 'modal') {
        const modals = document.getElementsByClassName('modal');
        for (let modal of modals) {
            modal.style.display = 'none';
        }
    }
}

// Функция открытия модального окна редактирования
function openEditModal(button) {
    document.getElementById('editHouseId').value = button.getAttribute('data-id');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editPrice').value = button.getAttribute('data-price');
    openModal('editModal');
}

// Функция для добавления дома в таблицу
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

function confirmDelete(button) {
    houseIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}