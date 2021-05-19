<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-map"
            action="<@spring.url '/admin/maps/' + formdata.id />"
            method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-map-name" class="form-label">Nom</label>
                  <input autocomplete="off" required value="${formdata.name}" name="name" type="text" class="form-control" id="form-map-name" placeholder="Parcours de la semaine">
                </div>

                 <div class="mb-3">
                    <div class="form-check mb-3">
                      <input id="form-map-visible" name="visible" class="form-check-input" type="checkbox" id="flexCheckDefault"<#if formdata.visible> checked</#if>>
                      <label class="form-check-label" for="form-map-visible">
                        La map est visible
                      </label>
                    </div>
                </div>

                <div class="mb-3">

                    <label for="form-map-tags" class="form-label">Tags</label>
                    <div class="input-group">
                        <select onchange="handleTagChange('form-map-tags', 'tags', 'form-map-tags-container', 'form-map-tags-field-container')" class="form-select" id="form-map-tags">
                            <option value="">---</option>
                            <#list tags as tag>
                            <option value="${tag}">${tag}</option>
                            </#list>
                        </select>
                        <input autocomplete="off" id="form-map-tags-add" type="text" class="form-control" placeholder="nouveau">
                        <button class="btn btn-secondary" type="button" onclick="addTag('form-map-tags-add', 'tags', 'form-map-tags-container', 'form-map-tags-field-container')"><strong><i class="bi bi-plus"></i></strong></button>
                    </div>
                    <div id="form-map-tags-container"></div>
                    <div id="form-map-tags-field-container">
                        <input type="hidden" name="tags" value="" />
                    </div>

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

                 <img src="<@spring.url '/api/maps/${map.id}/image' />" class="d-block shadow rounded w-75 h-auto mx-auto" alt="${map.id} image">


        </form>

    </div>

     <script type="text/javascript">

        <#list formdata.tags as tag>
            createTag('${tag}', 'tags', 'form-map-tags-container', 'form-map-tags-field-container');
       </#list>

    </script>

</div>

<#include "_includes_tail.ftlh">