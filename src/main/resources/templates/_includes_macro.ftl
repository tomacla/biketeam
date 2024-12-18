<#macro teamUrlPrefix teamId>${_siteUrl}<#if teamId != ''>/${teamId}</#if></#macro>
<#macro teamUrl teamId targetUrl><@common.teamUrlPrefix teamId />${targetUrl}</#macro>

<#macro pagination page pages pageFieldId formId>
  <div class="btn-group btn-group-sm" role="group">
    <button <#if page == 0>disabled</#if> onclick="setFieldValue('${pageFieldId}', 0); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-skip-backward-fill"></i></button>
    <button <#if page == 0>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${[page - 1, 0]?max}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-left-fill"></i></button>
    <span class="btn">${page + 1}/${pages}</span>
    <button <#if page == pages - 1>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${[page + 1, pages - 1]?min}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-right-fill"></i></button>
    <button <#if page == pages - 1>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${pages - 1}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-skip-forward-fill"></i></button>
  </div>
</#macro>

<#macro displayPlace header place>
  <div class="card">
    <h6 class="card-header">${header} - ${place.name}</h6>
    <div class="card-body py-2 px-2">
      <#if place.point??>
        <div id="mapPlace${place.id}" style="width:100%; height:150px"></div>
        <script type="text/javascript">initPlaceMap('mapPlace${place.id}', ${place.point.lat?c}, ${place.point.lng?c})</script>
      <#else>
        <p class="card-text small fst-italic">${place.address}</p>
      </#if>
    </div>
        <div class="card-body p-1">
          <#if place.point??>
            <a class="small link-dark me-2" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${place.point.lat?c},${place.point.lng?c}">Itinéraire</a>
          <#else>
            <a class="small link-dark me-2" target="_blank" href="https://www.google.com/maps/search/?api=1&query=${place.address?url('UTF-8')}">Itinéraire</a>
          </#if>
          <#if place.link??>
          <a class="small link-dark" href="${place.link}" target="_blank" class="card-link">Site web</a>
          </#if>
        </div>
  </div>
</#macro>

<#macro displayFeed withTeam>
    <div class="g-4 row">

        <form id="feed-filter" action="<#if team??><@common.teamUrl team.id '' /><#else><@common.teamUrl '' '' /></#if>" method="GET" class="col-12 mb-2">

            <div class="border rounded d-flex flex-row flex-wrap justify-content-center justify-content-md-around justify-content-md-start align-items-center p-2">

          <div class="d-flex flex-row align-items-center justify-content-center mb-2 mb-md-0 me-0 me-md-2">
              <div class="pe-1"><label for="feed-filter-from" class="form-label small">Du</label></div>
              <div><input autocomplete="off" type="date" name="from" value="${formdata.from}" class="form-control form-control-sm" id="feed-filter-from"></div>
              <div class="px-1"><label for="feed-filter-to" class="form-label small">Au</label></div>
              <div><input autocomplete="off" type="date" name="to" value="${formdata.to}" class="form-control form-control-sm" id="feed-filter-to"></div>
          </div>

            <#if _authenticated>
               <div class="form-check form-switch me-2">
                <input onchange="toggleVisibleButton('input-onlymyfeed-field')" class="form-check-input" type="checkbox" id="input-onlymyfeed-button" <#if formdata.onlyMyFeed?? && formdata.onlyMyFeed> checked</#if>>
                <label class="form-check-label" for="input-onlymyfeed-button">Mes activités</label>
              </div>
            </#if>

           <#if _authenticated>
            <input type="hidden" id="input-onlymyfeed-field" value="${formdata.onlyMyFeed?c}" name="onlyMyFeed" />
           </#if>

           <div>
               <button type="submit" id="feed-search-button" class="btn btn-secondary btn-sm"><i class="bi bi-funnel"></i> Filtrer</button>
           </div>

         </div>

        </form>
    </div>
    <div class="row g-4">
        <#if feed?size != 0>
            <#list feed as feedItem>
                <#if feedItem.feedType == 'RIDE'>
                    <@common.displayRide feedItem withTeam />
                <#elseif feedItem.feedType == 'TRIP'>
                    <@common.displayTrip feedItem withTeam />
                <#elseif feedItem.feedType == 'PUBLICATION'>
                    <@common.displayPublication feedItem withTeam />
                </#if>
            </#list>
        <#else>
            <div class="alert alert-warning" role="alert">
                <#if withTeam>
                    Vous n'avez pas d'actualité récente.
                <#else>
                    Ce groupe n'a pas d'actualité correspondant aux filtres sélectionnés.
                </#if>
             </div>
        </#if>
    </div>

    <script type="text/javascript">

        function toggleVisibleButton(fieldId) {

            var field = document.getElementById(fieldId);

            if(field.value === 'true') {
                field.value = "false";
            } else {
                field.value = "true";
            }

        }

    </script>

</#macro>

<#macro displayPublication publication withTeam>
    <div class="col-12">
        <div class="card" id="publication-${publication.id}">
            <div class="card-header p-1">
                <div class="d-flex flex-row justify-content-start align-items-start p-0">
                    <div>
                        <a class="link-dark text-decoration-none" href="<@common.teamUrl publication.teamId '' />"><img src="<@common.teamUrl publication.teamId '/image' />" height="50" alt="Team image"></a>
                    </div>
                    <div class="ms-3 d-flex flex-column align-items-start">
                        <div><#if withTeam><a class="link-dark" href="<@common.teamUrl publication.teamId '' />"><#list _user_teams?filter(t -> t.id == publication.teamId) as itemTeam>${itemTeam.name}</#list></a> - </#if><a class="link-dark" href="<@common.teamUrl publication.teamId '/publications/'+ publication.id />">${publication.title}</a></div>
                        <div class="small text-muted"><i class="bi bi-newspaper"></i> ${publication.publishedAt.format(_date_formatter)}</div>
                    </div>
                </div>
            </div>
            <#if publication.imaged>
            <img src="<@common.teamUrl publication.teamId '/publications/${publication.id}/image' />" class="card-img-top" alt="${publication.title} image">
            </#if>
            <div class="card-body">
              <p class="card-text wrap-content">${publication.content}</p>
              <#if publication.allowRegistration>
              <div class="accordion" id="publication-${publication.id}-accordion">
              <div class="accordion-item">
                <h2 class="accordion-header" id="publication-${publication.id}-accordion-title">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#publication-${publication.id}-accordion-content" aria-expanded="true" aria-controls="publication-${publication.id}-accordion-content">
                      Inscription (${publication.registrations?filter(r -> r.userEmailValid)?size} inscrit.e<#if publication.registrations?filter(r -> r.userEmailValid)?size gt 1>.s</#if>)
                    </button>
                  </h2>
                  <div id="publication-${publication.id}-accordion-content" class="accordion-collapse collapse" aria-labelledby="publication-${publication.id}-accordion-title" data-bs-parent="#publication-${publication.id}-accordion">
                <form action="<@common.teamUrl publication.teamId '/publications/${publication.id}/register' />" method="POST" class="p-2" id="publication-${publication.id}-registration">
                    <input type="hidden" name="id" value="${publication.id}" />
                    <div class="row mb-2">
                      <div class="col-md-4">
                          <label for="publication-${publication.id}-registration-name" class="form-label">Nom</label>
                          <input name="firstname" required="true" min="1" type="text" class="form-control" id="publication-${publication.id}-registration-name">
                        </div>
                        <div class="col-md-4">
                          <label for="publication-${publication.id}-registration-lastname" class="form-label">Prénom</label>
                          <input name="lastname" required="true" min="1" type="text" class="form-control" id="publication-${publication.id}-registration-lastname">
                        </div>
                        <div class="col-md-4">
                          <label for="publication-${publication.id}-registration-email" class="form-label">Email</label>
                          <input name="email" required="true" type="email" class="form-control" id="publication-${publication.id}-registration-email">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-secondary">S'inscrire</button>
                    </form>
                    </div>
                </div>
                </div>
              </#if>
            </div>
        </div>
      </div>
</#macro>

<#macro displayRide ride withTeam>
    <div class="col-12">
        <div class="card">
            <#if _date_today.isAfter(ride.date)>
                <div class="done-ribbon" data-ribbon="Terminé"></div>
            </#if>
            <div class="card-header p-1">
                <div class="d-flex flex-row justify-content-start align-items-start p-0">
                    <div>
                        <a class="link-dark text-decoration-none" href="<@common.teamUrl ride.teamId '' />"><img src="<@common.teamUrl ride.teamId '/image' />" height="50" alt="Team image"></a>
                    </div>
                    <div class="ms-3 d-flex flex-column align-items-start">
                        <div><#if withTeam><a class="link-dark" href="<@common.teamUrl ride.teamId '' />"><#list _user_teams?filter(t -> t.id == ride.teamId) as itemTeam>${itemTeam.name}</#list></a> - </#if><a class="link-dark" href="<@common.teamUrl ride.teamId '/rides/'+ ride.permalink!ride.id />">${ride.title}</a></div>
                        <div class="small text-muted"><i class="bi bi-bicycle"></i> ${ride.publishedAt.format(_date_formatter)}</div>
                    </div>
                </div>
            </div>
            <div class="card-body">
            <h5 class="card-title">${ride.date.format(_date_formatter)}</h5>
            <ul class="list-unstyled m-0">
                <li >${ride.groups?size} groupe<#if ride.groups?size gt 1>s</#if></li>
                <#if ride.sortedGroups?first.meetingTime != ride.sortedGroups?last.meetingTime>
                    <li>Départ de ${ride.sortedGroups?first.meetingTime} à ${ride.sortedGroups?last.meetingTime}<#if ride.startPlace??> - ${ride.startPlace.name}</#if></li>
                <#else>
                    <li>Départ à ${ride.sortedGroups?first.meetingTime}<#if ride.startPlace??> - ${ride.startPlace.name}</#if></li>
                </#if>
                  <#if ride.endPlace??>
                    <li>Arrivée : ${ride.endPlace.name}</li>
                  </#if>
             </ul>
            </div>
            <#if ride.imaged>
            <div class="card-body">

                  <div class="row justify-content-center">
                    <div class="col-12 col-md-6">
                      <a href="<@common.teamUrl ride.teamId '/rides/'+ ride.permalink!ride.id />"><img src="<@common.teamUrl ride.teamId '/rides/${ride.id}/image' />" style="max-width:500px;width:100%" class="mx-auto d-block shadow rounded w-100 h-auto mx-auto" alt="${ride.title} image"></a>
                    </div>
                  </div>
            </div>
            </#if>
            <div class="card-footer d-flex justify-content-center">
                    <div><a href="<@common.teamUrl ride.teamId '/rides/'+ ride.permalink!ride.id />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye-fill"></i> Détails</a></div>
                </div>
        </div>
      </div>
</#macro>

<#macro displayTrip trip withTeam>
    <div class="col-12">
      <div class="card">
        <#if _date_today.isAfter(trip.endDate)>
            <div class="done-ribbon" data-ribbon="Terminé"></div>
        </#if>
        <div class="card-header p-1">
            <div class="d-flex flex-row justify-content-start align-items-start p-0">
                <div>
                    <a class="link-dark text-decoration-none" href="<@common.teamUrl trip.teamId '' />"><img src="<@common.teamUrl trip.teamId '/image' />" height="50" alt="Team image"></a>
                </div>
                <div class="ms-3 d-flex flex-column align-items-start">
                    <div><#if withTeam><a class="link-dark" href="<@common.teamUrl trip.teamId '' />"><#list _user_teams?filter(t -> t.id == trip.teamId) as itemTeam>${itemTeam.name}</#list></a> - </#if><a class="link-dark" href="<@common.teamUrl trip.teamId '/trips/'+ trip.permalink!trip.id />">${trip.title}</a></div>
                    <div class="small text-muted"><i class="bi bi-signpost-2"></i> ${trip.publishedAt.format(_date_formatter)}</div>
                </div>
            </div>
        </div>
        <div class="card-body">
        <h5 class="card-title">${trip.date.format(_date_formatter)}</h5>
        <ul class="list-unstyled m-0">
            <li >${trip.stages?size} étape<#if trip.stages?size gt 1>s</#if></li>
            <li>Départ à ${trip.meetingTime}<#if trip.startPlace??> - ${trip.startPlace.name}</#if></li>
            <#if trip.endPlace??>
                <li>Arrivée : ${trip.endPlace.name}</li>
              </#if>
         </ul>
        </div>
        <#if trip.imaged>
        <div class="card-body">

              <div class="row justify-content-center">
                <div class="col-12 col-md-6">
                  <a href="<@common.teamUrl trip.teamId '/trips/'+ trip.permalink!trip.id />"><img src="<@common.teamUrl trip.teamId '/trips/${trip.id}/image' />" style="max-width:500px;width:100%" class="mx-auto d-block shadow rounded w-100 h-auto mx-auto" alt="${trip.title} image"></a>
                </div>
              </div>

        </div>
        </#if>
            <div class="card-footer d-flex justify-content-center">
                <div><a href="<@common.teamUrl trip.teamId '/trips/'+ trip.permalink!trip.id />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye-fill"></i> Détails</a></div>
            </div>
    </div>
  </div>
</#macro>

<#macro countrySelect selected name id required>
    <select<#if required> required</#if> class="form-select" name="${name}" id="${id}">
        <#if !required>
            <option value="">---</option>
        </#if>
        <option<#if selected == 'AF'> selected</#if> value="AF">Afghanistan</option>
        <option<#if selected == 'AL'> selected</#if> value="AL">Albanie</option>
        <option<#if selected == 'DZ'> selected</#if> value="DZ">Algérie</option>
        <option<#if selected == 'AS'> selected</#if> value="AS">Samoa américaine</option>
        <option<#if selected == 'AD'> selected</#if> value="AD">Andorre</option>
        <option<#if selected == 'AO'> selected</#if> value="AO">Angola</option>
        <option<#if selected == 'AI'> selected</#if> value="AI">Anguilla</option>
        <option<#if selected == 'AQ'> selected</#if> value="AQ">Antarctique</option>
        <option<#if selected == 'AG'> selected</#if> value="AG">Antigua et Barbuda</option>
        <option<#if selected == 'AR'> selected</#if> value="AR">Argentine</option>
        <option<#if selected == 'AM'> selected</#if> value="AM">Arménie</option>
        <option<#if selected == 'AW'> selected</#if> value="AW">Aruba</option>
        <option<#if selected == 'AU'> selected</#if> value="AU">Australie</option>
        <option<#if selected == 'AT'> selected</#if> value="AT">Autriche</option>
        <option<#if selected == 'AZ'> selected</#if> value="AZ">Azerbaïdjan</option>
        <option<#if selected == 'BS'> selected</#if> value="BS">Bahamas</option>
        <option<#if selected == 'BH'> selected</#if> value="BH">Bahrein</option>
        <option<#if selected == 'BD'> selected</#if> value="BD">Bangladesh</option>
        <option<#if selected == 'BB'> selected</#if> value="BB">Barbade</option>
        <option<#if selected == 'BY'> selected</#if> value="BY">Bielorussie</option>
        <option<#if selected == 'BE'> selected</#if> value="BE">Belgique</option>
        <option<#if selected == 'BZ'> selected</#if> value="BZ">Belize</option>
        <option<#if selected == 'BJ'> selected</#if> value="BJ">Bénin</option>
        <option<#if selected == 'BM'> selected</#if> value="BM">Bermudes</option>
        <option<#if selected == 'BT'> selected</#if> value="BT">Bhoutan</option>
        <option<#if selected == 'BO'> selected</#if> value="BO">Bolivie</option>
        <option<#if selected == 'BA'> selected</#if> value="BA">Bosnie-Herzégovine</option>
        <option<#if selected == 'BW'> selected</#if> value="BW">Botswana</option>
        <option<#if selected == 'BV'> selected</#if> value="BV">Île Bouvet</option>
        <option<#if selected == 'BR'> selected</#if> value="BR">Brésil</option>
        <option<#if selected == 'IO'> selected</#if> value="IO">Océan Indien Britannique</option>
        <option<#if selected == 'BN'> selected</#if> value="BN">Brunei Darussalam</option>
        <option<#if selected == 'BG'> selected</#if> value="BG">Bulgarie</option>
        <option<#if selected == 'BF'> selected</#if> value="BF">Burkina Faso</option>
        <option<#if selected == 'BI'> selected</#if> value="BI">Burundi</option>
        <option<#if selected == 'KH'> selected</#if> value="KH">Cambodge</option>
        <option<#if selected == 'CM'> selected</#if> value="CM">Cameroun</option>
        <option<#if selected == 'CA'> selected</#if> value="CA">Canada</option>
        <option<#if selected == 'CV'> selected</#if> value="CV">Cap-Vert</option>
        <option<#if selected == 'KY'> selected</#if> value="KY">Caïmanes</option>
        <option<#if selected == 'CF'> selected</#if> value="CF">Centrafricaine, République</option>
        <option<#if selected == 'TD'> selected</#if> value="TD">Tchad</option>
        <option<#if selected == 'CL'> selected</#if> value="CL">Chili</option>
        <option<#if selected == 'CN'> selected</#if> value="CN">Chine</option>
        <option<#if selected == 'CX'> selected</#if> value="CX">Île Christmas</option>
        <option<#if selected == 'CC'> selected</#if> value="CC">Cocos</option>
        <option<#if selected == 'CO'> selected</#if> value="CO">Colombie</option>
        <option<#if selected == 'KM'> selected</#if> value="KM">Comores</option>
        <option<#if selected == 'CG'> selected</#if> value="CG">République du Congo</option>
        <option<#if selected == 'CD'> selected</#if> value="CD">Congo, République démocratique</option>
        <option<#if selected == 'CK'> selected</#if> value="CK">Îles Cook</option>
        <option<#if selected == 'CR'> selected</#if> value="CR">Costa Rica</option>
        <option<#if selected == 'CI'> selected</#if> value="CI">Côte-d'Ivoire</option>
        <option<#if selected == 'HR'> selected</#if> value="HR">Croatie</option>
        <option<#if selected == 'CU'> selected</#if> value="CU">Cuba</option>
        <option<#if selected == 'CY'> selected</#if> value="CY">Chypre</option>
        <option<#if selected == 'CZ'> selected</#if> value="CZ">République Tchèque</option>
        <option<#if selected == 'DK'> selected</#if> value="DK">Danemark</option>
        <option<#if selected == 'DJ'> selected</#if> value="DJ">Djibouti</option>
        <option<#if selected == 'DM'> selected</#if> value="DM">Dominique</option>
        <option<#if selected == 'DO'> selected</#if> value="DO">République Dominicaine</option>
        <option<#if selected == 'EC'> selected</#if> value="EC">Équateur</option>
        <option<#if selected == 'EG'> selected</#if> value="EG">Égypte</option>
        <option<#if selected == 'SV'> selected</#if> value="SV">El Salvador</option>
        <option<#if selected == 'GQ'> selected</#if> value="GQ">Guinée équatoriale</option>
        <option<#if selected == 'ER'> selected</#if> value="ER">Érythrée</option>
        <option<#if selected == 'EE'> selected</#if> value="EE">Estonie</option>
        <option<#if selected == 'ET'> selected</#if> value="ET">Éthiopie</option>
        <option<#if selected == 'FK'> selected</#if> value="FK">Îles Malouines</option>
        <option<#if selected == 'FO'> selected</#if> value="FO">Îles Féroé</option>
        <option<#if selected == 'FJ'> selected</#if> value="FJ">Fidji</option>
        <option<#if selected == 'FI'> selected</#if> value="FI">Finlande</option>
        <option<#if selected == 'FR'> selected</#if> value="FR">France</option>
        <option<#if selected == 'GF'> selected</#if> value="GF">Guyane française</option>
        <option<#if selected == 'PF'> selected</#if> value="PF">Polynésie française</option>
        <option<#if selected == 'TF'> selected</#if> value="TF">Terres australes françaises</option>
        <option<#if selected == 'GA'> selected</#if> value="GA">Gabon</option>
        <option<#if selected == 'GM'> selected</#if> value="GM">Gambie</option>
        <option<#if selected == 'GE'> selected</#if> value="GE">Géorgie</option>
        <option<#if selected == 'DE'> selected</#if> value="DE">Allemagne</option>
        <option<#if selected == 'GH'> selected</#if> value="GH">Ghana</option>
        <option<#if selected == 'GI'> selected</#if> value="GI">Gibraltar</option>
        <option<#if selected == 'GR'> selected</#if> value="GR">Grèce</option>
        <option<#if selected == 'GL'> selected</#if> value="GL">Groenland</option>
        <option<#if selected == 'GD'> selected</#if> value="GD">Grenada</option>
        <option<#if selected == 'GP'> selected</#if> value="GP">Guadeloupe</option>
        <option<#if selected == 'GU'> selected</#if> value="GU">Guam</option>
        <option<#if selected == 'GT'> selected</#if> value="GT">Guatemala</option>
        <option<#if selected == 'GN'> selected</#if> value="GN">Guinée</option>
        <option<#if selected == 'GW'> selected</#if> value="GW">Guinée-Bissau</option>
        <option<#if selected == 'GY'> selected</#if> value="GY">Guyana</option>
        <option<#if selected == 'HT'> selected</#if> value="HT">Haïti</option>
        <option<#if selected == 'HM'> selected</#if> value="HM">Îles Heard-et-MacDonald</option>
        <option<#if selected == 'VA'> selected</#if> value="VA">Saint-Siège</option>
        <option<#if selected == 'HN'> selected</#if> value="HN">Honduras</option>
        <option<#if selected == 'HK'> selected</#if> value="HK">Hong Kong</option>
        <option<#if selected == 'HU'> selected</#if> value="HU">Hongrie</option>
        <option<#if selected == 'IS'> selected</#if> value="IS">Islande</option>
        <option<#if selected == 'IN'> selected</#if> value="IN">Inde</option>
        <option<#if selected == 'ID'> selected</#if> value="ID">Indonésie</option>
        <option<#if selected == 'IR'> selected</#if> value="IR">Iran</option>
        <option<#if selected == 'IQ'> selected</#if> value="IQ">Irak</option>
        <option<#if selected == 'IE'> selected</#if> value="IE">Irlande</option>
        <option<#if selected == 'IL'> selected</#if> value="IL">Israël</option>
        <option<#if selected == 'IT'> selected</#if> value="IT">Italie</option>
        <option<#if selected == 'JM'> selected</#if> value="JM">Jamaïque</option>
        <option<#if selected == 'JP'> selected</#if> value="JP">Japon</option>
        <option<#if selected == 'JO'> selected</#if> value="JO">Jordanie</option>
        <option<#if selected == 'KZ'> selected</#if> value="KZ">Kazakhstan</option>
        <option<#if selected == 'KE'> selected</#if> value="KE">Kenya</option>
        <option<#if selected == 'KI'> selected</#if> value="KI">Kiribati</option>
        <option<#if selected == 'KP'> selected</#if> value="KP">Corée du Nord, République populaire démocratique</option>
        <option<#if selected == 'KR'> selected</#if> value="KR">Corée du Sud, République</option>
        <option<#if selected == 'KW'> selected</#if> value="KW">Koweit</option>
        <option<#if selected == 'KG'> selected</#if> value="KG">Kirghistan</option>
        <option<#if selected == 'LA'> selected</#if> value="LA">Laos</option>
        <option<#if selected == 'LV'> selected</#if> value="LV">Lettonie</option>
        <option<#if selected == 'LB'> selected</#if> value="LB">Liban</option>
        <option<#if selected == 'LS'> selected</#if> value="LS">Lesotho</option>
        <option<#if selected == 'LR'> selected</#if> value="LR">Libéria</option>
        <option<#if selected == 'LY'> selected</#if> value="LY">Libye</option>
        <option<#if selected == 'LI'> selected</#if> value="LI">Liechtenstein</option>
        <option<#if selected == 'LT'> selected</#if> value="LT">Lituanie</option>
        <option<#if selected == 'LU'> selected</#if> value="LU">Luxembourg</option>
        <option<#if selected == 'MO'> selected</#if> value="MO">Macao</option>
        <option<#if selected == 'MK'> selected</#if> value="MK">Macédoine du Nord</option>
        <option<#if selected == 'MG'> selected</#if> value="MG">Madagascar</option>
        <option<#if selected == 'MW'> selected</#if> value="MW">Malawi</option>
        <option<#if selected == 'MY'> selected</#if> value="MY">Malaisie</option>
        <option<#if selected == 'MV'> selected</#if> value="MV">Maldives</option>
        <option<#if selected == 'ML'> selected</#if> value="ML">Mali</option>
        <option<#if selected == 'MT'> selected</#if> value="MT">Malte</option>
        <option<#if selected == 'MH'> selected</#if> value="MH">Îles Marshall</option>
        <option<#if selected == 'MQ'> selected</#if> value="MQ">Martinique</option>
        <option<#if selected == 'MR'> selected</#if> value="MR">Mauritanie</option>
        <option<#if selected == 'MU'> selected</#if> value="MU">Maurice</option>
        <option<#if selected == 'YT'> selected</#if> value="YT">Mayotte</option>
        <option<#if selected == 'MX'> selected</#if> value="MX">Mexique</option>
        <option<#if selected == 'FM'> selected</#if> value="FM">Micronésie</option>
        <option<#if selected == 'MD'> selected</#if> value="MD">Moldavie</option>
        <option<#if selected == 'MC'> selected</#if> value="MC">Monaco</option>
        <option<#if selected == 'MN'> selected</#if> value="MN">Mongolie</option>
        <option<#if selected == 'MS'> selected</#if> value="MS">Montserrat</option>
        <option<#if selected == 'MA'> selected</#if> value="MA">Maroc</option>
        <option<#if selected == 'MZ'> selected</#if> value="MZ">Mozambique</option>
        <option<#if selected == 'MM'> selected</#if> value="MM">Myanmar</option>
        <option<#if selected == 'NA'> selected</#if> value="NA">Namibie</option>
        <option<#if selected == 'NR'> selected</#if> value="NR">Nauru</option>
        <option<#if selected == 'NP'> selected</#if> value="NP">Népal</option>
        <option<#if selected == 'NL'> selected</#if> value="NL">Pays-Bas</option>
        <option<#if selected == 'NC'> selected</#if> value="NC">Nouvelle-Calédonie</option>
        <option<#if selected == 'NZ'> selected</#if> value="NZ">Nouvelle-Zélande</option>
        <option<#if selected == 'NI'> selected</#if> value="NI">Nicaragua</option>
        <option<#if selected == 'NE'> selected</#if> value="NE">Niger</option>
        <option<#if selected == 'NG'> selected</#if> value="NG">Nigéria</option>
        <option<#if selected == 'NU'> selected</#if> value="NU">Niué</option>
        <option<#if selected == 'NF'> selected</#if> value="NF">Île Norfolk</option>
        <option<#if selected == 'MP'> selected</#if> value="MP">Mariannes du Nord</option>
        <option<#if selected == 'NO'> selected</#if> value="NO">Norvège</option>
        <option<#if selected == 'OM'> selected</#if> value="OM">Oman</option>
        <option<#if selected == 'PK'> selected</#if> value="PK">Pakistan</option>
        <option<#if selected == 'PW'> selected</#if> value="PW">Palau</option>
        <option<#if selected == 'PS'> selected</#if> value="PS">Palestine</option>
        <option<#if selected == 'PA'> selected</#if> value="PA">Panama</option>
        <option<#if selected == 'PG'> selected</#if> value="PG">Papouasie-Nouvelle-Guinée</option>
        <option<#if selected == 'PY'> selected</#if> value="PY">Paraguay</option>
        <option<#if selected == 'PE'> selected</#if> value="PE">Pérou</option>
        <option<#if selected == 'PH'> selected</#if> value="PH">Philippines</option>
        <option<#if selected == 'PN'> selected</#if> value="PN">Pitcairn</option>
        <option<#if selected == 'PL'> selected</#if> value="PL">Pologne</option>
        <option<#if selected == 'PT'> selected</#if> value="PT">Portugal</option>
        <option<#if selected == 'PR'> selected</#if> value="PR">Porto Rico</option>
        <option<#if selected == 'QA'> selected</#if> value="QA">Qatar</option>
        <option<#if selected == 'RE'> selected</#if> value="RE">Réunion</option>
        <option<#if selected == 'RO'> selected</#if> value="RO">Roumanie</option>
        <option<#if selected == 'RU'> selected</#if> value="RU">Russie</option>
        <option<#if selected == 'RW'> selected</#if> value="RW">Rwanda</option>
        <option<#if selected == 'SH'> selected</#if> value="SH">Sainte-Hélène</option>
        <option<#if selected == 'KN'> selected</#if> value="KN">Saint-Christophe-et-Niévès</option>
        <option<#if selected == 'LC'> selected</#if> value="LC">Sainte-Lucie</option>
        <option<#if selected == 'PM'> selected</#if> value="PM">Saint Pierre and Miquelon</option>
        <option<#if selected == 'VC'> selected</#if> value="VC">Saint-Vincent et les Grenadines</option>
        <option<#if selected == 'WS'> selected</#if> value="WS">Samoa</option>
        <option<#if selected == 'SM'> selected</#if> value="SM">Saint-Marin</option>
        <option<#if selected == 'ST'> selected</#if> value="ST">São Tomé et Principe</option>
        <option<#if selected == 'SA'> selected</#if> value="SA">Arabie Saoudite</option>
        <option<#if selected == 'SN'> selected</#if> value="SN">Sénégal</option>
        <option<#if selected == 'SC'> selected</#if> value="SC">Seychelles</option>
        <option<#if selected == 'SL'> selected</#if> value="SL">Sierra Leone</option>
        <option<#if selected == 'SG'> selected</#if> value="SG">Singapour</option>
        <option<#if selected == 'SK'> selected</#if> value="SK">Slovaquie</option>
        <option<#if selected == 'SI'> selected</#if> value="SI">Slovénie</option>
        <option<#if selected == 'SB'> selected</#if> value="SB">Salomon</option>
        <option<#if selected == 'SO'> selected</#if> value="SO">Somalie</option>
        <option<#if selected == 'ZA'> selected</#if> value="ZA">Afrique du Sud</option>
        <option<#if selected == 'GS'> selected</#if> value="GS">Géorgie du Sud-et-les Îles Sandwich du Sud</option>
        <option<#if selected == 'ES'> selected</#if> value="ES">Espagne</option>
        <option<#if selected == 'LK'> selected</#if> value="LK">Sri Lanka</option>
        <option<#if selected == 'SD'> selected</#if> value="SD">Soudan</option>
        <option<#if selected == 'SR'> selected</#if> value="SR">Suriname</option>
        <option<#if selected == 'SJ'> selected</#if> value="SJ">Svalbard et Île Jan Mayen</option>
        <option<#if selected == 'SZ'> selected</#if> value="SZ">Ngwane, Royaume d'Eswatini</option>
        <option<#if selected == 'SE'> selected</#if> value="SE">Suède</option>
        <option<#if selected == 'CH'> selected</#if> value="CH">Suisse</option>
        <option<#if selected == 'SY'> selected</#if> value="SY">Syrie</option>
        <option<#if selected == 'TW'> selected</#if> value="TW">Taïwan</option>
        <option<#if selected == 'TJ'> selected</#if> value="TJ">Tadjikistan</option>
        <option<#if selected == 'TZ'> selected</#if> value="TZ">Tanzanie, République unie</option>
        <option<#if selected == 'TH'> selected</#if> value="TH">Thaïlande</option>
        <option<#if selected == 'TL'> selected</#if> value="TL">Timor Leste</option>
        <option<#if selected == 'TG'> selected</#if> value="TG">Togo</option>
        <option<#if selected == 'TK'> selected</#if> value="TK">Tokelau</option>
        <option<#if selected == 'TO'> selected</#if> value="TO">Tonga</option>
        <option<#if selected == 'TT'> selected</#if> value="TT">Trinidad et Tobago</option>
        <option<#if selected == 'TN'> selected</#if> value="TN">Tunisie</option>
        <option<#if selected == 'TR'> selected</#if> value="TR">Turquie</option>
        <option<#if selected == 'TM'> selected</#if> value="TM">Turkménistan</option>
        <option<#if selected == 'TC'> selected</#if> value="TC">Îles Turques-et-Caïques</option>
        <option<#if selected == 'TV'> selected</#if> value="TV">Tuvalu</option>
        <option<#if selected == 'UG'> selected</#if> value="UG">Ouganda</option>
        <option<#if selected == 'UA'> selected</#if> value="UA">Ukraine</option>
        <option<#if selected == 'AE'> selected</#if> value="AE">Émirats Arabes Unis</option>
        <option<#if selected == 'GB'> selected</#if> value="GB">Royaume-Uni</option>
        <option<#if selected == 'US'> selected</#if> value="US">États-Unis d'Amérique</option>
        <option<#if selected == 'UM'> selected</#if> value="UM">Îles mineures éloignées des États-Unis</option>
        <option<#if selected == 'UY'> selected</#if> value="UY">Uruguay</option>
        <option<#if selected == 'UZ'> selected</#if> value="UZ">Ouzbékistan</option>
        <option<#if selected == 'VU'> selected</#if> value="VU">Vanuatu</option>
        <option<#if selected == 'VE'> selected</#if> value="VE">Venezuela</option>
        <option<#if selected == 'VN'> selected</#if> value="VN">Vietnam</option>
        <option<#if selected == 'VG'> selected</#if> value="VG">Îles vierges britanniques</option>
        <option<#if selected == 'VI'> selected</#if> value="VI">Îles vierges américaines</option>
        <option<#if selected == 'WF'> selected</#if> value="WF">Wallis et Futuna</option>
        <option<#if selected == 'EH'> selected</#if> value="EH">Sahara occidental</option>
        <option<#if selected == 'YE'> selected</#if> value="YE">Yémen</option>
        <option<#if selected == 'ZM'> selected</#if> value="ZM">Zambie</option>
        <option<#if selected == 'ZW'> selected</#if> value="ZW">Zimbabwe</option>
        <option<#if selected == 'AX'> selected</#if> value="AX">Åland</option>
        <option<#if selected == 'BQ'> selected</#if> value="BQ">Bonaire, Saint-Eustache et Saba</option>
        <option<#if selected == 'CW'> selected</#if> value="CW">Curaçao</option>
        <option<#if selected == 'GG'> selected</#if> value="GG">Guernesey</option>
        <option<#if selected == 'IM'> selected</#if> value="IM">Île de Man</option>
        <option<#if selected == 'JE'> selected</#if> value="JE">Jersey</option>
        <option<#if selected == 'ME'> selected</#if> value="ME">Monténégro</option>
        <option<#if selected == 'BL'> selected</#if> value="BL">Saint-Barthélemy</option>
        <option<#if selected == 'MF'> selected</#if> value="MF">Saint-Martin (partie française)</option>
        <option<#if selected == 'RS'> selected</#if> value="RS">Serbie</option>
        <option<#if selected == 'SX'> selected</#if> value="SX">Saint-Martin (partie néerlandaise)</option>
        <option<#if selected == 'SS'> selected</#if> value="SS">Sud-Soudan</option>
        <option<#if selected == 'XK'> selected</#if> value="XK">Kosovo"</option>
    </select>
</#macro>

<#macro displayMapType mapType>
    <#if mapType == 'ROAD'>Route</#if>
    <#if mapType == 'GRAVEL'>Gravel</#if>
    <#if mapType == 'MTB'>VTT</#if>
</#macro>