<#include "_includes_head.ftlh">

<script type="text/javascript">

    function createLabel(forId, text) {
        var label = document.createElement("label");
        label.setAttribute('for', forId);
        label.classList.add('form-label');
        label.innerHTML = text;
        return label;
    }

    function createTextInput(id, name, type, value, required, placeholder) {
        var input = document.createElement("input");
        input.setAttribute('type', type);
        input.setAttribute('autocomplete', 'off');
        if(required) {
            input.setAttribute('required', 'true');
        }
        input.setAttribute('value', value);
        input.setAttribute('name', name);
        input.setAttribute('id', id);
        if(placeholder) {
            input.setAttribute('placeholder', placeholder);
        }
        input.classList.add('form-control');
        return input;
    }

    function createHiddenInput(id, name, value) {
        var input = document.createElement("input");
        input.setAttribute('type', 'hidden');
        input.setAttribute('value', value);
        input.setAttribute('name', name);
        input.setAttribute('id', id);
        return input;
    }

    function createMapInput(id, name, value, required) {
        var input = document.createElement("input");
        input.setAttribute('type', 'text');
        input.setAttribute('autocomplete', 'off');
        if(required) {
            input.setAttribute('required', 'true');
        }
        input.setAttribute('value', value);
        input.setAttribute('name', name);
        input.setAttribute('id', id);
        input.classList.add('form-control');

        return input;
    }

    function createSpeedInput(id, name, value, required) {
        var divGroup = document.createElement("div");
        divGroup.classList.add('input-group');
        var input = createTextInput(id, name, 'number', value, required, '30.0');
        var legend = document.createElement("span");
        legend.classList.add('input-group-text');
        legend.innerHTML = 'km/h';
        divGroup.appendChild(input);
        divGroup.appendChild(legend);
        return divGroup;
    }

    function createFormGroup() {
        var div = document.createElement("div");
        div.classList.add('mb-3');
        return div;
    }

    function createColFormGroup(size) {
        var div = createFormGroup();
        div.classList.add('col-' + size);
        return div;
    }

    function createRow() {
        var div = document.createElement("div");
        div.classList.add('row');
        return div;
    }

    var geoCodeTimeouts = {};
    var currentGroupIndex = 0;

    function addGroup(values) {

        if(!values) {
             values = {
                id:null,
                name: '',
                meetingTime: '12:00',
                mapId : '',
                upperSpeed: 0,
                lowerSpeed: 0,
                meetingLocation: '',
                meetingPoint: ''
            }
        }

        var target = document.getElementById('form-ride-groups');

        var container = document.createElement('div');
        container.classList.add('border');
        container.classList.add('border-dark');
        container.classList.add('rounded');
        container.classList.add('p-2');
        container.classList.add('m-2');

        // name and time
        var firstRow = createRow();
        var formRow1 = createColFormGroup(6);
        formRow1.appendChild(createHiddenInput('form-ride-group-id-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].id', values.id));
        formRow1.appendChild(createLabel('form-ride-group-name-' + currentGroupIndex, 'Nom'));
        formRow1.appendChild(createTextInput('form-ride-group-name-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].name', 'text', values.name, true, 'G1 rapide'));
        firstRow.appendChild(formRow1);
        var formRow2 = createColFormGroup(6);
        formRow2.appendChild(createLabel('form-ride-group-time-' + currentGroupIndex, 'Heure de départ'));
        formRow2.appendChild(createTextInput('form-ride-group-time-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].meetingTime', 'time', values.meetingTime, true));
        firstRow.appendChild(formRow2);
        container.appendChild(firstRow);

        // map and speed
        var secondRow = createRow();
        var formRow3 = createColFormGroup(4);
        formRow3.appendChild(createLabel('form-ride-group-map-' + currentGroupIndex, 'Map'));
        formRow3.appendChild(createMapInput('form-ride-group-map-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].mapId', values.mapId, false));
        secondRow.appendChild(formRow3);
        var formRow4 = createColFormGroup(4);
        formRow4.appendChild(createLabel('form-ride-group-lowerspeed-' + currentGroupIndex, 'Vitesse basse'));
        formRow4.appendChild(createSpeedInput('form-ride-group-lowerspeed-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].lowerSpeed', values.lowerSpeed, true));
        secondRow.appendChild(formRow4);
        var formRow5 = createColFormGroup(4);
        formRow5.appendChild(createLabel('form-ride-group-upperspeed-' + currentGroupIndex, 'Vitesse haute'));
        formRow5.appendChild(createSpeedInput('form-ride-group-upperspeed-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].upperSpeed', values.upperSpeed, true));
        secondRow.appendChild(formRow5);
        container.appendChild(secondRow);

        // meeting point
        var thirdRow = createRow();
        var formRow6 = createFormGroup();
        formRow6.appendChild(createLabel('form-ride-group-meetinglocation-' + currentGroupIndex, 'Lieu de rendez vous'));
        formRow6.appendChild(createTextInput('form-ride-group-meetinglocation-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].meetingLocation', 'text', values.meetingLocation, true, 'Place des fêtes'));
        formRow6.appendChild(createHiddenInput('form-ride-group-meetingpoint-' + currentGroupIndex, 'groups_tmp['+currentGroupIndex+'].meetingPoint', values.meetingPoint));
        thirdRow.appendChild(formRow6);
        container.appendChild(thirdRow);

        target.appendChild(container);


        document.getElementById('form-ride-group-meetinglocation-' + currentGroupIndex).addEventListener('keyup', function(event) {
            if(geoCodeTimeouts['form-ride-group-meetinglocation-' + currentGroupIndex]) {
                clearTimeout(geoCodeTimeouts['form-ride-group-meetinglocation-' + currentGroupIndex]);
                delete geoCodeTimeouts['form-ride-group-meetinglocation-' + currentGroupIndex];
            }

            if(event.target.value.length > 2) {
                geoCodeTimeouts['form-ride-group-meetinglocation-' + currentGroupIndex] = setTimeout(function() {
                    geoCode(event.target.value, function(label, point) {
                        event.target.value = label;
                        document.getElementById(event.target.id.replace('meetinglocation', 'meetingpoint')).value = JSON.stringify(point);
                    });
                }, 1000);
            }
        });

        AutoComplete({
            EmptyMessage: "Aucune map trouvée",
            Url : "<@spring.url '/api/autocomplete/maps' />"
        }, '#form-ride-group-map-' + currentGroupIndex);

        currentGroupIndex++;
        if(currentGroupIndex > 1) {
            document.getElementById('form-ride-group-remove').disabled = false;
        }

    }

    function removeGroup() {
        var target = document.getElementById('form-ride-groups');
        target.removeChild(target.lastChild);

        currentGroupIndex--;
        if(currentGroupIndex <= 1) {
            document.getElementById('form-ride-group-remove').disabled = true;
        }
    }

    function handleGroups() {
        var formData = new FormData(document.getElementById('form-ride'));
        var groups = []
        var keysToDelete = [];
        for(var pair of formData.entries()) {
            if(pair[0].indexOf('groups_tmp') !== -1) {
                var groupExtractor = /^groups_tmp\[([0-9]+)\]\.([a-zA-Z]+)/g;
                var regexpExtract = groupExtractor.exec(pair[0]);
                var groupIndex = parseInt(regexpExtract[1]);
                var groupKey = regexpExtract[2];
                if(groups.length < (groupIndex + 1)) {
                    groups.push({});
                }
                groups[groupIndex][groupKey] = pair[1];
                keysToDelete.push(pair[0]);
            }
        }
        for(var keyToDelete of keysToDelete) {
            formData.delete(keyToDelete);
        }

        console.log(groups);

        document.getElementById('form-ride-groups-field').value = JSON.stringify(groups);
    }

</script>

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form id="form-ride"
            onsubmit="handleGroups()"
            enctype="multipart/form-data"
            name="form-ride"
            action="<@spring.url '/admin/rides/' + formdata.id />"
            method="POST"
            class="border rounded p-4">

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-ride-title" class="form-label">Titre</label>
                      <input autocomplete="off" required="true" value="${formdata.title}" name="title" type="text" class="form-control" id="form-ride-title" placeholder="Ride du mercredi">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-date" class="form-label">Date</label>
                      <input autocomplete="off" required="true" value="${formdata.date}" name="date" type="date" class="form-control" id="form-ride-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-type" class="form-label">Type</label>
                      <select required="true" name="type" class="form-select" id="form-ride-type">
                        <option value="REGULAR"<#if formdata.type == 'REGULAR'> selected</#if>>Sortie régulière</option>
                        <option value="SPECIAL"<#if formdata.type == 'SPECIAL'> selected</#if>>Evénement exceptionnel</option>
                        <option value="RACE"<#if formdata.type == 'RACE'> selected</#if>>Course</option>
                      </select>
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-file" class="form-label">Image associée (format jpg uniquement)</label>
                      <input autocomplete="off" name="file" accept=".jpg" class="form-control form-size-check" type="file" id="form-ride-file">
                    </div>
                </div>

                <div class="mb-3">
                  <label for="form-ride-description" class="form-label">Description</label>
                  <textarea required="true" name="description" class="form-control" id="form-ride-description" rows="5">${formdata.description}</textarea>
                </div>

                <hr/>

                <h5>Groupes <div class="btn-group"><button type="button" onclick="addGroup()" class="btn btn-outline-secondary btn-sm" role="button"><strong><i class="bi bi-plus"></i></strong></button><button type="button" disabled="true" id="form-ride-group-remove" onclick="removeGroup()" class="ml-2 btn btn-outline-secondary btn-sm" role="button"><strong><i class="bi bi-dash"></i></strong></button></div></h5>
                <div id="form-ride-groups"></div>

                <input type="hidden" name="groups" id="form-ride-groups-field">

                 <hr/>

                <button type="submit" class="btn btn-secondary">Publier</button>
        </form>

    </div>

    <script type="text/javascript">

        JSON.parse('${(formdata.groups)?no_esc}').forEach(function(group) {
            addGroup(group);
        });

    </script>

</div>

<#include "_includes_tail.ftlh">