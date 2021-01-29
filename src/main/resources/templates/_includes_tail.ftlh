
    </div>


    <#if _config.soundEnabled>

        <script src="<@spring.url '/js/shake.js' />" type="text/javascript"></script>
        <script type="text/javascript">

            var shakeEvent = new Shake({ threshold: 5 });
            shakeEvent.start();

            window.addEventListener('shake', function () {
                var audio = new Audio("<@spring.url '/js/turkey.mp3' />");
                audio.play();
            }, false);

        </script>
    </#if>

</body>
</html>