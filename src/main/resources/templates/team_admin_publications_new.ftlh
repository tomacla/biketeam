<#include "_includes_head.ftlh">

<#if formdata.id == 'new'>
    <script type="text/javascript">

            function validateForm(form) {

                var submitDate = document.getElementById('form-ride-publishedat-date').value;
                var submitTime = document.getElementById('form-ride-publishedat-time').value;
                var submitDateTime = new Date(Date.parse(submitDate+'T'+submitTime));

                return confirm('La publication sera publiée le ' + submitDateTime.toLocaleDateString() + ' à ' + submitTime + '. Confirmez vous la création de la publication ?');

            }

    </script>
</#if>

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">
    
        <form enctype="multipart/form-data" name="form-publication"
            action="<@common.teamUrl team.id '/admin/publications/' + formdata.id />"
            <#if formdata.id == 'new'>onsubmit="return validateForm(this);"</#if>
            method="POST"
            class="border rounded p-4">

                <div class="row mb-3 border rounded bg-light p-2">
                    <#if published>
                        <div class="alert alert-warning p-2" role="alert">
                            <i class="bi bi-calendar-check"></i> La publication a été publiée.
                        </div>
                    </#if>
                    <div class="col-6 mb-3">
                      <label for="form-ride-title" class="form-label">Date de publication</label>
                      <input <#if published>disabled </#if>autocomplete="off" required value="${formdata.publishedAtDate}" name="publishedAtDate" type="date" class="form-control" id="form-ride-publishedat-date">
                    </div>
                    <div class="col-6 mb-3">
                      <label for="form-ride-date" class="form-label">Heure de publication</label>
                      <input <#if published>disabled </#if>autocomplete="off" required value="${formdata.publishedAtTime}" name="publishedAtTime" type="time" class="form-control" id="form-ride-publishedat-time">
                    </div>
                </div>

            <div class="col-12 mb-3">
                        <div class="border rounded p-2">
                            <div class="form-check form-switch">
                             <input class="form-check-input" name="allowRegistration" type="checkbox" id="form-publication-allowRegistration"<#if formdata.allowRegistration?? && formdata.allowRegistration == 'on'> checked</#if>>
                             <label class="form-check-label" for="form-ride-allowRegistration">Permettre les inscriptions</label>
                           </div>
                           <div id="form-ride-allowRegistration-help" class="form-text">Si l'option est désactivée, vous pourrez récolter une liste d'inscrits (gestion d'événements).</div>
                       </div>
                    </div>

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
                  <input autocomplete="off" name="file" accept=".png,.jpg,.jpeg" class="form-control form-size-check" type="file" id="form-publication-file">
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <#if publication?? && publication.allowRegistration>
            <div class="mt-4">
            <h2>Liste des inscrits</h2>
            <table class="table">
            <thead>
             <tr>
                <th>Nom</th>
                <th>Email</th>
               </tr>
            </thead>
            <tbody>
            <#list publication.registrations?filter(r -> r.userEmailValid) as registration>
                   <tr>
                    <td>${registration.userName}</td>
                    <td>${registration.userEmail}</td>
                   </tr>
            </#list>
            </tbody>
            </table>
            </div>
            </#if>

    </div>



</div>

<#include "_includes_tail.ftlh">