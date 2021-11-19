<#include "_includes_head.ftlh">

<script type="text/javascript">

    function resetStageIndexes() {
        var groupContainer = document.getElementById('form-trip-stages');
        var groups = [...groupContainer.getElementsByClassName("form-trip-stage")];
        groups.forEach(function(group, index) {

                [...group.getElementsByTagName("input")].forEach(function(input) {
                    input.setAttribute('name', input.name.replace(/\[[0-9]+\]/, '[' + index  +']'));
                    input.setAttribute('id', input.id.replace(/-[0-9]+/, '-' + index  ));

                    if(input.hasAttribute('data-target-id-field')) {
                         initMapAutoComplete(input);
                         input.setAttribute('data-target-id-field', input.getAttribute('data-target-id-field').replace(/-[0-9]+/, '-' + index  ));
                         input.setAttribute('data-target-name-field', input.getAttribute('data-target-name-field').replace(/-[0-9]+/, '-' + index  ));
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
        var groupContainer = document.getElementById('form-trip-stages');
        var groups = [...groupContainer.getElementsByClassName("form-trip-stage")];
        if(groups.length === 1) {
            alert('Il est impossible de supprimer la dernière étape.');
        } else {
            groupContainer.removeChild(groups[groupIndex]);
            resetStageIndexes();
        }
    }

    function addGroup() {
        var groupContainer = document.getElementById('form-trip-stages');
        var groupCopy = groupContainer.firstElementChild.cloneNode(true);
        groupContainer.appendChild(groupCopy);
        [...groupCopy.getElementsByTagName("input")].forEach(function(input) {
            if(input.hasAttribute('data-empty-on-copy')) {
                input.value='';
            }
        });
        resetStageIndexes();
    }

</script>

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form id="form-trip"
            enctype="multipart/form-data"
            name="form-trip"
            action="<@common.teamUrl team.id '/admin/trips/' + formdata.id />"
            method="POST"
            class="border rounded p-4">

                <div class="row mb-3 border rounded bg-light p-2">
                    <#if published>
                        <div class="alert alert-warning p-2" role="alert">
                            <i class="bi bi-calendar-check"></i> Le trip a été publié.
                        </div>
                    </#if>
                    <div class="col-6 mb-3">
                      <label for="form-trip-title" class="form-label">Date de publication</label>
                      <input <#if published>disabled </#if>autocomplete="off" required value="${formdata.publishedAtDate}" name="publishedAtDate" type="date" class="form-control" id="form-trip-publishedat-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-trip-date" class="form-label">Heure de publication</label>
                      <input <#if published>disabled </#if> autocomplete="off" required value="${formdata.publishedAtTime}" name="publishedAtTime" type="time" class="form-control" id="form-trip-publishedat-time">
                    </div>
                </div>

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-trip-title" class="form-label">Titre</label>
                      <input data-element="trips" data-target-id-field="form-trip-permalink" autocomplete="off" required value="${formdata.title}" name="title" type="text" class="form-control form-unique-id" id="form-trip-title" placeholder="Voyage AR">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-trip-type" class="form-label">Type</label>
                      <select required name="type" class="form-select" id="form-trip-type">
                        <option value="ROAD"<#if formdata.type == 'ROAD'> selected</#if>>Route</option>
                        <option value="GRAVEL"<#if formdata.type == 'GRAVEL'> selected</#if>>Gravel</option>
                        <option value="MTB"<#if formdata.type == 'MTB'> selected</#if>>VTT</option>
                      </select>
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-trip-startDate" class="form-label">Date de début</label>
                      <input autocomplete="off" required value="${formdata.startDate}" name="startDate" type="date" class="form-control" id="form-trip-startDate">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-trip-endDate" class="form-label">Date de fin</label>
                      <input autocomplete="off" required value="${formdata.endDate}" name="endDate" type="date" class="form-control" id="form-trip-endDate">
                    </div>
                    <div class="mb-3 col-6">
                            <label for="form-trip-meetinglocation" class="form-label">Lieu de rendez vous</label>
                            <input type="text" autocomplete="off" required value="${formdata.meetingLocation}" name="meetingLocation" id="form-trip-meetinglocation" placeholder="Place des fêtes" class="form-input-geocode form-control">
                            <input type="hidden" value="${formdata.meetingPointLat?c}" name="meetingPointLat" id="form-trip-meetinglocation-lat">
                            <input type="hidden" value="${formdata.meetingPointLng?c}" name="meetingPointLng" id="form-trip-meetinglocation-lng">
                    </div>
                    <div class="mb-3 col-6">
                        <label for="form-trip-time" class="form-label">Heure de départ</label>
                        <input type="time" autocomplete="off" required value="${formdata.meetingTime}" name="meetingTime" id="form-trip-time" class="form-control">
                    </div>
                </div>
                
                <div class="row">
                    <div class="mb-3 col-6">
                        <label for="form-trip-lowerspeed" class="form-label">Vitesse basse</label>
                        <div class="input-group">
                            <input type="number" autocomplete="off" required value="${formdata.lowerSpeed}" name="lowerSpeed" id="form-trip-lowerspeed" placeholder="30.0" class="form-control">
                            <span class="input-group-text">km/h</span>
                        </div>
                    </div>
                    <div class="mb-3 col-6">
                        <label for="form-trip-upperspeed" class="form-label">Vitesse haute</label>
                        <div class="input-group"><input type="number" autocomplete="off" required value="${formdata.upperSpeed}" name="upperSpeed" id="form-trip-upperspeed" placeholder="30.0" class="form-control">
                        <span class="input-group-text">km/h</span>
                    </div>
                </div>

                <div class="mb-3">
                  <label for="form-trip-description" class="form-label">Description</label>
                  <textarea required name="description" class="form-control" id="form-trip-description" rows="5">${formdata.description}</textarea>
                </div>

                <div class="row">

                    <div class="col-6 mb-3">
                      <label for="form-trip-file" class="form-label">Image associée (PNG ou JPG) <#if imaged><i class="bi bi-check-circle-fill"></i></#if></label>
                      <input autocomplete="off" name="file" accept=".png,.jpg,.jpeg" class="form-control form-size-check" type="file" id="form-trip-file">
                    </div>

                     <div class="col-6">
                        <label for="form-trip-permalink" class="form-label">URL unique</label>
                        <input data-element="trips" data-target-id-field="form-trip-permalink" minLength="2" maxLength="19" autocomplete="off" value="${formdata.permalink}" name="permalink" type="text" class="form-control form-unique-id" id="form-trip-permalink">
                    </div>

                </div>

                <hr/>

                <h5>Etapes <button onclick="addGroup();" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-plus-circle-fill"></i> Ajouter</button></h5>

                <div id="form-trip-stages">
                <#list formdata.stages as stage>
                    <div class="form-trip-stage border border-dark rounded p-2 m-2">
                        <input type="hidden" data-empty-on-copy="true" name="stages[${stage?index}].id" id="form-trip-stage-id-${stage?index}" value="${stage.id}">
                        <div class="row">
                            <div class="mb-3 col-6">
                                <label for="form-trip-stage-name-${stage?index}" class="form-label">Nom</label>
                                <input type="text" autocomplete="off" required value="${stage.name}" name="stages[${stage?index}].name" id="form-trip-stage-name-${stage?index}" placeholder="Etape 1" class="form-control">
                            </div>
                            <div class="col-6 mb-3">
                              <label for="form-trip-stage-date-${stage?index}" class="form-label">Date</label>
                              <input autocomplete="off" required value="${stage.date}" name="stages[${stage?index}].date" type="date" class="form-control" id="form-trip-stage-date-${stage?index}">
                            </div>
                        </div>
                        <div class="row">
                            <div class="mb-3 col-6">
                                <label for="form-trip-stage-map-${stage?index}" class="form-label">Map</label>
                                <input id="search-map-field-${stage?index}" type="text" data-target-name-field="form-trip-stage-mapname-${stage?index}" data-team-id="<@common.teamUrlPrefix team.id />" data-target-id-field="form-trip-stage-mapid-${stage?index}" autocomplete="off" value="" class="form-control form-map-control">
                            </div>
                            <div class="mb-3 col-6">
                                <label for="form-trip-stage-map-${stage?index}" class="form-label text-muted">&nbsp;</label>
                                <div class="input-group mb-3">
                                  <input placeholder="Aucune map sélectionnée" type="text" name="stages[${stage?index}].mapName" readonly class="form-control" id="form-trip-stage-mapname-${stage?index}" value="${stage.mapName}">
                                  <button class="btn btn-outline-secondary" type="button" onclick="this.previousElementSibling.value=''; this.nextElementSibling.value='';"><i class="bi bi-x"></i></button>
                                  <input type="hidden" name="stages[${stage?index}].mapId" id="form-trip-stage-mapid-${stage?index}" value="${stage.mapId}">
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="mb-3 col-4 align-self-end text-end">
                                <button id="form-trip-stage-delete-${stage?index}" onclick="removeGroup(this);" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-trash"></i> Supprimer</button>
                            </div>
                       </div>
                    </div>
                </#list>

                </div>

             </div>

                 <hr/>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">