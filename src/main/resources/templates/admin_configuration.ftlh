<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <script type="text/javascript">

            function createTag(label) {

                if(label === null || label === '') {
                    return;
                }

                var badge = document.createElement("span");
                badge.classList.add('badge');
                badge.classList.add('bg-secondary');
                badge.classList.add('me-2');
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

                document.getElementById('maps-filter-tags-container').appendChild(badge);
            }

            function handleTags() {
               document.getElementById('form-map-tags-field').value = JSON.stringify(getTags());
            }

            function handleTagChange() {
                var tagSelect = document.getElementById('maps-filter-tags');
                var newValue = tagSelect.value;
                if(newValue !== '' && getTags().indexOf(newValue) === -1) {
                    createTag(tagSelect.value);
                }
                tagSelect.value = '';
            }

            function getTags() {
                var container = document.getElementById('maps-filter-tags-container');
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
                return tags;
            }

        </script>

        <form onsubmit="handleTags()" name="form-configuration" action="<@spring.url '/admin/configuration' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-configuration-timezone" class="form-label">Timezone</label>
                    <select required="true" name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="maps-filter-tags" class="form-label">Tags par défaut pour la recherche</label>
                    <div class="input-group">
                          <label class="input-group-text" for="maps-filter-tags" class="form-label">Tags</label>
                          <select name="tmp-tags" onchange="handleTagChange()" class="form-select" id="maps-filter-tags">
                            <option value="">---</option>
                            <#list tags as tag>
                                <option value="${tag}">${tag}</option>
                            </#list>
                          </select>
                      </div>
                      <div id="maps-filter-tags-container"></div>
                      <input type="hidden" name="defaultSearchTags" id="form-map-tags-field">
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

     <script type="text/javascript">

        JSON.parse('${(formdata.defaultSearchTags)?no_esc}').forEach(function(tag) {
            createTag(tag);
        });

        preventElementToSubmitForm('maps-filter-tags');

    </script>

</div>

<#include "_includes_tail.ftlh">