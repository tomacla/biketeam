<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <div class="alert alert-warning" role="alert">
          Le logo est utilisé sur la page d'accueil, dans la barre de navigation et comme icône de favori.
        </div>

        <form enctype="multipart/form-data" name="form-logo" action="<@spring.url '/' + team.id +  '/admin/logo' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-logo-file" class="form-label">Nouveau logo (PNG ou JPEG)</label>
                  <input name="file" accept=".png,.jpg,.jpeg" class="form-control form-size-check" type="file" id="form-logo-file">
                </div>
                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <figure class="figure w-25 border rounded my-3">
            <img src="<@spring.url '/api/' + team.id + '/image' />" class="figure-img img-fluid rounded" alt="Team logo">
           <figcaption class="figure-caption text-center">Logo actuel.</figcaption>
        </figure>

    </div>

</div>

<#include "_includes_tail.ftlh">