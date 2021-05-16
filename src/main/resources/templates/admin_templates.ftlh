<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="GET" action="<@spring.url '/admin/templates/new' />" class="row mb-0">
            <div class="col-auto">
                <select class="form-select" autocomplete="off" required name="numberOfGroups" id="form-template-groups">
                    <option value="">-- Nombre de groupes --</option>
                    <option value="1">1 groupe</option>
                    <option value="2">2 groupes</option>
                    <option value="3">3 groupes</option>
                    <option value="4">4 groupes</option>
                    <option value="5">5 groupes</option>
                </select>
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouveau template">
            </div>
        </form>
        <hr/>

        <#if templates?size gt 0>
            <ul class="list-group mt-4">
                <#list templates as template>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${template.name}</div>
                        <div class="btn-group" role="group">
                            <a href="<@spring.url '/admin/templates/${template.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@spring.url '/admin/templates/delete/${template.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun template.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">