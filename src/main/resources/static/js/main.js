/** GLOBAL FORM BEHAVIOURS **/

var maxFileSizeInMB = 1;
var urlPattern = /(?:(?:https?|ftp):\/\/)?(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)(?:\.(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)*(?:\.(?:[a-z\x{00a1}\-\x{ffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?/ig;

var geoCodeTimeouts = {};
document.addEventListener("DOMContentLoaded", function() {

    Array.from(document.getElementsByClassName('form-size-check')).forEach(function(input) {
        input.addEventListener('change', (event) => {
            var target = event.target;
            if (target.files && target.files[0]) {
                if (target.files[0].size > (maxFileSizeInMB * 1024 * 1024)) {
                    new bootstrap.Modal(document.getElementById('modal-filetoobig')).show();
                    target.value = ''
                }
            }
        });

    });


    Array.from(document.getElementsByClassName('form-input-geocode')).forEach(function(input) {

        input.addEventListener('keyup', function(event) {

           if(geoCodeTimeouts[input.id]) {
               clearTimeout(geoCodeTimeouts[input.id]);
               delete geoCodeTimeouts[input.id];
           }

           if(input.value.length > 2) {
               geoCodeTimeouts[input.id] = setTimeout(function() {
                   geoCode(input.value, function(geocodeResponse) {
                       if(geocodeResponse !== null) {
                           input.value = geocodeResponse.label;
                           document.getElementById(input.id + '-lat').value = geocodeResponse.point.lat;
                           document.getElementById(input.id + '-lng').value = geocodeResponse.point.lng;
                       }
                   });
               }, 1000);
           }
       });

    });

    Array.from(document.getElementsByClassName('form-map-control')).forEach(function(input) {

        AutoComplete({
            EmptyMessage: "Aucune map trouvée",
            Url : '/api/autocomplete/maps',
            ClearOnSelect: true,
            OnSelect: function(field, value) {
                var jsonValue = JSON.parse(value);
                var targetIdField = document.getElementById(field.getAttribute("data-target-id-field"));
                var targetNameField = document.getElementById(field.getAttribute("data-target-name-field"));
                targetIdField.value = jsonValue.key;
                targetNameField.value = jsonValue.value;
            }
        }, '#' + input.id);

    });

    Array.from(document.getElementsByClassName('wrap-content')).forEach(function(contentContainer) {

          var text = contentContainer.innerHTML;

          text = text.replace(urlPattern, function (url) {
                             var protocol_pattern = /^(?:(?:https?|ftp):\/\/)/i;
                             var href = protocol_pattern.test(url) ? url : 'http://' + url;
                             return '<a href="' + href + '">' + url + '</a>';
                           });

          text = text.replace(new RegExp('\r?\n','g'), "<br />");

          contentContainer.innerHTML = text;

    });

});

/** FORMS **/

function setFieldValue(fieldId, value) {
    document.getElementById(fieldId).value = value;
}

function forceSubmitForm(formId) {
    document.getElementById(formId).submit();
}

function preventElementToSubmitForm(elementId, replacement) {

    var preventSubmit = function(event) {
        if(event.keyCode == 13) {
            event.preventDefault();
            event.stopPropagation();
            if(replacement) {
                replacement();
            }
            return false;
        }
    }

    document.getElementById(elementId).addEventListener('keypress', preventSubmit);
    document.getElementById(elementId).addEventListener('keydown', preventSubmit);
    document.getElementById(elementId).addEventListener('keyup', preventSubmit);
}

/** TAGS **/

function handleTagChange(containerId, fieldName, tagsContainerId, tagsFieldContainerId) {
    var tagSelect = document.getElementById(containerId);
    var newValue = tagSelect.value;
    if(newValue !== '' && getTags(tagsContainerId).indexOf(newValue) === -1) {
        createTag(tagSelect.value, fieldName, tagsContainerId, tagsFieldContainerId);
    }
    tagSelect.value = '';
}

function getTags(containerId) {
    var container = document.getElementById(containerId);
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

function createTag(label, fieldName, tagsContainerId, tagsFieldContainerId) {

    if(label === null || label === '') {
        return;
    }

    var fieldContainer = document.getElementById(tagsFieldContainerId);
    var badgeContainer = document.getElementById(tagsContainerId);

    var input = document.createElement('input');
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', fieldName);
    input.setAttribute('value', label);


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
        badgeContainer.removeChild(badge);
        fieldContainer.removeChild(input);
    });

    badge.appendChild(button);

    badgeContainer.appendChild(badge);
    fieldContainer.appendChild(input);

}

function addTag(inputId, fieldName, tagsContainerId, tagsFieldContainerId) {
   var tagInput = document.getElementById(inputId);
   createTag(tagInput.value, fieldName, tagsContainerId, tagsFieldContainerId);
   tagInput.value = '';
}

/** GEOCODE **/

var geoCodeModal = null;
function geoCode(toGeoCode, callback) {

    if(geoCodeModal === null) {
        geoCodeModal = new bootstrap.Modal(document.getElementById('modal-choose-geocode'));
    }

    var modalEl = document.getElementById('modal-choose-geocode');

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var r = JSON.parse(xmlHttp.responseText);
            if(r.features.length > 0) {
                var result = [];
                var geocodeList = document.getElementById('modal-choose-geocode-list');
                while (geocodeList.firstChild) {
                    geocodeList.removeChild(geocodeList.lastChild);
                }
                for(var i = 0; i < r.features.length; i++) {

                    var li = document.createElement("li");
                    li.classList.add('list-group-item');

                    var span = document.createElement("span");
                    span.textContent = r.features[i].properties.label;

                    var input = document.createElement("input");
                    input.classList.add('form-check-input');
                    input.classList.add('me-1');
                    input.setAttribute('type', 'radio');
                    input.setAttribute('name', 'geocode-selected-value');
                    input.setAttribute('value', ''+i);

                    li.appendChild(input);
                    li.appendChild(span);
                    geocodeList.appendChild(li);

                    result.push({
                        label : r.features[i].properties.label,
                        point: lambertToGPS({
                            x: r.features[i].properties.x,
                            y: r.features[i].properties.y
                        })
                    });
                }

                var li = document.createElement("li");
                li.classList.add('list-group-item');

                var span = document.createElement("span");
                span.textContent = 'Conserver l\'adresse saisie';

                var input = document.createElement("input");
                input.classList.add('form-check-input');
                input.classList.add('me-1');
                input.setAttribute('type', 'radio');
                input.setAttribute('name', 'geocode-selected-value');
                input.setAttribute('value', '-1');
                input.setAttribute('checked', 'checked');

                li.appendChild(input);
                li.appendChild(span);
                geocodeList.appendChild(li);

                geoCodeModal.show();

                var hideListener = function(event) {

                    var checked = modalEl.querySelector("input[type=radio]:checked");
                    if (checked && parseInt(checked.value) !== -1) {
                       callback(result[parseInt(checked.value)]);
                    } else {
                        callback(null);
                    }

                    modalEl.removeEventListener('hidden.bs.modal', hideListener, true);
                    geoCodeModalListener = null;
                }

                modalEl.addEventListener('hidden.bs.modal', hideListener, true);

            }
        }
    }
    xmlHttp.open("GET", "https://api-adresse.data.gouv.fr/search/?q="+encodeURI(toGeoCode), true); // true for asynchronous
    xmlHttp.send(null);
}


function lambertToGPS(lambert) {

    var c= 11754255.426096; //constante de la projection
    var e= 0.0818191910428158; //première exentricité de l'ellipsoïde
    var n= 0.725607765053267; //exposant de la projection
    var xs= 700000; //coordonnées en projection du pole
    var ys= 12655612.049876; //coordonnées en projection du pole

    var a=(Math.log(c/(Math.sqrt(Math.pow((lambert.x - xs),2)+Math.pow((lambert.y - ys),2))))/n);

    var lng = ((Math.atan(-(lambert.x-xs)/(lambert.y-ys)))/n+3/180*Math.PI)/Math.PI*180;
    var lat = Math.asin(Math.tanh((Math.log(c/Math.sqrt(Math.pow((lambert.x-xs),2)+Math.pow((lambert.y-ys),2)))/n)+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*(Math.tanh(a+e*Math.atanh(e*Math.sin(1))))))))))))))))))))))/Math.PI*180;

     return {
        lng : lng,
        lat : lat
     }

}