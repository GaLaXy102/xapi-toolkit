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

function setHighlightOfNav(el, testPath) {
    if (testPath.match($(el).attr('href').replace(new RegExp('\\w*$'), '.*').replace("/", "\\/"))) {
        el.classList.add('active');
        if (el.parentNode.classList.contains('xapi-flow')) el.parentNode.lastElementChild.classList.remove('d-none');
    } else {
        el.classList.remove('active');
        if (el.parentNode.classList.contains('xapi-flow')) el.parentNode.lastElementChild.classList.add('d-none');
    }
}

function highlightActiveSubapp(event) {
    // This doesn't contain any query parameters :)
    const targetPath = event.currentTarget.contentWindow.location.pathname;
    $('a.nav-link').each((_, el) => setHighlightOfNav(el, targetPath));
}

function setHighlightOfStep(el, testPath) {
    if (testPath.match(el.dataset.pathregex.replace("/", "\\/"))) {
        el.classList.remove('text-opacity-25');
    } else {
        el.classList.add('text-opacity-25');
    }
}

function highlightFlowStep(event) {
    // This doesn't contain any query parameters :)
    const targetPath = event.currentTarget.contentWindow.location.pathname;
    $('a.nav-link').parent().children('ol').children().each((_, el) => setHighlightOfStep(el, targetPath));
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
    $('#componentSelectBase').children(':not(#componentSelectType)').each(function () {
        setInvisible(this);
        setNonReqiured(this);
        unsetName(this)
    });
    const targetElementIdQuery = '#componentSelect-' + (element.value === '' ? 'NONE' : element.value);
    $(targetElementIdQuery).each(function () {
        setVisible(this);
        setReqiured(this);
        setName(this, 'component')
    });
}

function triggerQuerySelect(element) {
    $('#queryIdInput').val(element.dataset.qid);
    $('#queryNameInput').val(element.innerText);
    $('.xapi-query-content').each(function () {
        setInvisible(this);
        setNonReqiured(this);
        unsetName(this);
    });
    $('#queryContentInput-' + element.dataset.qid).each(function () {
        setVisible(this);
        setReqiured(this);
        setName(this, 'queryContent');
    });
}

function triggerQueryShow(element) {
    const qIdInpElem = $('#queryIdInput')[0];
    if (qIdInpElem.value === '') {
        // Try lookup by Name
        const qName = $('#queryNameInput')[0].value;
        $('#querySelectBase>ul>li>a').each(function () {
            if (this.innerText === qName) qIdInpElem.value = this.dataset.qid;
        });
    }
    // Not found
    if (qIdInpElem.value === '') return;
    $('.xapi-query-content').each(function () {
        setInvisible(this);
        setNonReqiured(this);
        unsetName(this);
    });
    $('#queryContentInput-' + element.value).each(function () {
        setVisible(this);
        setReqiured(this);
        setName(this, 'queryContent');
    });
}

function triggerGraphSelect(element) {
    $('#graphIdInput').val(element.dataset.gid);
    $('#graphNameInput').val(element.innerText);
    $('.xapi-graph-content').each(function () {
        setInvisible(this);
        setNonReqiured(this);
        unsetName(this);
    });
    $('#graphContentInput-' + element.dataset.gid).each(function () {
        setVisible(this);
        setReqiured(this);
        setName(this, 'graphContent');
    });
}

function triggerGraphShow(element) {
    const gIdInpElem = $('#graphIdInput')[0];
    if (gIdInpElem.value === '') {
        // Try lookup by Name
        const gName = $('#graphNameInput')[0].value;
        $('#graphSelectBase>ul>li>a').each(function () {
            if (this.innerText === gName) gIdInpElem.value = this.dataset.gid;
        });
    }
    // Not found
    if (gIdInpElem.value === '') return;
    $('.xapi-graph-content').each(function () {
        setInvisible(this);
        setNonReqiured(this);
        unsetName(this);
    });
    $('#graphContentInput-' + element.value).each(function () {
        setVisible(this);
        setReqiured(this);
        setName(this, 'graphContent');
    });
}

function adaptDataset(element) {
    var openButton = element.querySelector('#openModal')
    openButton.dataset.bsVName = element.dataset.vname;
    openButton.dataset.bsVId = element.dataset.vid;
}

function adaptModal(element) {
    // Adapted from https://getbootstrap.com/docs/5.0/components/modal/
    element.addEventListener('show.bs.modal', function (event) {
        // Button that triggered the modal
        var button = event.relatedTarget
        // Extract info from data-bs-* attributes
        var valueName = button.getAttribute('data-bs-v-name')
        var valueId = button.getAttribute('data-bs-v-id')
        // Update the modal's content.
        var modalText = element.querySelector('#vName')
        var modalFlowInput = element.querySelector('#flow')

        modalText.textContent = valueName
        modalFlowInput.value = valueId
    })
}

function replaceSvgVis(element) {
    fetch('/api/v1/dave/visualisation' + '?flow=' + element.dataset.did
        + '&activityURL=' + element.dataset.aid
        + '&visId=' + element.dataset.vid)
        .then(response => response.text())
        .then(data => element.innerHTML = data)
        .then(() => element.firstElementChild.removeAttribute('viewport'))
        .then(() => element.firstElementChild.setAttribute('height', 1.2 * element.firstElementChild.getAttribute('height')));
}

// Inspired by https://thecoderain.blogspot.com/2020/11/generate-valid-random-email-js-jquery.html
const emailChars = '1234567890';

function generateEmailAddress(btnEl) {
    const target = $(btnEl).parent().children().first().children().first();
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
    $('.service-status').each(function () {
        // Query first
        setTimeout(queryExternalService, 0, this, $(this).get(0).dataset.checkendpoint);
        // Periodic query
        setInterval(queryExternalService, 60000, this, $(this).get(0).dataset.checkendpoint);
    });
    $("#contentFrame").bind('load', highlightActiveSubapp);
    $("#contentFrame").bind('load', highlightFlowStep);
    // Alert on pending changes
    $('input').change(() => {
        hasChanges = true;
    });
    $('select').change(() => {
        hasChanges = true;
    });
    $('form').submit(() => {
        hasChanges = false;
    });
    window.onbeforeunload = (ev) => {
        if (hasChanges) ev.returnValue = "Changes you made may not be saved.";
    }
    // Disallow start before end
    $('input[type="datetime-local"]#start').change(function () {
        const inputDate = new Date($(this).val());
        inputDate.setSeconds(inputDate.getSeconds() + 1);
        $('input[type="datetime-local"]#end').attr('min', inputDate.toISOString());
    });
    $('input[type="datetime-local"]#end').change(function () {
        const inputDate = new Date($(this).val());
        inputDate.setSeconds(inputDate.getSeconds() - 1);
        $('input[type="datetime-local"]#start').attr('max', inputDate.toISOString());
    });
    // Spinners
    $('.dropdown-spinner').click(function () {
        addSpinner($(this).parent().parent().parent().children().first());
    });
    $('.button-spinner').click(function () {
        if (this.form.checkValidity()) addSpinner($(this));
    });
    $('.replace-spinner').click(function () {
        addSpinner($(this));
        this.removeChild(this.lastElementChild);
    });
    // Toasts
    $('.toast').each(function () {
        (new bootstrap.Toast(this, {})).show();
    });
    // DAVE Analysis Editor
    $('#queryIdInput').each(function () {
        triggerQueryShow(this);
    });

    $('#graphIdInput').each(function () {
        triggerGraphShow(this);
    });
    // Analyses deletion modal
    $('.xapi-vis').each(function () {
        adaptDataset(this)
    });
    $('.xapi-vis-modal').each(function () {
        adaptModal(this)
    });
    // Dashboard deletion modal
    $('.xapi-dashboard').each(function () {
        adaptDataset(this)
    });
    $('.xapi-dashboard-modal').each(function () {
        adaptModal(this)
    });
    // Dashboard presenter
    $('.xapi-dashboard-vis').each(function () {
        replaceSvgVis(this);
        setInterval(() => replaceSvgVis(this), 300000);
    });
    // Tooltips
    $('.xapi-alignment-tooltip').each(function () {
        (new bootstrap.Tooltip(this, {
            title: 'x',
            container: this.parentNode,
            placement: 'left'
        }));
    }).on('input', function () {
            const tt = bootstrap.Tooltip.getInstance(this).tip;
            tt.lastElementChild.innerText = this.value;
        }
    );
    $(window).on('inserted.bs.tooltip', function (ev) {
        const inpEl = ev.target.parentNode.firstElementChild;
        const tt = bootstrap.Tooltip.getInstance(inpEl).tip;
        tt.lastElementChild.innerText = inpEl.value;
    });
});

