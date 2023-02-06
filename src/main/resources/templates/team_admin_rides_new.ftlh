<#include "_includes_head.ftlh">

<script type="text/javascript">

    <#if formdata.id == 'new'>
        function validateForm(form) {

            var submitDate = document.getElementById('form-ride-publishedat-date').value;
            var submitTime = document.getElementById('form-ride-publishedat-time').value;
            var submitDateTime = new Date(Date.parse(submitDate+'T'+submitTime));

            return confirm('Le ride sera publié le ' + submitDateTime.toLocaleDateString() + ' à ' + submitTime + '. Confirmez vous la création du ride ?');

        }
    </#if>

    function resetGroupIndexes() {
        var groupContainer = document.getElementById('form-ride-groups');
        var groups = [...groupContainer.getElementsByClassName("form-ride-group")];
        groups.forEach(function(group, index) {

                [...group.getElementsByTagName("input")].forEach(function(input) {

                    input.setAttribute('name', input.name.replace(/\[[0-9]+\]/, '[' + index  +']'));
                    input.setAttribute('id', input.id.replace(/-[0-9]+/, '-' + index  ));

                    if(input.hasAttribute('data-target-id-field')) {
                         initMapAutoComplete(input);
                         input.setAttribute('data-target-id-field', input.getAttribute('data-target-id-field').replace(/-[0-9]+/, '-' + index  ));
                         input.setAttribute('data-target-name-field', input.getAttribute('data-target-name-field').replace(/-[0-9]+/, '-' + index  ));
                    }

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
            action="<@common.teamUrl team.id '/admin/rides/' + formdata.id />"
            method="POST"
            <#if formdata.id == 'new'>onsubmit="return validateForm(this);"</#if>
            class="border rounded p-4">

                <input type="hidden" name="templateId" id="form-ride-templateId" value="${formdata.templateId}">

                <div class="row mb-3 border rounded bg-light p-2">
                    <#if published>
                        <div class="alert alert-warning p-2" role="alert">
                            <i class="bi bi-calendar-check"></i> Le ride a été publié.
                        </div>
                    </#if>
                    <div class="col-6 mb-3">
                      <label for="form-ride-title" class="form-label">Date de publication</label>
                      <input <#if published>disabled </#if>autocomplete="off" required value="${formdata.publishedAtDate}" name="publishedAtDate" type="date" class="form-control" id="form-ride-publishedat-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-date" class="form-label">Heure de publication</label>
                      <input <#if published>disabled </#if> autocomplete="off" required value="${formdata.publishedAtTime}" name="publishedAtTime" type="time" class="form-control" id="form-ride-publishedat-time">
                    </div>
                </div>

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-ride-title" class="form-label">Titre</label>
                      <input data-element="rides" data-target-id-field="form-ride-permalink" autocomplete="off" required value="${formdata.title}" name="title" type="text" class="form-control form-unique-id" id="form-ride-title" placeholder="Ride du mercredi">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-date" class="form-label">Date</label>
                      <input autocomplete="off" required value="${formdata.date}" name="date" type="date" class="form-control" id="form-ride-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-type" class="form-label">Type</label>
                      <select required name="type" class="form-select" id="form-ride-type">
                        <option value="REGULAR"<#if formdata.type == 'REGULAR'> selected</#if>>Sortie régulière</option>
                        <option value="SPECIAL"<#if formdata.type == 'SPECIAL'> selected</#if>>Evénement</option>
                        <option value="RACE"<#if formdata.type == 'RACE'> selected</#if>>Course</option>
                      </select>
                    </div>
                    <div class="col-6">
                        <label for="form-ride-permalink" class="form-label">URL unique</label>
                        <input data-element="rides" data-target-id-field="form-ride-permalink" minLength="2" maxLength="19" autocomplete="off" value="${formdata.permalink}" name="permalink" type="text" class="form-control form-unique-id" id="form-ride-permalink">
                    </div>

                </div>

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-ride-startplace" class="form-label">Départ</label>
                      <select required name="startPlaceId" class="form-select" id="form-ride-startplace">
                          <option value="---"<#if formdata.startPlaceId == ''> selected</#if>>---</option>
                          <#list startPlaces?filter(p -> p.startPlace) as place>
                              <option value="${place.id}"<#if formdata.startPlaceId == place.id> selected</#if>>${place.name}</option>
                          </#list>
                        </select>
                    </div>

                    <div class="col-6 mb-3">
                      <label for="form-ride-endplace" class="form-label">Arrivée</label>
                      <select required name="endPlaceId" class="form-select" id="form-ride-endplace">
                        <option value="---"<#if formdata.endPlaceId == ''> selected</#if>>---</option>
                        <#list endPlaces?filter(p -> p.endPlace) as place>
                            <option value="${place.id}"<#if formdata.endPlaceId == place.id> selected</#if>>${place.name}</option>
                        </#list>
                      </select>
                    </div>
                </div>

                <div class="mb-3">
                  <label for="form-ride-file" class="form-label">Image associée (PNG ou JPG) <#if imaged><i class="bi bi-check-circle-fill"></i></#if></label>
                  <input autocomplete="off" name="file" accept=".png,.jpg,.jpeg" class="form-control form-size-check" type="file" id="form-ride-file">
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
                            <div class="mb-3 col-4">
                                <label for="form-ride-group-name-${group?index}" class="form-label">Nom</label>
                                <input type="text" autocomplete="off" required value="${group.name}" name="groups[${group?index}].name" id="form-ride-group-name-${group?index}" placeholder="G1 rapide" class="form-control">
                            </div>
                            <div class="mb-3 col-4">
                                <label for="form-ride-group-time-${group?index}" class="form-label">Heure de départ</label>
                                <input type="time" autocomplete="off" required value="${group.meetingTime}" name="groups[${group?index}].meetingTime" id="form-ride-group-time-${group?index}" class="form-control">
                            </div>
                            <div class="mb-3 col-4">
                                <label for="form-ride-group-averageSpeed-${group?index}" class="form-label">Allure</label>
                                <div class="input-group">
                                    <input type="number" autocomplete="off" required value="${group.averageSpeed}" name="groups[${group?index}].averageSpeed" id="form-ride-group-averageSpeed-${group?index}" placeholder="30.0" class="form-control">
                                    <span class="input-group-text">km/h</span>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="mb-3 col-6">
                                <label for="form-ride-group-map-${group?index}" class="form-label">Map</label>
                                <input id="search-map-field-${group?index}" type="text" data-target-name-field="form-ride-group-mapname-${group?index}" data-team-id="<@common.teamUrlPrefix team.id />" data-target-id-field="form-ride-group-mapid-${group?index}" autocomplete="off" value="" class="form-control form-map-control">
                            </div>
                            <div class="mb-3 col-6">
                                <label for="form-ride-group-map-${group?index}" class="form-label text-muted">&nbsp;</label>
                                <div class="input-group mb-3">
                                  <input placeholder="Aucune map sélectionnée" type="text" name="groups[${group?index}].mapName" readonly class="form-control" id="form-ride-group-mapname-${group?index}" value="${group.mapName}">
                                  <button class="btn btn-outline-secondary" type="button" onclick="this.previousElementSibling.value=''; this.nextElementSibling.value='';"><i class="bi bi-x"></i></button>
                                  <input type="hidden" name="groups[${group?index}].mapId" id="form-ride-group-mapid-${group?index}" value="${group.mapId}">
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="mb-3 col-4 align-self-end text-end">
                                <button id="form-ride-group-delete-${group?index}" onclick="removeGroup(this);" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-trash"></i> Supprimer</button>
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