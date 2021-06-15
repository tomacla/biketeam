/** GLOBAL FORM BEHAVIOURS **/

var maxFileSizeInMB = 1;
var urlPattern = /(?:(?:https?|ftp):\/\/)?(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)(?:\.(?:[a-z\x{00a1}\-\x{ffff}0-9]+-?)*[a-z\x{00a1}\-\x{ffff}0-9]+)*(?:\.(?:[a-z\x{00a1}\-\x{ffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?/ig;

var geoCodeTimeouts = {};
document.addEventListener("DOMContentLoaded", function() {
    Array.from(document.getElementsByClassName('form-size-check')).forEach(initFormSizeCheck);
    Array.from(document.getElementsByClassName('form-input-geocode')).forEach(initGeoCode);
    Array.from(document.getElementsByClassName('form-map-control')).forEach(initMapAutoComplete);
    Array.from(document.getElementsByClassName('wrap-content')).forEach(wrapContent);
    Tags.init();

    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
      return new bootstrap.Tooltip(tooltipTriggerEl, {
        container: 'body'
      })
    })

});

function initFormSizeCheck(input) {
    input.addEventListener('change', (event) => {
            var target = event.target;
            if (target.files && target.files[0]) {
                if (target.files[0].size > (maxFileSizeInMB * 1024 * 1024)) {
                    new bootstrap.Modal(document.getElementById('modal-filetoobig')).show();
                    target.value = ''
                }
            }
        });
}

function initGeoCode(input) {

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

    }

function initMapAutoComplete(input) {

    var autocompleteField = new Autocomplete(input, {
        data: [],
        maximumItems: 10,
        treshold: 2,
        highlightClass: 'text-warning',
        onSelectItem: (selected) => {
            var targetIdField = document.getElementById(input.getAttribute("data-target-id-field"));
            var targetNameField = document.getElementById(input.getAttribute("data-target-name-field"));
            targetIdField.value = selected.value;
            targetNameField.value = selected.label;
            input.value = '';
        },
        onInput: (fieldValue) => {
            var xmlHttp = new XMLHttpRequest();
                xmlHttp.onreadystatechange = function() {
                    if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                        var r = JSON.parse(xmlHttp.responseText);
                        var result = [];
                        Object.keys(r).forEach(function(key) {
                            result.push({
                                label: r[key],
                                value: key
                            });
                        });
                        autocompleteField.setData(result);
                    }
                }
                xmlHttp.open("GET", "/api/" + input.getAttribute("data-team-id") + "/autocomplete/maps?q="+encodeURI(fieldValue), true); // true for asynchronous
                xmlHttp.send(null);
        }
    });

}

function wrapContent(contentContainer) {

      var text = contentContainer.innerHTML;

      text = text.replace(urlPattern, function (url) {
                         var protocol_pattern = /^(?:(?:https?|ftp):\/\/)/i;
                         var href = protocol_pattern.test(url) ? url : 'http://' + url;
                         return '<a class="link-dark" href="' + href + '">' + url + '</a>';
                       });

      text = text.replace(new RegExp('\r?\n','g'), "<br />");

      contentContainer.innerHTML = text;

}

/** FORMS **/

function setFieldValue(fieldId, value) {
    document.getElementById(fieldId).value = value;
}

function forceSubmitForm(formId) {
    document.getElementById(formId).submit();
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