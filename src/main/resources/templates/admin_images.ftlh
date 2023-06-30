<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" name="form-logo" action="<@common.teamUrl '' '/admin/images/logo' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-logo-file" class="form-label">Logo (PNG)</label>
                  <input name="file" accept=".png" class="form-control form-size-check" type="file" id="form-logo-file">
                </div>
                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <figure class="figure w-25 border rounded my-3">
            <img src="<@common.teamUrl '' '/misc/logo.png' />" class="figure-img img-fluid rounded" alt="Team logo">
           <figcaption class="figure-caption text-center">Logo actuel.</figcaption>
        </figure>

        <form enctype="multipart/form-data" name="form-home" action="<@common.teamUrl '' '/admin/images/home' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-home-file" class="form-label">Bouton retour (PNG)</label>
                  <input name="file" accept=".png" class="form-control form-size-check" type="file" id="form-home-file">
                </div>
                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <figure class="figure w-25 border rounded my-3">
            <img src="<@common.teamUrl '' '/misc/home.png' />" class="figure-img img-fluid rounded" alt="Home">
           <figcaption class="figure-caption text-center">Bouton actuel.</figcaption>
        </figure>

        <form enctype="multipart/form-data" name="form-favicon" action="<@common.teamUrl '' '/admin/images/favicon' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-favicon-file" class="form-label">Favicon (PNG)</label>
                  <input name="file" accept=".png" class="form-control form-size-check" type="file" id="form-favicon-file">
                </div>
                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <figure class="figure w-25 border rounded my-3">
            <img src="<@common.teamUrl '' '/misc/favicon.png' />" class="figure-img img-fluid rounded" alt="favicon">
           <figcaption class="figure-caption text-center">Favicon actuelle.</figcaption>
        </figure>

    </div>

</div>

<#include "_includes_tail.ftlh">