<#include "_includes_head.ftlh">

<script type="text/javascript">

    function resetGroupIndexes() {
        var groupContainer = document.getElementById('form-ride-groups');
        var groups = [...groupContainer.getElementsByClassName("form-ride-group")];
        groups.forEach(function(group, index) {

                [...group.getElementsByTagName("input")].forEach(function(input) {

                    input.setAttribute('name', input.name.replace(/\[[0-9]+\]/, '[' + index  +']'));
                    input.setAttribute('id', input.id.replace(/-[0-9]+/, '-' + index  ));

                    if(input.classList.contains('form-input-geocode')) {
                        initGeoCode(input);
                    }

                });
                [...group.getElementsByTagName("label")].forEach(function(label) {
                    label.setAttribute('for', label.getAttribute('for').replace(/-[0-9]+/, '-' + index  ));
                });
                [...group.getElementsByTagName("button")].forEach(function(button) {
                    button.setAttribute('id', button.id.replace(/-[0-9]+/, '-' + index  ));
                });
        });
    }

    function removeGroup(element) {
        var groupIndex = parseInt(element.id.split('-').pop());
        var groupContainer = document.getElementById('form-ride-groups');
        var groups = [...groupContainer.getElementsByClassName("form-ride-group")];
        if(groups.length === 1) {
            alert('Il est impossible de supprimer le dernier groupe.');
        } else {
            groupContainer.removeChild(groups[groupIndex]);
            resetGroupIndexes();
        }
    }

    function addGroup() {
        var groupContainer = document.getElementById('form-ride-groups');
        var groupCopy = groupContainer.firstElementChild.cloneNode(true);
        groupContainer.appendChild(groupCopy);
        [...groupCopy.getElementsByTagName("input")].forEach(function(input) {
            if(input.hasAttribute('data-empty-on-copy')) {
                input.value='';
            }
        });
        resetGroupIndexes();
    }

</script>

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form id="form-ride"
            enctype="multipart/form-data"
            name="form-ride"
            action="<@common.teamUrl team.id '/admin/templates/' + formdata.id />"
            method="POST"
            class="border rounded p-4">

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-ride-name" class="form-label">Nom</label>
                      <input autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control" id="form-ride-name" placeholder="Sortie hebdomadaire">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-type" class="form-label">Type</label>
                      <select required name="type" class="form-select" id="form-ride-type">
                        <option value="REGULAR"<#if formdata.type == 'REGULAR'> selected</#if>>Sortie régulière</option>
                        <option value="SPECIAL"<#if formdata.type == 'SPECIAL'> selected</#if>>Evénement</option>
                        <option value="RACE"<#if formdata.type == 'RACE'> selected</#if>>Course</option>
                      </select>
                    </div>
                </div>

                <div class="row">
                    <div class="col-12 mb-3">
                      <label for="form-ride-increment" class="form-label">Incrément (augmenté automatiquement à chaque ride associé)</label>
                      <input autocomplete="off" value="${formdata.increment}" name="increment" type="number" min="1" step="1" class="form-control" id="form-ride-increment" placeholder="123">
                    </div>
                </div>

                <div class="mb-3">
                  <label for="form-ride-description" class="form-label">Description</label>
                  <textarea required name="description" class="form-control" id="form-ride-description" rows="5">${formdata.description}</textarea>
                </div>

                <hr/>

                <h5>Groupes <button onclick="addGroup();" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-plus-circle-fill"></i> Ajouter</button></h5>

                <div id="form-ride-groups">
                    <#list formdata.groups as group>
                        <div class="form-ride-group border border-dark rounded p-2 m-2">
                        <input type="hidden" data-empty-on-copy="true" name="groups[${group?index}].id" id="form-ride-group-id-${group?index}" value="${group.id}">
                            <div class="row">
                                <div class="mb-3 col-6">
                                    <label for="form-ride-group-name-${group?index}" class="form-label">Nom</label>
                                    <input type="text" autocomplete="off" required value="${group.name}" name="groups[${group?index}].name" id="form-ride-group-name-${group?index}" placeholder="G1 rapide" class="form-control">
                                </div>
                                <div class="mb-3 col-6">
                                    <label for="form-ride-group-time-${group?index}" class="form-label">Heure de départ</label>
                                    <input type="time" autocomplete="off" required value="${group.meetingTime}" name="groups[${group?index}].meetingTime" id="form-ride-group-time-${group?index}" class="form-control">
                                </div>
                            </div>
                            <div class="row">
                                <div class="mb-3 col-4">
                                    <label for="form-ride-group-lowerspeed-${group?index}" class="form-label">Vitesse basse</label>
                                    <div class="input-group">
                                        <input type="number" autocomplete="off" required value="${group.lowerSpeed}" name="groups[${group?index}].lowerSpeed" id="form-ride-group-lowerspeed-${group?index}" placeholder="30.0" class="form-control">
                                        <span class="input-group-text">km/h</span>
                                    </div>
                                </div>
                                <div class="mb-3 col-4">
                                    <label for="form-ride-group-upperspeed-${group?index}" class="form-label">Vitesse haute</label>
                                    <div class="input-group"><input type="number" autocomplete="off" required value="${group.upperSpeed}" name="groups[${group?index}].upperSpeed" id="form-ride-group-upperspeed-${group?index}" placeholder="30.0" class="form-control">
                                    <span class="input-group-text">km/h</span>
                                </div>
                            </div>
                            <div class="row">
                                <div class="mb-3 col-8">
                                    <label for="form-ride-group-meetinglocation-${group?index}" class="form-label">Lieu de rendez vous</label>
                                    <input type="text" autocomplete="off" required value="${group.meetingLocation}" name="groups[${group?index}].meetingLocation" id="form-ride-group-meetinglocation-${group?index}" placeholder="Place des fêtes" class="form-input-geocode form-control">
                                    <input type="hidden" value="${group.meetingPointLat?c}" name="groups[${group?index}].meetingPointLat" id="form-ride-group-meetinglocation-${group?index}-lat">
                                    <input type="hidden" value="${group.meetingPointLng?c}" name="groups[${group?index}].meetingPointLng" id="form-ride-group-meetinglocation-${group?index}-lng">
                                </div>
                                <div class="mb-3 col-4 align-self-end text-end">
                                    <button id="form-ride-group-delete-${group?index}" onclick="removeGroup(this);" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-trash"></i> Supprimer</button>
                                </div>
                           </div>
                        </div>

                      </div>

                    </#list>

                 </div>

                 <hr/>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">