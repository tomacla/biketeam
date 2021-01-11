<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-publication"
            action="<#if formdata.id??><@spring.url '/admin/publications/' + formdata.id /><#else><@spring.url '/admin/publications' /></#if>"
            method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-publication-title" class="form-label">Titre</label>
                  <input autocomplete="off" required="true" value="${formdata.title}" name="title" type="text" class="form-control" id="form-publication-title" placeholder="Actualité de la semaine">
                </div>
                <div class="mb-3">
                  <label for="form-publication-content" class="form-label">Contenu</label>
                  <textarea required="true" name="content" class="form-control" id="form-publication-content" rows="10">${formdata.content}</textarea>
                </div>
                <#if formdata.id??>
                    <div class="alert alert-warning" role="alert">
                        Remplissez le champ image pour modifier l'image de la publication. Pour conserver l'image existante, ne rien modifier.
                    </div>
                </#if>
                <div class="mb-3">
                  <label for="form-publication-file" class="form-label">Image associée (format jpg uniquement)</label>
                  <input autocomplete="off" name="file" accept=".jpg" class="form-control form-size-check" type="file" id="form-publication-file">
                </div>
                <button type="submit" class="btn btn-secondary">Publier</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">