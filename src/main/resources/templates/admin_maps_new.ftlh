<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <script type="text/javascript">

        function createTag(label) {

            if(label === null || label === '') {
                return;
            }

            var badge = document.createElement("span");
            badge.classList.add('badge');
            badge.classList.add('bg-secondary');
            badge.classList.add('me-1');
            badge.innerHTML = label;


            var button = document.createElement("button");
            button.setAttribute('type', 'button');
            button.classList.add('btn-close');
            button.style.padding = '0';
            button.style.margin = '0 0 0 5px';
            button.style.width = '10px';
            button.style.height = '10px';
            button.addEventListener('click', function(event) {
                badge.parentNode.removeChild(badge);
            });

            badge.appendChild(button);

            document.getElementById('form-map-tags-container').appendChild(badge);
        }

        function addTag() {
           var tagInput = document.getElementById('form-map-tags');
           createTag(tagInput.value);
           tagInput.value = '';
        }

        function toggleMapVisibility() {

            var currentValue = document.getElementById('form-map-visible').value;

            if(currentValue === 'false') {
                document.getElementById('form-map-visible').value = 'true';
                document.getElementById('form-map-visible-toggle').classList.remove('btn-outline-secondary');
                document.getElementById('form-map-visible-toggle').classList.add('btn-secondary');
                 document.getElementById('form-map-visible-toggle').innerHTML= 'La map est visible';
            } else {
                document.getElementById('form-map-visible').value = 'false';
                document.getElementById('form-map-visible-toggle').classList.remove('btn-secondary');
                document.getElementById('form-map-visible-toggle').classList.add('btn-outline-secondary');
                 document.getElementById('form-map-visible-toggle').innerHTML= 'La map est invisible';
            }

        }

        function handleTags() {

            var container = document.getElementById('form-map-tags-container');
            var tags = [];
            for (var i = 0; i < container.childNodes.length; i++) {

                var badge = container.childNodes[i];
                for (var j = 0; j < badge.childNodes.length; j++) {
                    if (badge.childNodes[j].nodeType === Node.TEXT_NODE) {
                        tags.push(badge.childNodes[j].nodeValue);
                        break;
                    }
                }

            }

            document.getElementById('form-map-tags-field').value = JSON.stringify(tags);
        }

    </script>

    <div class="col-12 col-md-8">
    
        <form onsubmit="handleTags()" enctype="multipart/form-data" name="form-map"
            action="<@spring.url '/admin/maps/' + formdata.id />"
            method="POST" class="border rounded p-4">

                 <input type="hidden" id="form-map-visible" value="${formdata.visible?c}" name="visible">
                  <input type="hidden" name="tags" id="form-map-tags-field">

                <div class="mb-3">
                  <label for="form-map-name" class="form-label">Nom</label>
                  <input autocomplete="off" required="true" value="${formdata.name}" name="name" type="text" class="form-control" id="form-map-name" placeholder="Parcours de la semaine">
                </div>

                 <div class="mb-3">
                   <div class="mb-3">
                      <#if formdata.visible>
                            <button type="button" id="form-map-visible-toggle" class="btn btn-secondary" onClick="toggleMapVisibility()">La map est visible</button>
                      <#else>
                           <button type="button" id="form-map-visible-toggle" class="btn btn-outline-secondary" onClick="toggleMapVisibility()">La map est invisible</button>
                      </#if>
                  </div>
                </div>

                <div class="mb-3">
                  <label for="form-map-tags" class="form-label">Tags</label>
                  <div class="input-group">
                    <input autocomplete="off" id="form-map-tags" type="text" class="form-control" placeholder="tag">
                    <button class="btn btn-outline-secondary" type="button" onclick="addTag()"><strong><i class="bi bi-plus"></i></strong></button>
                 </div>
                 <div class="fs-5 p-2" id="form-map-tags-container"></div>
                </div>

                <hr/>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>

                <hr/>

                <h5>Informations sur la map</h5>

                <p>
                    <i class="bi bi-arrow-left-right"></i> ${formdata.length}km
                    <i class="bi bi-arrow-up"></i> ${formdata.positiveElevation}m
                    <i class="bi bi-arrow-down"></i> ${formdata.negativeElevation}m
                  </p>

                 <img src="<@spring.url '/api/maps/${formdata.id}/image' />" class="d-block shadow rounded w-75 h-auto mx-auto" alt="${formdata.id} image">




        </form>

    </div>

     <script type="text/javascript">

        JSON.parse('${(formdata.tags)?no_esc}').forEach(function(tag) {
            createTag(tag);
        });

        preventElementToSubmitForm('form-map-tags', addTag);

    </script>

</div>

<#include "_includes_tail.ftlh">