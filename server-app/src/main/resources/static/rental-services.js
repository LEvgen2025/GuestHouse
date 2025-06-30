// rentalServices.js

// Глобальные переменные
let addExTimePicker, editExTimePicker;

// Форматирование цены (единообразное для всех страниц)
function formatPrice(price) {
    const num = parseFloat(price) || 0;
    return num.toLocaleString('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: num % 1 === 0 ? 0 : 2
    });
}

// Инициализация страницы
function initRentalServicesPage() {
    // Инициализация календарей
    initDateTimePickers();

    // Настройка обработчиков событий
    setupEventListeners();

    // Загрузка начальных данных
    loadRentalServices();

    // Форматирование существующих цен в таблице
    formatExistingPrices();
}

// Форматирование цен уже имеющихся в таблице (при загрузке страницы)
function formatExistingPrices() {
    document.querySelectorAll("tbody td:nth-child(3)").forEach(td => {
        const price = parseFloat(td.textContent);
        if (!isNaN(price)) {
            td.textContent = formatPrice(price);
        }
    });
}

// Инициализация календарей
function initDateTimePickers() {
    addExTimePicker = flatpickr("#addExTime", {
        enableTime: true,
        dateFormat: "Y-m-d H:i",
        time_24hr: true,
        locale: "ru",
        minDate: "today"
    });

    editExTimePicker = flatpickr("#editExTime", {
        enableTime: true,
        dateFormat: "Y-m-d H:i",
        time_24hr: true,
        locale: "ru",
        minDate: "today"
    });
}

// Настройка обработчиков событий
function setupEventListeners() {
    // Обработчик формы добавления
    document.getElementById('addRentalServiceForm').addEventListener('submit', handleAddRentalService);

    // Обработчик формы редактирования
    document.getElementById('editRentalServiceForm').addEventListener('submit', handleEditRentalService);

    // Обработчик клика вне модального окна
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            closeModal(event.target.id);
        }
    });
}

// Функции для работы с модальными окнами
function openModal(modalId) {
    if (modalId === 'addRentalServiceModal') {
        loadRentalServiceFormData();
    }
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Загрузка данных
async function loadRentalServiceFormData() {
    try {
        const [rentalsResponse, servicesResponse] = await Promise.all([
            fetch('/api/rentals/show'),
            fetch('/api/services/show')
        ]);

        const rentals = await rentalsResponse.json();
        const services = await servicesResponse.json();

        fillClientSelect(rentals);
        fillServiceSelect(services);
    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        alert('Не удалось загрузить данные для формы');
    }
}

function fillClientSelect(rentals) {
    const clientSelect = document.getElementById('addClient');
    clientSelect.innerHTML = '<option value="">Выберите клиента</option>';

    const clientsMap = new Map();
    rentals.forEach(rental => {
        if (rental.client && !clientsMap.has(rental.client.id)) {
            clientsMap.set(rental.client.id, {
                id: rental.id, // Используем ID аренды
                name: rental.client.name
            });
        }
    });

    clientsMap.forEach(client => {
        const option = document.createElement('option');
        option.value = client.id;
        option.textContent = client.name;
        clientSelect.appendChild(option);
    });
}

function fillServiceSelect(services) {
    const serviceSelect = document.getElementById('addService');
    serviceSelect.innerHTML = '<option value="">Выберите услугу</option>';

    services.forEach(service => {
        const option = document.createElement('option');
        option.value = service.id;
        option.textContent = `${service.name} (${formatPrice(service.price)})`;
        serviceSelect.appendChild(option);
    });
}

async function loadRentalServices() {
    try {
        const response = await fetch('/api/rentServices/show');
        const rentalServices = await response.json();
        renderRentalServices(rentalServices);
    } catch (error) {
        console.error('Ошибка загрузки заказов:', error);
        alert('Не удалось загрузить список заказов');
    }
}

function renderRentalServices(rentalServices) {
    const tbody = document.querySelector('table tbody');
    tbody.innerHTML = '';

    rentalServices.forEach(service => {
        const row = document.createElement('tr');
        row.setAttribute('data-id', service.id);

        const formattedDate = new Date(service.exTime).toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });

        row.innerHTML = `
            <td>${service.rental.client.name}</td>
            <td>${service.service.name}</td>
            <td>${formatPrice(service.service.price)}</td>
            <td>${formattedDate}</td>
            <td>
                <button class="edit-btn"
                        data-id="${service.id}"
                        data-service-id="${service.service.id}"
                        data-rental-id="${service.rental.id}"
                        data-price="${service.service.price}"
                        data-exTime="${new Date(service.exTime).toISOString().slice(0, 16)}"
                        onclick="openEditRentalServiceModal(this)">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="delete-btn"
                        data-id="${service.id}"
                        onclick="confirmRentalServiceDelete(this)">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;

        tbody.appendChild(row);
    });
}

// Обработчики форм
async function handleAddRentalService(e) {
    e.preventDefault();

    const rentalId = document.getElementById('addClient').value;
    const serviceId = document.getElementById('addService').value;
    const exTime = addExTimePicker.selectedDates[0]?.toISOString();

    if (!rentalId || !serviceId || !exTime) {
        alert('Заполните все поля');
        return;
    }

    try {
        const response = await fetch('/api/rentServices', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
            body: JSON.stringify({
                service: { id: parseInt(serviceId) },
                rental: { id: parseInt(rentalId) },
                exTime: exTime
            })
        });

        if (response.ok) {
            closeModal('addRentalServiceModal');
            document.getElementById('addRentalServiceForm').reset();
            await loadRentalServices();
            alert('Заказ успешно добавлен!');
        } else {
            const errorText = await response.text();
            alert('Ошибка: ' + errorText);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при добавлении заказа');
    }
}

async function handleEditRentalService(e) {
    e.preventDefault();

    const id = document.getElementById('editId').value;
    const selectedDate = editExTimePicker.selectedDates[0];

    if (!selectedDate) {
        alert('Заполните все поля');
        return;
    }

    const year = selectedDate.getFullYear();
    const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(selectedDate.getDate()).padStart(2, '0');
    const hours = String(selectedDate.getHours()).padStart(2, '0');
    const minutes = String(selectedDate.getMinutes()).padStart(2, '0');

    const exTime = `${year}-${month}-${day}T${hours}:${minutes}`;

    try {
        const response = await fetch(`/api/rentServices/${id}?exTime=${encodeURIComponent(exTime)}`, {
            method: 'PUT',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            closeModal('editRentalServiceModal');
            await loadRentalServices();
            alert('Заказ успешно обновлен!');
        } else {
            const errorText = await response.text();
            alert('Ошибка: ' + errorText);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при обновлении заказа');
    }
}

// Функции для работы с UI
function openEditRentalServiceModal(button) {
    const id = button.getAttribute('data-id');
    const price = button.getAttribute('data-price');
    const exTime = button.getAttribute('data-exTime');

    const row = button.closest('tr');
    const clientName = row.querySelector('td:nth-child(1)').textContent;
    const serviceName = row.querySelector('td:nth-child(2)').textContent;

    document.getElementById('editId').value = id;
    document.getElementById('editClientName').textContent = clientName;
    document.getElementById('editServiceName').textContent = serviceName;
    document.getElementById('editServicePrice').textContent = formatPrice(price);

    editExTimePicker.setDate(exTime);
    openModal('editRentalServiceModal');
}

function confirmRentalServiceDelete(button) {
    const rentalServiceId = button.getAttribute('data-id');
    document.getElementById('deleteRentalServiceId').value = rentalServiceId;
    openModal('deleteConfirmationDialog');
}

async function deleteRentalService() {
    const rentalServiceId = document.getElementById('deleteRentalServiceId').value;

    try {
        const response = await fetch(`/api/rentServices/${rentalServiceId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            closeModal('deleteConfirmationDialog');
            await loadRentalServices();
            alert('Заказ успешно удален!');
        } else {
            const errorText = await response.text();
            alert('Ошибка: ' + errorText);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при удалении заказа');
    }
}

// Экспорт функций в глобальную область видимости
window.openModal = openModal;
window.closeModal = closeModal;
window.openEditRentalServiceModal = openEditRentalServiceModal;
window.confirmRentalServiceDelete = confirmRentalServiceDelete;
window.deleteRentalService = deleteRentalService;

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', initRentalServicesPage);