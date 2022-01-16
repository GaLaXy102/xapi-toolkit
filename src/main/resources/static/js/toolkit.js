function removeAllActive() {
    $('.nav-link').each(function () {
        if (this.parentNode.classList.contains('xapi-flow')) this.parentNode.lastElementChild.classList.add('d-none');
        this.classList.remove('active');
    })
}

function setActive(el) {
    el.parentNode.lastElementChild.classList.remove('d-none');
    el.classList.add('active');
}

function queryExternalService(targetEl, endpoint) {
    fetch(endpoint)
        .then(response => response.ok ? response : new Response(""))
        .then(response => response.json()) // This throws an error when response contains the empty string
        .then(health => {
            targetEl.classList.remove('text-success', 'text-warning', 'text-danger')
            targetEl.classList.add(health ? 'text-success' : 'text-danger');
        })
        .catch(() => {
            targetEl.classList.remove('text-success', 'text-warning', 'text-danger')
            targetEl.classList.add('text-warning');
        });
}

function setHighlightOfStep(el, testPath) {
    if (testPath.match(el.dataset.pathregex.replace("/", "\\/"))) {
        el.classList.remove('text-white-50')
        el.classList.add('text-white')
    } else {
        el.classList.remove('text-white')
        el.classList.add('text-white-50')
    }
}

function highlightFlowStep(event) {
    // This doesn't contain any query parameters :)
    const targetPath = event.currentTarget.contentWindow.location.pathname;
    $('.nav-link').first().parent().children('ol').children().each((_, el) => setHighlightOfStep(el, targetPath));
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
    $("#contentFrame").bind('load', highlightFlowStep);
});

