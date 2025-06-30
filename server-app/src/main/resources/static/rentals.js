// Глобальные переменные
let bookedDates = {};
let selectedHouseId = null;
let startPicker, endPicker;
let editStartPicker, editEndPicker;
let currentRentalId = null;

// Инициализация страницы
function initRentalsPage() {
    // Получаем данные из Thymeleaf
    bookedDates = window.bookedPeriods || {};

    // Форматирование цен
    document.querySelectorAll("tbody td:nth-child(5)").forEach(td => {
        const price = parseFloat(td.textContent);
        if (!isNaN(price)) {
            td.textContent = formatPrice(price);
        }
    });

    // Инициализация календарей
    initDatePickers();

    // Настройка обработчиков событий
    setupEventListeners();
}

// Форматирование цены
function formatPrice(price) {
    const num = parseFloat(price) || 0;
    return num.toLocaleString('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: num % 1 === 0 ? 0 : 2
    });
}

// Инициализация календарей
function initDatePickers() {
    // Календари для добавления
    startPicker = flatpickr("#addStartDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today",
        disable: [{ from: "1900-01-01", to: "9999-12-31" }],
        onChange: function(selectedDates) {
            if (selectedDates.length > 0) {
                endPicker.set('minDate', selectedDates[0]);
            }
        }
    });

    endPicker = flatpickr("#addEndDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today",
        disable: [{ from: "1900-01-01", to: "9999-12-31" }]
    });

    // Календари для редактирования
    editStartPicker = flatpickr("#editStartDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today"
    });

    editEndPicker = flatpickr("#editEndDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today"
    });
}

// Настройка обработчиков событий
function setupEventListeners() {
    // Обработчик выбора дома
    document.getElementById('addHouse').addEventListener('change', function(e) {
        selectedHouseId = e.target.value;
        updateDisabledDates();
    });

    // Обработчик формы добавления
    document.getElementById('addRentalForm').addEventListener('submit', handleAddRental);

    // Обработчик формы редактирования
    document.getElementById('editRentalForm').addEventListener('submit', handleEditRental);

    // Обработчик клика вне модального окна
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            closeModal(event.target.id);
        }
    });
}

// Функции для работы с модальными окнами
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';

        if (modalId === 'addRentalModal') {
            loadRentalFormData();
            selectedHouseId = null;
            updateDisabledDates();
        }
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';

        if (modalId === 'addRentalModal') {
            document.getElementById('addRentalForm').reset();
        }
    }
}

// Загрузка данных для форм
async function loadRentalFormData() {
    try {
        const [clientsResponse, housesResponse] = await Promise.all([
            fetch('/api/clients/show'),
            fetch('/api/houses/show')
        ]);

        const clients = await clientsResponse.json();
        const houses = await housesResponse.json();

        fillSelect('#addClient', clients, 'Выберите клиента');
        fillSelect('#addHouse', houses, 'Выберите дом');
    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        alert('Не удалось загрузить данные');
    }
}

function fillSelect(selector, items, placeholder) {
    const select = document.querySelector(selector);
    select.innerHTML = `<option value="">${placeholder}</option>`;

    items.forEach(item => {
        const option = document.createElement('option');
        option.value = item.id;
        option.textContent = item.name;
        select.appendChild(option);
    });
}

// Управление заблокированными датами
function updateDisabledDates() {
    if (!selectedHouseId) {
        startPicker.set('disable', [{ from: "1900-01-01", to: "9999-12-31" }]);
        endPicker.set('disable', [{ from: "1900-01-01", to: "9999-12-31" }]);
        return;
    }

    if (!bookedDates[selectedHouseId] || bookedDates[selectedHouseId].length === 0) {
        startPicker.set('disable', []);
        endPicker.set('disable', []);
        return;
    }

    const disabledRanges = bookedDates[selectedHouseId].map(p => ({
        from: p.startDate,
        to: p.endDate
    }));

    startPicker.set('disable', disabledRanges);
    endPicker.set('disable', disabledRanges);
}

function updateEditDisabledDates(houseId, currentStart, currentEnd) {
    if (!bookedDates[houseId]) return;

    const disabledDates = bookedDates[houseId]
        .filter(period => period.startDate !== currentStart || period.endDate !== currentEnd)
        .flatMap(period => {
            const dates = [];
            const start = new Date(period.startDate);
            const end = new Date(period.endDate);

            for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
                dates.push(d.toISOString().split('T')[0]);
            }
            return dates;
        });

    editStartPicker.set('disable', disabledDates);
    editEndPicker.set('disable', disabledDates);

    editStartPicker.set('onChange', function(selectedDates) {
        if (selectedDates.length > 0) {
            editEndPicker.set('minDate', selectedDates[0]);
        }
    });
}

// Форматирование даты
function formatDateLocal(date) {
    if (!date) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Обработчики форм
async function handleAddRental(e) {
    e.preventDefault();

    const clientId = document.getElementById('addClient').value;
    const houseId = document.getElementById('addHouse').value;
    const startDate = formatDateLocal(startPicker.selectedDates[0]);
    const endDate = formatDateLocal(endPicker.selectedDates[0]);

    if (!clientId || !houseId || !startDate || !endDate) {
        alert('Заполните все поля');
        return;
    }

    try {
        const response = await fetch('/api/rentals', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
            body: JSON.stringify({
                client: { id: parseInt(clientId) },
                house: { id: parseInt(houseId) },
                startDate,
                endDate
            })
        });

        if (response.ok) {
            closeModal('addRentalModal');
            await loadAllRentals();
            alert('Аренда успешно добавлена!');
        } else {
            alert('Ошибка: ' + await response.text());
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при добавлении');
    }
}

async function handleEditRental(e) {
    e.preventDefault();

    const rentalId = document.getElementById('editRentalId').value;
    const startDate = formatDateLocal(editStartPicker.selectedDates[0]);
    const endDate = formatDateLocal(editEndPicker.selectedDates[0]);

    if (!startDate || !endDate) {
        alert('Заполните все поля');
        return;
    }

    try {
        const response = await fetch(`/api/rentals/${rentalId}?startDate=${startDate}&endDate=${endDate}`, {
            method: 'PUT',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            closeModal('editRentalModal');
            await loadAllRentals();
            alert('Изменения сохранены!');
        } else {
            alert('Ошибка: ' + await response.text());
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при сохранении');
    }
}

// Работа с таблицей
async function loadAllRentals() {
    try {
        const response = await fetch('/api/rentals/show');
        const rentals = await response.json();
        const tbody = document.querySelector('table tbody');
        tbody.innerHTML = '';

        rentals.forEach(rental => {
            addRentalToTable(rental);
        });
    } catch (error) {
        console.error('Ошибка загрузки аренд:', error);
    }
}

function addRentalToTable(rental) {
    const tbody = document.querySelector('table tbody');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-id', rental.id);

    const startDate = new Date(rental.startDate).toLocaleDateString('ru-RU');
    const endDate = new Date(rental.endDate).toLocaleDateString('ru-RU');

    newRow.innerHTML = `
        <td>${rental.client?.name || ''}</td>
        <td>${rental.house?.name || ''}</td>
        <td>${startDate}</td>
        <td>${endDate}</td>
        <td>${formatPrice(rental.summaryPrice || 0)}</td>
        <td>
            <button class="edit-btn"
                    data-id="${rental.id}"
                    data-client-id="${rental.client?.id || ''}"
                    data-house-id="${rental.house?.id || ''}"
                    data-start-date="${rental.startDate}"
                    data-end-date="${rental.endDate}"
                    data-summary-price="${rental.summaryPrice || ''}"
                    onclick="openEditRentalModal(this)">
                <i class="bi bi-pencil"></i>
            </button>
            <button class="delete-btn"
                    data-id="${rental.id}"
                    onclick="confirmRentalDelete(this)">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
}

// Функции для редактирования
function openEditRentalModal(button) {
    const row = button.closest('tr');
    currentRentalId = button.dataset.id;

    document.getElementById('editRentalId').value = currentRentalId;
    document.getElementById('editClientName').textContent = row.cells[0].textContent;
    document.getElementById('editHouseName').textContent = row.cells[1].textContent;
    document.getElementById('editSummaryPrice').textContent = row.cells[4].textContent;

    editStartPicker.setDate(button.dataset.startDate);
    editEndPicker.setDate(button.dataset.endDate);
    updateEditDisabledDates(button.dataset.houseId, button.dataset.startDate, button.dataset.endDate);

    openModal('editRentalModal');
}

// Функции для удаления
function confirmRentalDelete(button) {
    currentRentalId = button.dataset.id;
    document.getElementById('deleteRentalId').value = currentRentalId;
    openModal('confirmDeleteModal');
}

async function deleteRental() {
    if (!currentRentalId) return;

    try {
        const response = await fetch(`/api/rentals/${currentRentalId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            closeModal('confirmDeleteModal');
            await loadAllRentals();
            alert('Аренда успешно удалена!');
        } else {
            alert('Ошибка: ' + await response.text());
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при удалении');
    } finally {
        currentRentalId = null;
    }
}

// Экспорт функций в глобальную область видимости
window.openModal = openModal;
window.closeModal = closeModal;
window.openEditRentalModal = openEditRentalModal;
window.confirmRentalDelete = confirmRentalDelete;
window.deleteRental = deleteRental;

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', initRentalsPage);