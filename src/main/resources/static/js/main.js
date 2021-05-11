var maxFileSizeInMB = 1;

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

});

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