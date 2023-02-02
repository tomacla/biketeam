<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

     <script type="text/javascript">

            function confirmDeleteUser(targetUrl) {
                var confirmedDeletion = confirm('Votre compte sera définitivement supprimé ainsi que toutes les données associées. Confirmez vous ?');
                if(confirmedDeletion) {
                    window.location=targetUrl;
                }
            }

        </script>

    <div class="col-12 col-md-4 mb-4">

        <div class="border rounded p-2">

            <h5>${(user.firstName)!'inconnu'} ${(user.lastName)!'inconnu'}</h5>
            <div class="mb-3">
               <img class="rounded" src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="profile">
            </div>
            <dl class="mb-3">
                <dt>Ville</dt>
                <#if user.city??>
                    <dd>${user.city}</dd>
                <#else>
                    <dd class="text-muted">Non renseignée</dd>
                </#if>
                <hr/>
                <a href="#" onclick="confirmDeleteUser('<@common.teamUrl '' '/users/me/delete' />')" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i> Supprimer mon compte</a>
                <hr/>
                <dt><i class="bi bi-strava"></i> Strava</dt>
                <#if user.stravaId??>
                        <dd><a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>
                <dt><i class="bi bi-google"></i> Google</dt>
                <#if user.googleId??>
                    <dd>Connecté</dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>
                <dt><i class="bi bi-facebook"></i> Facebook</dt>
                <#if user.facebookId??>
                        <dd>Connecté</dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>

            </dl>

        </div>

    </div>

    <div class="col-12 col-md-8">

        <form name="form-user" action="<@common.teamUrl '' '/users/me' />" method="POST" class="border rounded p-4">

               <h4>Préférences</h4>

             <h5>Strava</h5>

             <div class="alert alert-info" role="alert">
                Le Strava ID permet de reconnaitre l'utilisateur lors de la connexion avec Strava.
              </div>

             <div class="mb-3">
               <label for="form-user-strava" class="form-label">Strava ID</label>
               <input autocomplete="off" name="stravaId" value="${formdata.stravaId}" type="text" class="form-control" id="form-user-strava">
             </div>

             <hr/>

             <h5>Email</h5>

             <div class="alert alert-info" role="alert">
                 L'adresse email permet de reconnaitre l'utilisateur lors de la connexion Google ou Facebook.
               </div>

              <div class="mb-3">
                <label for="form-user-email" class="form-label">Email</label>
                <input autocomplete="off" name="email" value="${formdata.email}" type="email" class="form-control" id="form-user-email">
              </div>
              
               <div class="row mb-3">
              
                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishRides" type="checkbox" id="form-configuration-emailrides"<#if formdata.emailPublishRides?? && formdata.emailPublishRides == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailrides">Recevoir les rides</label>
                         </div>
                  </div>
    
                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishTrips" type="checkbox" id="form-configuration-emailtrips"<#if formdata.emailPublishTrips?? && formdata.emailPublishTrips == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailtrips">Recevoir les trips</label>
                         </div>
                  </div>
    
                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishPublications" type="checkbox" id="form-configuration-emailpubs"<#if formdata.emailPublishPublications?? && formdata.emailPublishPublications == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailpubs">Recevoir les publications</label>
                         </div>
                  </div>
    
              </div>
              
              <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <div class="border rounded mt-2 p-4">
            <h4>Maps favorites</h4>

            <div>
                <#list _user.mapFavorites as favorite>
                    <span class="text-nowrap me-2 mb-2"><img src="<@common.teamUrl favorite.teamId '/image' />" height="18" alt="Team image"> <a href="<@common.teamUrl favorite.teamId '/maps/${favorite.permalink!favorite.id}' />" class="link-dark" role="button"> ${favorite.name}</a></span>
                </#list>
            </div>

        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">