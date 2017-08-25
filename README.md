
Use our code to save yourself time on cross-platform, cross-device and cross OS version development and testing
- The VideoList widget is designed for viewing video.
You can comment on your impressions of what you saw and put like.
Authorization via facebook, twitter or create a new account is implemented.

# XML Structure declaration
- App_name - the name of the mobile application. 
- Allowsharing - Ability to repost on your page in social networks
- Allowcomments - Ability to leave comments about the melody heard.
- Colorskin - this is the basic color scheme. Contains 5 elements (color [1-5]). Each widget can set colors for elements of the interface using color scheme in any order, but generally color1-background color, color3-titles color, color4-font color, color5-date or price color.
- Video - this is a root tag about video
- Url  - link to video
- Description - description of video
- Title - name of video
- Cover_image - link to cover of video
- Id - id track
# Tags:  
     <data>
         <app_name>BigApp</app_name>
         <allowsharing>on</allowsharing>
         <allowcomments>on</allowcomments>
         <allowlikes>on</allowlikes>
         <colorskin>
            <color1><![CDATA[#c2e793]]></color1>
            <color2><![CDATA[#2d910b]]></color2>
            <color3><![CDATA[#225112]]></color3>
            <color4><![CDATA[#313e20]]></color4>
            <color5><![CDATA[#2d910b]]></color5>
            <color6><![CDATA[rgba(255,255,255,0.2)]]></color6>
            <color7><![CDATA[rgba(255,255,255,0.15)]]></color7>
            <color8><![CDATA[rgba(0,0,0,0.3)]]></color8>
            <isLight><![CDATA[1]]></isLight>
         </colorskin>
         <video>
             <url><![CDATA[http://www.youtube.com/watch?v=W55b4BcCPM4]]></url>
             <description><![CDATA[Поджигаем 10 000 бенгальских свечей. Не повторяйте!Сотрудничество - slivkivideos@gmail.com
              Группа - https://vk.com/slivkishow
              Автор Вконтакте - https]]></description>
             <title><![CDATA[ЧТО БУДЕТ, ЕСЛИ ПОДЖЕЧЬ 10 000 БЕНГАЛЬСКИХ ОГНЕЙ!]]></title>
             <cover><![CDATA[https://i.ytimg.com/vi/W55b4BcCPM4/default.jpg]]></cover>
             <id><![CDATA[1529491983729510]]></id>
         </video>
         <video>
             <url><![CDATA[http://vimeo.com/15022912]]></url>
             <description><![CDATA[DE: Ein kurzer Film über die Ankunft in einer fremden Stadt.
              EN: A short film about the arrival in a foreign city.
              CH 2011, 1'56", Zeichentrick / dr]]></description>
             <title><![CDATA[Na cidade / In der Stadt]]></title>
             <cover><![CDATA[http://i.vimeocdn.com/video/307178327_1280.jpg]]></cover>
             <id><![CDATA[1529492833232153]]></id>
         </video>
    </data>
