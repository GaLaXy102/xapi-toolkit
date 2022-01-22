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

function addSpinner(el) {
    if ($(el).children('.spinner-border').length !== 0) return;
    const spinner = document.createElement('span');
    spinner.classList.add('spinner-border', 'spinner-border-sm', 'me-1');
    spinner.ariaHidden = 'true';
    spinner.ariaRoleDescription = 'status';
    $(el).prepend(spinner);
}

function setVisible(el) {
    el.classList.remove('d-none');
}

function setNonReqiured(el) {
    el.removeAttribute('required');
}

function setName(el, name) {
    el.setAttribute('name', name);
}

function unsetName(el) {
    el.removeAttribute('name');
}

function setReqiured(el) {
    el.setAttribute('required', true);
}

function setInvisible(el) {
    el.classList.add('d-none');
}

function triggerComponentSelect(element) {
    $('#componentSelectBase').children(':not(#componentSelectType)').each(function () {setInvisible(this);setNonReqiured(this);unsetName(this)});
    const targetElementIdQuery = '#componentSelect-' + (element.value === '' ? 'NONE' : element.value);
    $(targetElementIdQuery).each(function () {setVisible(this);setReqiured(this);setName(this, 'component')});
}

// Inspired by https://thecoderain.blogspot.com/2020/11/generate-valid-random-email-js-jquery.html
const emailChars = '1234567890';
function generateEmailAddress(btnEl) {
    const target = $(btnEl).parent().children().first();
    var mail = 'mail';
    for (let i = 0; i < 10; i++) {
        mail += emailChars[Math.floor(Math.random() * emailChars.length)];
    }
    mail += '@example.org';
    target.val(mail);
}

let hasChanges = false;

$(document).ready(function () {
    // Sidebar
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
    });
    $("#contentFrame").bind('load', highlightFlowStep);
    // Alert on pending changes
    $('input').change(() => {hasChanges = true;});
    $('form').submit(() => {hasChanges = false;});
    window.onbeforeunload = (ev) => {
        if (hasChanges) ev.returnValue = "Changes you made may not be saved.";
    }
    // Spinners
    $('button').click(function () {
        if (this.classList.contains('dropdown-item')) addSpinner($(this).parent().parent().parent().children().first());
        else if ($(this).parent('a').length === 0 && !this.classList.contains('no-spinner')) addSpinner(this);
    });
    $('.toast').each(function () {
        (new bootstrap.Toast(this, {})).show();
    })
});

