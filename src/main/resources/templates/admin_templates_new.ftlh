<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form id="form-ride"
            enctype="multipart/form-data"
            name="form-ride"
            action="<@spring.url '/admin/templates/' + formdata.id />"
            method="POST"
            class="border rounded p-4">

                <div class="row">
                    <div class="col-6 mb-3">
                      <label for="form-ride-name" class="form-label">Nom</label>
                      <input autocomplete="off" required="true" value="${formdata.name}" name="name" type="text" class="form-control" id="form-ride-name" placeholder="Sortie hebdomadaire">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-type" class="form-label">Type</label>
                      <select required="true" name="type" class="form-select" id="form-ride-type">
                        <option value="REGULAR"<#if formdata.type == 'REGULAR'> selected</#if>>Sortie régulière</option>
                        <option value="SPECIAL"<#if formdata.type == 'SPECIAL'> selected</#if>>Evénement exceptionnel</option>
                        <option value="RACE"<#if formdata.type == 'RACE'> selected</#if>>Course</option>
                      </select>
                    </div>
                </div>

                <div class="mb-3">
                  <label for="form-ride-description" class="form-label">Description</label>
                  <textarea required="true" name="description" class="form-control" id="form-ride-description" rows="5">${formdata.description}</textarea>
                </div>

                <hr/>

                <h5>Groupes</h5>
                <div id="form-ride-groups">
                    <#list formdata.groups as group>
                        <div class="border border-dark rounded p-2 m-2">
                            <div class="row">
                                <div class="mb-3 col-6">
                                    <input type="hidden" value="${group.id}" name="groups[${group?index}].id" id="form-ride-group-id-${group?index}">
                                    <label for="form-ride-group-name-${group?index}" class="form-label">Nom</label>
                                    <input type="text" autocomplete="off" required="true" value="${group.name}" name="groups[${group?index}].name" id="form-ride-group-name-${group?index}" placeholder="G1 rapide" class="form-control">
                                </div>
                                <div class="mb-3 col-6">
                                    <label for="form-ride-group-time-${group?index}" class="form-label">Heure de départ</label>
                                    <input type="time" autocomplete="off" required="true" value="${group.meetingTime}" name="groups[${group?index}].meetingTime" id="form-ride-group-time-${group?index}" class="form-control">
                                </div>
                            </div>
                            <div class="row">
                                <div class="mb-3 col-4">
                                    <label for="form-ride-group-lowerspeed-${group?index}" class="form-label">Vitesse basse</label>
                                    <div class="input-group">
                                        <input type="number" autocomplete="off" required="true" value="${group.lowerSpeed}" name="groups[${group?index}].lowerSpeed" id="form-ride-group-lowerspeed-${group?index}" placeholder="30.0" class="form-control">
                                        <span class="input-group-text">km/h</span>
                                    </div>
                                </div>
                                <div class="mb-3 col-4">
                                    <label for="form-ride-group-upperspeed-${group?index}" class="form-label">Vitesse haute</label>
                                    <div class="input-group"><input type="number" autocomplete="off" required="true" value="${group.upperSpeed}" name="groups[${group?index}].upperSpeed" id="form-ride-group-upperspeed-${group?index}" placeholder="30.0" class="form-control">
                                    <span class="input-group-text">km/h</span>
                                </div>
                            </div>
                            <div class="row">
                                <div class="mb-3">
                                    <label for="form-ride-group-meetinglocation-${group?index}" class="form-label">Lieu de rendez vous</label>
                                    <input type="text" autocomplete="off" required="true" value="${group.meetingLocation}" name="groups[${group?index}].meetingLocation" id="form-ride-group-meetinglocation-${group?index}" placeholder="Place des fêtes" class="form-input-geocode form-control">
                                    <input type="hidden" value="${group.meetingPointLat?c}" name="groups[${group?index}].meetingPointLat" id="form-ride-group-meetinglocation-${group?index}-lat">
                                    <input type="hidden" value="${group.meetingPointLng?c}" name="groups[${group?index}].meetingPointLng" id="form-ride-group-meetinglocation-${group?index}-lng">
                                </div>
                           </div>
                        </div>

                      </div>

                    </#list>

                 </div>

                 <hr/>

                <button type="submit" class="btn btn-secondary">Publier</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">