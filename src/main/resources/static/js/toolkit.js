function removeAllActive() {
    $('.nav-link').each(function () {
        this.classList.remove('active');
    })
}

function setActive(el) {
    el.classList.add('active');
}

function queryExternalService(targetEl, endpoint) {
    fetch(endpoint)
        .then(response => response.json())
        .then(health => {
            targetEl.classList.remove('text-success', 'text-warning', 'text-danger')
            targetEl.classList.add(health ? 'text-success' : 'text-danger');
        })
        .catch(() => {
            targetEl.classList.remove('text-success', 'text-warning', 'text-danger')
            targetEl.classList.add('text-warning');
        });
}

$(document).ready(function () {
    $('.nav-link').each(function () {
        $(this).click(function () {
            removeAllActive();
            setActive(this);
        })
    });
    $('.service-status').each(function () {
        // Query first
        setTimeout(queryExternalService, 0, this, $(this).get(0).dataset.checkendpoint);
        // Periodic query
        setInterval(queryExternalService, 60000, this, $(this).get(0).dataset.checkendpoint);
    })
});

