<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-map"
            action="<@common.teamUrl team.id '/admin/maps/' + formdata.id />"
            method="POST" class="border rounded p-4">

                <div class="mb-3 row">
                    <div class="col-6">
                        <div class="form-check form-switch mb-3">
                          <input id="form-map-visible" name="visible" class="form-check-input" type="checkbox" id="flexCheckDefault"<#if formdata.visible?? && formdata.visible == 'on'> checked</#if>>
                          <label class="form-check-label" for="form-map-visible">
                            La map est visible
                          </label>
                        </div>
                    </div>
                    <div class="col-6 text-end">
                        <#if map.crossing>
                            <i class="bi bi-x"></i> Ca croise !
                        </#if>
                    </div>
                </div>

                <div class="mb-3 row">

                     <div class="col-6">
                          <label for="form-map-name" class="form-label">Nom</label>
                          <input autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control" id="form-map-name" placeholder="Parcours de la semaine">
                    </div>

                    <div class="col-6">
                        <label for="form-map-tags" class="form-label">Type</label>
                        <select name="type" class="form-select" id="form-map-type">
                            <option<#if map.type == 'ROAD'> selected</#if> value="ROAD">Route</option>
                            <option<#if map.type == 'GRAVEL'> selected</#if> value="GRAVEL">Gravel</option>
                            <option<#if map.type == 'MTB'> selected</#if> value="MTB">VTT</option>
                        </select>
                    </div>

                </div>

                 <div class="mb-3 row">
                    <div class="col-6">
                        <label for="form-map-tags" class="form-label">Tags</label>
                        <div class="input-group">
                            <select multiple data-remove-first="true" data-allow-new="true" name="tags" class="form-select" id="form-map-tags">
                                <option selected disabled hidden value="">Tag...</option>
                                <#list tags as tag>
                                    <option <#if formdata.tags?seq_contains(tag)>selected</#if> value="${tag}">${tag}</option>
                                </#list>
                            </select>
                        </div>
                    </div>

                    <div class="col-6">
                        <label for="form-map-newId" class="form-label">URL unique</label>
                        <input data-target-id-field="form-map-newId" minLength="2" maxLength="19" autocomplete="off" required value="${formdata.newId}" name="newId" type="text" class="form-control form-unique-id" id="form-map-newId">
                    </div>


                </div>

                <hr/>

                <div class="alert alert-info" role="alert">Mettre un nouveau fichier GPX uniquement pour remplacer l'existant. Sinon laisser vide.</div>

                <div class="mb-3">
                  <label for="form-map-file" class="form-label">GPX</label>
                  <input autocomplete="off" name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-file">
                </div>

                <hr/>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>

                <hr/>

                <h5>Informations sur la map</h5>

                <p>
                    <i class="bi bi-arrow-left-right"></i> ${map.length}km
                    <i class="bi bi-arrow-up"></i> ${map.positiveElevation}m
                    <i class="bi bi-arrow-down"></i> ${map.negativeElevation}m
                  </p>

                 <img src="<@common.teamUrl team.id '/maps/${map.id}/image' />" class="d-block shadow rounded w-75 h-auto mx-auto" alt="${map.id} image">


        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">