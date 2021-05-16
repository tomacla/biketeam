<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-publication"
            action="<@spring.url '/admin/publications/' + formdata.id />"
            method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-publication-title" class="form-label">Titre</label>
                  <input autocomplete="off" required value="${formdata.title}" name="title" type="text" class="form-control" id="form-publication-title" placeholder="Actualité de la semaine">
                </div>
                <div class="mb-3">
                  <label for="form-publication-content" class="form-label">Contenu</label>
                  <textarea required name="content" class="form-control" id="form-publication-content" rows="10">${formdata.content}</textarea>
                </div>
                <#if formdata.id != 'new'>
                    <div class="alert alert-warning" role="alert">
                        Remplissez le champ image pour modifier l'image de la publication. Pour conserver l'image existante, ne rien modifier.
                    </div>
                </#if>
                <div class="mb-3">
                  <label for="form-publication-file" class="form-label">Image associée (PNG ou JPEG)</label>
                  <input autocomplete="off" name="file" accept=".png,.jpg" class="form-control form-size-check" type="file" id="form-publication-file">
                </div>
                <div class="row mb-3">
                    <div class="col-6 mb-3">
                      <label for="form-ride-title" class="form-label">Date de publication</label>
                      <input autocomplete="off" required value="${formdata.publishedAtDate}" name="publishedAtDate" type="date" class="form-control" id="form-ride-publishedat-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-date" class="form-label">Heure de publication</label>
                      <input autocomplete="off" required value="${formdata.publishedAtTime}" name="publishedAtTime" type="time" class="form-control" id="form-ride-publishedat-time">
                    </div>
                </div>
                <button type="submit" class="btn btn-secondary">Publier</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">