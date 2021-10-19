<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-configuration" action="<@common.teamUrl team.id '/admin/page' />" method="POST" class="border rounded p-4">

                <h5>FAQ</h5>

                 <div class="alert alert-info" role="alert">La syntaxe demandée est du markdown. Pour plus de simplicité, utilisez un éditeur de markdown en ligne puis copier le résultat ici.</div>

                <div class="mb-3">
                  <textarea name="markdownPage" class="form-control" id="form-configuration-mdpage" rows="15">${formdata.markdownPage}</textarea>
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>

        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">