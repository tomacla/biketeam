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

function geoCode(toGeoCode, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var r = JSON.parse(xmlHttp.responseText);
            if(r.features.length > 0) {
                callback(r.features[0].properties.label, lambertToGPS({
                    x: r.features[0].properties.x,
                    y: r.features[0].properties.y
                }));
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