<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@spring.url '/admin/maps/new' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-map-gpx" class="form-label">Fichier GPX</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouvelle Map">
            </div>
        </form>
        <hr/>

        <#if maps?size gt 0>
            <ul class="list-group mt-4">
                <#list maps as map>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${map.name}<span class="text-muted"> - ${map.postedAt.format(_date_formatter)}</span><#if !map.visible> - <i class="bi bi-eye-slash"></i></#if></div>
                        <div class="btn-group" role="group">
                            <a href="<@spring.url '/admin/maps/${map.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@spring.url '/admin/maps/delete/${map.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>
         <#else>
             <div class="alert alert-warning" role="alert">
               Aucune map.
             </div>
         </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">