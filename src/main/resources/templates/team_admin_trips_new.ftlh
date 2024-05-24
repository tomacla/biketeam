<#include "_includes_head.ftlh">

<script type="text/javascript">

    <#if  formdata.id == 'new'>
        function validateForm(form) {

            var submitDate = document.getElementById('form-trip-publishedat-date').value;
            var submitTime = document.getElementById('form-trip-publishedat-time').value;
            var submitDateTime = new Date(Date.parse(submitDate+'T'+submitTime));

            return confirm('Le trip sera publié le ' + submitDateTime.toLocaleDateString() + ' à ' + submitTime + '. Confirmez vous la création du trip ?');

        }
    </#if>

    function resetStageIndexes() {
        var groupContainer = document.getElementById('form-trip-stages');
        var groups = [...groupContainer.getElementsByClassName("form-trip-stage")];
        groups.forEach(function(group, index) {

                [...group.getElementsByTagName("span")].forEach(function(span) {
                        if(span.hasAttribute('data-target-id-field')) {

                             span.onclick = function() {
                                var targetIdField = document.getElementById(span.getAttribute("data-target-id-field"));
                                var targetNameField = document.getElementById(span.getAttribute("data-target-name-field"));
                                var sourceIdField = span.getAttribute("data-source-id");
                                var sourceNameField = span.getAttribute("data-source-name");
                                targetIdField.value = sourceIdField;
                                targetNameField.value = sourceNameField;
                             };

                             span.setAttribute('data-target-id-field', span.getAttribute('data-target-id-field').replace(/-[0-9]+/, '-' + index  ));
                             span.setAttribute('data-target-name-field', span.getAttribute('data-target-name-field').replace(/-[0-9]+/, '-' + index  ));
                        }
                    });

                [...group.getElementsByTagName("input")].forEach(function(input) {
                    input.setAttribute('name', input.name.replace(/\[[0-9]+\]/, '[' + index  +']'));
                    input.setAttribute('id', input.id.replace(/-[0-9]+/, '-' + index  ));

                    if(input.hasAttribute('data-target-id-field')) {
                         if(!input.hasAttribute('data-bs-toggle')) {
                             initMapAutoComplete(input);
                          }
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
            if(input.hasAttribute('data-bs-toggle')) {
               input.removeAttribute('data-bs-toggle');
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
            <#if  formdata.id == 'new'>onsubmit="return validateForm(this);"</#if>
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

                    <div class="col-12 mb-3">
                        <div class="border rounded p-2">
                            <div class="form-check form-switch">
                             <input class="form-check-input" name="listedInFeed" type="checkbox" id="form-trip-listedInFeed"<#if formdata.listedInFeed?? && formdata.listedInFeed == 'on'> checked</#if>>
                             <label class="form-check-label" for="form-trip-listedInFeed">Trip visible dans le flux des abonnés</label>
                           </div>
                           <div id="form-trip-listedInFeed-help" class="form-text">Si l'option est désactivée, le trip est toujours visible aux utilisateurs ayant accès au groupe et disposant de l'URL.</div>
                       </div>
                    </div>

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
                        <label for="form-trip-time" class="form-label">Heure de départ</label>
                        <input type="time" autocomplete="off" required value="${formdata.meetingTime}" name="meetingTime" id="form-trip-time" class="form-control">
                    </div>
                </div>

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-trip-startplace" class="form-label">Départ</label>
                      <select required name="startPlaceId" class="form-select" id="form-trip-startplace">
                          <option value="---"<#if formdata.startPlaceId == ''> selected</#if>>---</option>
                          <#list startPlaces?filter(p -> p.startPlace) as place>
                              <option value="${place.id}"<#if formdata.startPlaceId == place.id> selected</#if>>${place.name}</option>
                          </#list>
                        </select>
                    </div>

                    <div class="col-6 mb-3">
                      <label for="form-trip-endplace" class="form-label">Arrivée</label>
                      <select required name="endPlaceId" class="form-select" id="form-trip-endplace">
                        <option value="---"<#if formdata.endPlaceId == ''> selected</#if>>---</option>
                        <#list endPlaces?filter(p -> p.endPlace) as place>
                            <option value="${place.id}"<#if formdata.endPlaceId == place.id> selected</#if>>${place.name}</option>
                        </#list>
                      </select>
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
                        <input data-element="trips" data-target-id-field="form-trip-permalink" minLength="2" maxLength="40" autocomplete="off" value="${formdata.permalink}" name="permalink" type="text" class="form-control form-unique-id" id="form-trip-permalink">
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
                        <div class="col-12 mb-3">
                            <div class="p-2">
                                <div class="form-check form-switch">
                                 <input class="form-check-input" name="stages[${stage?index}].alternative" type="checkbox" id="form-trip-stage-mapalt-${stage?index}"<#if stage.alternative?? && stage.alternative == 'on'> checked</#if>>
                                 <label class="form-check-label" for="form-trip-stage-mapalt-${stage?index}">Cette étape est une alternative au tracé principal</label>
                               </div>
                           </div>
                        </div>
                        <div class="row border rounded mx-2">
                            <div class="my-2 col-12 align-items-center d-flex flex-row justify-content-between">
                                <h6 class="m-0">Map associée</h6>
                                <div class="input-group m-0 w-50">
                                  <input placeholder="Aucune map sélectionnée" type="text" name="stages[${stage?index}].mapName" readonly class="form-control form-control-sm" id="form-trip-stage-mapname-${stage?index}" value="${stage.mapName}">
                                  <input type="hidden" name="stages[${stage?index}].mapId" id="form-trip-stage-mapid-${stage?index}" value="${stage.mapId}">
                                  <button class="btn btn-outline-danger btn-sm" type="button" onclick="this.previousElementSibling.previousElementSibling.value=''; this.previousElementSibling.value='';"><i class="bi bi-x"></i></button>
                                </div>
                            </div>
                            <div class="mb-2 col-12 align-items-center d-flex flex-row justify-content-start">
                                <span>Rechercher</span>
                                <div class="ms-2"><input placeholder="par nom" id="search-map-field-${stage?index}" type="text" data-target-name-field="form-trip-stage-mapname-${stage?index}" data-team-id="<@common.teamUrlPrefix team.id />" data-target-id-field="form-trip-stage-mapid-${stage?index}" autocomplete="off" value="" class="form-control form-control-sm form-map-control"></div>
                            </div>
                            <div class="mb-2 col-12 align-items-center d-flex flex-row justify-content-start">
                                <span text-nowrap">Dernières</span>
                                <div class="ms-2">
                                    <#if lastMaps?? && lastMaps?size gt 0>
                                        <#list lastMaps as lastMap>
                                            <span data-source-id="${lastMap.id}" data-source-name="${lastMap.name}" data-target-name-field="form-trip-stage-mapname-${stage?index}" data-target-id-field="form-trip-stage-mapid-${stage?index}" role="button" class="badge bg-light text-dark border">${lastMap.name}</span>
                                        </#list>
                                    </#if>
                                </div>
                            </div>

                        </div>
                        <div class="row mt-2">
                            <div class="mb-2 col-4 align-self-end text-end">
                                <button id="form-trip-stage-delete-${stage?index}" onclick="removeGroup(this);" type="button" class="btn btn-sm btn-secondary"><i class="bi bi-trash"></i> Supprimer</button>
                            </div>
                       </div>
                    </div>
                </#list>

             </div>

            <hr/>

             <h5>Notes</h5>

             <div class="mb-3">
               <textarea name="markdownPage" class="form-control" id="form-trip-mdpage" rows="15">${formdata.markdownPage}</textarea>
             </div>

                 <hr/>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

     <script type="text/javascript">
            resetStageIndexes();
        </script>

</div>

<#include "_includes_tail.ftlh">